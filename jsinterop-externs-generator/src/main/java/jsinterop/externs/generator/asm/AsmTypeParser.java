package jsinterop.externs.generator.asm;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.objectweb.asm.ClassReader;

import jsinterop.externs.generator.asm.ast.AsmType;

public class AsmTypeParser {
	private AsmTypeParser() {}
	
	public static AsmType parse(Path classFile) {
		try(InputStream in = Files.newInputStream(classFile)) {
			AsmTypeVisitor visitor = new AsmTypeVisitor();
			
			new ClassReader(in).accept(visitor, ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);
			
			return visitor.getType();
		} catch(UnsupportedOperationException e) {
			// Stay silent and don't export the file
			return null;
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
