package jsinterop.externs.generator;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import jsinterop.externs.generator.ast.JsNamed;
import jsinterop.externs.generator.ast.Type;

public class Generator {
	private Path path;
	private boolean singleFile;
	private boolean onWindow;
	private Function<String, Type> typesDictionary;
	private Function<String, ? extends JsNamed> typeNamesDictionary;
	private Collection<String> generatedNamespaces;

	public Generator(
			Path path, 
			boolean singleFile, 
			boolean onWindow, 
			Function<String, Type> typesDictionary,
			Function<String, ? extends JsNamed> typeNamesDictionary,
			Collection<String> generatedNamespaces) {
		this.path = path;
		this.singleFile = singleFile;
		this.onWindow = onWindow;
		this.typesDictionary = typesDictionary;
		this.typeNamesDictionary = typeNamesDictionary;
		this.generatedNamespaces = generatedNamespaces;
	}

	public void emit(Stream<? extends Type> types) throws IOException {
		try(PrintWriter writer = singleFile? new PrintWriter(new OutputStreamWriter(Files.newOutputStream(path))): null) { 
			types
				.filter(Type::isJsExportable)
				.forEach(type -> {
					System.out.print("Emitting externs for type " + type.getClassName());

					if(type.isGeneric()) {
						System.out.println(" (SKIPPED - generic type)");
						return;
					} else
						System.out.println();
					
					try {
						Path innerPath;
						
						if(!singleFile) {
							if(!type.isInJsGlobalNamespace()) {
								innerPath = resolve(path, type.getJsNamespace());
								Files.createDirectories(innerPath);
							
								streamSubNamespaces(type.getJsNamespace())
									.filter(namespace -> !generatedNamespaces.contains(namespace))
									.forEach(namespace -> {
										try {
											try(PrintWriter internalWriter = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(resolve(path, namespace).resolve("type-info.js"))))) {
												new NamespaceGenerator(generatedNamespaces, namespace, onWindow, false/*recursive*/, internalWriter).emit();
											}
										} catch(IOException e) {
											throw new UncheckedIOException(e);
										}
									});
							} else
								innerPath = path;
						} else
							innerPath = null;
						
						try(PrintWriter internalWriter = !singleFile? new PrintWriter(new OutputStreamWriter(Files.newOutputStream(innerPath.resolve(type.getJsName() + ".js")))): null) {
							new TypeGenerator(type, onWindow, typesDictionary, typeNamesDictionary, generatedNamespaces, internalWriter != null? internalWriter: writer).emit();
						}
					} catch(IOException e) {
						throw new RuntimeException(e);
					}
				});
		}
	}
	
	private static Stream<String> streamSubNamespaces(String namespace) {
		int pos = namespace.lastIndexOf('.');
		if(pos >= 0)
			return Stream.concat(streamSubNamespaces(namespace.substring(0,  pos)), Stream.of(namespace));
		else
			return Stream.of(namespace);
	}

	private static Path resolve(Path path, String namespace) {
		return Arrays
			.stream(namespace.split("\\."))
			.reduce(path, Path::resolve, Path::resolve);
	}
}
