package jsinterop.externs.generator;

import java.io.PrintWriter;
import java.util.function.Function;

import jsinterop.externs.generator.ast.Field;
import jsinterop.externs.generator.ast.JsNamed;

public class FieldGenerator extends AbstractGenerator {
	private Field field;
	private Function<String, ? extends JsNamed> typeNamesDictionary;
	private boolean onWindow;

	public FieldGenerator(Field field, boolean onWindow, Function<String, ? extends JsNamed> typeNamesDictionary, PrintWriter writer) {
		super(writer);
		this.field = field;
		this.onWindow = onWindow;
		this.typeNamesDictionary = typeNamesDictionary;
	}
	
	@Override
	public void emit() {
		System.out.print("  Emitting externs for field " + field.getJsName());

		if(field.isGeneric()) {
			System.out.println(" (SKIPPED - generic field)");
			return;
		} else
			System.out.println();
		
		emitJSDocOpen();
		emitJSDocAnnotation("type {" + field.getClosureType(typeNamesDictionary) + "}");
		emitJSDocClose();
		
		if(field.isStatic()) {
			if(onWindow)
				emit("window.");
			else if(field.isInJsGlobalNamespace())
				emit("var ");
			
			emit(field.getJsFullName() + ";\n");
		} else
			emit(field.getEnclosingType().getJsFullName() + ".prototype." + field.getJsName() + ";\n\n");
	}
}
