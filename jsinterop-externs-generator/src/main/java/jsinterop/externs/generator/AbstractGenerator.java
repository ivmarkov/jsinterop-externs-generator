package jsinterop.externs.generator;

import java.io.PrintWriter;

public abstract class AbstractGenerator {
	protected final PrintWriter writer;

	public AbstractGenerator(PrintWriter writer) {
		this.writer = writer;
	}
	
	public abstract void emit();
	
	protected void emitJSDocOpen() {
		emit("/**\n");
	}
	
	protected void emitJSDocClose() {
		emit(" */\n");
	}
	
	protected void emitJSDocAnnotation(String annotation) {
		emit(" * @" + annotation + "\n");
	}
	
	protected void emit(String str) {
		writer.print(str);
	}
}
