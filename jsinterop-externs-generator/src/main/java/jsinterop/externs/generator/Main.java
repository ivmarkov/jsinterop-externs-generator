package jsinterop.externs.generator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import jsinterop.externs.generator.asm.AsmTypeParser;
import jsinterop.externs.generator.ast.JsNamed;
import jsinterop.externs.generator.ast.Type;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class Main implements Runnable {
	@Parameters(index = "0", arity="1", description = "A file or a directory where to generate the externs")
	protected Path externsPath;
	
	@Option(names = {"-s", "-singlefile"}, description = "Put all generated externs in a single JavaScript file")
	protected boolean singleFile;
	
	@Option(names = {"-et", "-externtypes"}, split = ":", description = "A sequence of directories and/or JAR files containing native JS types for which externs should be generated")
	protected Collection<Path> externTypesPath;
	
	@Option(names = {"-cp", "-classpath"}, split = ":", description = "A sequence of directories and/or JAR files containing types referenced from native JS types")
	protected Collection<Path> classPath;
	
	private Function<Path, ? extends Type> typeParser;

	protected Main() {
		this.typeParser = AsmTypeParser::parse;
	}
	
	protected Main(
			Path externsPath,
			boolean singleFile,
			Collection<Path> externTypesPath,
			Collection<Path> classPath) {
		this();
		this.externsPath = externsPath;
		this.singleFile = singleFile;
		this.externTypesPath = externTypesPath;
		this.classPath = classPath;
	}

	public void run() {
		try {
			Collection<Type> externTypes = new ArrayList<>();
			for(Path p: externTypesPath)
				processPath(p, path -> getClassFiles(path)
					.map(typeParser)
					.forEach(externTypes::add));
			
			Map<String, Type> typesMap = new HashMap<>(externTypes.stream()
				.collect(Collectors.toMap(Type::getClassName, Function.identity())));
			
			Collection<FileSystem> openedJars = new ArrayList<>();
				
			try {
				Collection<Path> expandedClassPath = expandJars(classPath, openedJars);
	
				URI zipUri;
				Map<String, String> zipEnv;
				if(!singleFile && (!Files.exists(externsPath) || !Files.isDirectory(externsPath))) {
				    // Use a Zip filesystem URI
					
					Files.deleteIfExists(externsPath);
				    
				    URI fileUri = externsPath.toUri();
				    zipUri = new URI("jar:" + fileUri.getScheme(), fileUri.getPath(), null);

				    zipEnv = new HashMap<>();
				    zipEnv.put("create", Boolean.TRUE.toString());
				} else {
					zipUri = null;
					zipEnv = null;
				}
			
				try(FileSystem zipFs = zipUri != null? FileSystems.newFileSystem(zipUri, zipEnv): null) {
					Path expandedExternsPath = zipFs != null? zipFs.getRootDirectories().iterator().next(): externsPath;
							
					new Generator(
							expandedExternsPath, 
							singleFile,
							false/*onWindow*/,
							key -> findType(expandedClassPath, key, typesMap).orElseThrow(() -> new IllegalStateException("Cannot find class " + key)),
							key -> findType(expandedClassPath, key, typesMap).map(type -> (JsNamed)type).orElseGet(() -> fallbackTypeName(key)),
							new HashSet<>())
						.emit(externTypes.stream());
				}
			} finally {
				for(FileSystem jar: openedJars)
					jar.close();
			}
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		} catch(URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Optional<Type> findType(Collection<Path> classPath, String className, Map<String, Type> typesMap) {
		Optional<Type> type = Optional.ofNullable(typesMap.get(className));
		if(!type.isPresent()) {
			type = findClassFile(classPath, className)
				.map(typeParser)
				.map(t -> (Type)t);
			
			if(type.isPresent())
				typesMap.put(className, type.get());
		}
		
		return type;
	}
	
	private static JsNamed fallbackTypeName(String className) {
		if(className.startsWith("java.") || className.startsWith("javax."))
			return new JsNamedImpl(className);
		else
			throw new IllegalStateException("Cannot find class " + className);
	}
	
	private static Optional<Path> findClassFile(Collection<Path> paths, String className) {
		String[] classFilePathSegments = className.split("\\.");
		classFilePathSegments[classFilePathSegments.length - 1] = classFilePathSegments[classFilePathSegments.length - 1] + ".class";
		
		return paths.stream()
			.map(path -> resolve(path, classFilePathSegments))
			.filter(Files::exists)
			.filter(Files::isRegularFile)
			.findAny();
	}
	
	private static Stream<Path> getClassFiles(Path path) {
		try {
			if(Files.isDirectory(path))
				return Files
					.list(path)
					.flatMap(Main::getClassFiles);
			else if(path.getFileName().toString().endsWith(".class"))
				return Stream.of(path);
			else
				return Stream.empty();
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	private static Path resolve(Path path, String... segments) {
		return Arrays
			.stream(segments)
			.reduce(path, Path::resolve, Path::resolve); 
	}
	
	private static void processPath(Path path, Consumer<Path> consumer) throws IOException {
		if(Files.isDirectory(path) || !isJar(path))
			consumer.accept(path);
		else {
			try(FileSystem jar = FileSystems.newFileSystem(path, null/*loader*/)) {
				StreamSupport
					.stream(jar.getRootDirectories().spliterator(), false/*parallel*/)
					.forEach(consumer);
			}
		}
	}
	
	private static Collection<Path> expandJars(Collection<Path> paths, Collection<FileSystem> openedJars) {
		return paths.stream()
			.flatMap(path -> {
				try {
					if(Files.isDirectory(path))
						return Stream.of(path);
					else if(isJar(path)) {
						FileSystem jar = FileSystems.newFileSystem(path, null/*loader*/);
						openedJars.add(jar);
						
						return StreamSupport.stream(jar.getRootDirectories().spliterator(), false/*parallel*/);
					} else
						return Stream.empty();
				} catch(IOException e) {
					throw new UncheckedIOException(e);
				}
			})
			.collect(Collectors.toList());
	}
	
	private static boolean isJar(Path path) {
		String pathStr = path.toString().toLowerCase();
		
		return pathStr.endsWith(".zip") || pathStr.endsWith(".jar");
	}

	public static void main(String[] args) throws Exception {
		CommandLine.run(new Main(), args);
		
//		Path externsPath = Paths.get("C:", "Users", "h151861", "externs.js");
//		
//		boolean singleFile = true;
//		
//		Collection<Path> externTypesPath = Arrays.asList(Paths.get("C:", "Users", "h151861", "git", "portal", "CPGWT_Lib", "bin"));
//		
//		Collection<Path> classPath = Arrays.asList(
//			Paths.get("C:", "Users", "h151861", "git", "portal", "CP_ServerAPI", "bin"),
//			Paths.get("C:", "Users", "h151861", ".cp", "ivy_HEAD", "caches", "main", "com.github.gwtreact", "gwt-react", "jars", "gwt-react-1.0.0.jar"),
//			Paths.get("C:", "Users", "h151861", ".cp", "ivy_HEAD", "caches", "main", "com.github.gwtreact", "gwt-interop-utils", "jars", "gwt-interop-utils-1.0.0.jar"),
//			Paths.get("C:", "Users", "h151861", ".cp", "ivy_HEAD", "caches", "main", "com.google.elemental2", "elemental2-core", "jars", "elemental2-core-1.0.0-RC1.jar"),
//			Paths.get("C:", "Users", "h151861", ".cp", "ivy_HEAD", "caches", "main", "com.google.elemental2", "elemental2-dom", "jars", "elemental2-dom-1.0.0-RC1.jar"),
//			Paths.get("C:", "Users", "h151861", ".cp", "ivy_HEAD", "caches", "main", "com.google.gwt", "gwt-user", "jars", "gwt-user-2.8.2.jar"));
//		
//		new Main(
//				externsPath, 
//				singleFile, 
//				externTypesPath, 
//				classPath)
//			.emit();
	}
}
