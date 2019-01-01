package jsinterop.externs.generator;

import java.io.PrintWriter;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jsinterop.externs.generator.ast.JsNamed;
import jsinterop.externs.generator.ast.Method;

public class MethodGenerator extends AbstractGenerator {
	private Method method;
	private boolean onWindow;
	private Function<String, ? extends JsNamed> typeNamesDictionary;

	public MethodGenerator(Method method, boolean onWindow, Function<String, ? extends JsNamed> typeNamesDictionary, PrintWriter writer) {
		super(writer);
		this.method = method;
		this.onWindow = onWindow;
		this.typeNamesDictionary = typeNamesDictionary;
	}
	
	@Override
	public void emit() {
		System.out.print("  Emitting externs for method " + method.getJsName());

		if(method.isGeneric()) {
			System.out.println(" (SKIPPED - generic method)");
			return;
		} else
			System.out.println();
		
		emitJSDocOpen();

		IntStream.range(0, method.getParameterNames().size())
			.forEach(index -> {
				String type = method.getParameterClosureTypes(typeNamesDictionary).get(index);
				String name = method.getParameterNames().get(index);
				
				emitJSDocAnnotation("param {" + type + "} " + name);
			});
			
		String returnType = method.getReturnClosureType(typeNamesDictionary);
		if(returnType != null)
			emitJSDocAnnotation("return {" + returnType + "}");

		method.getThrowsClosureTypes(typeNamesDictionary)
			.stream()
			.forEach(type -> emitJSDocAnnotation("throws {" + type + "}"));
		
		emitJSDocClose();
		
		if(method.isStatic()) {
			if(onWindow)
				emit("window.");
			else if(method.isInJsGlobalNamespace())
				emit("var ");
			
			emit(method.getJsFullName());
		} else {
			if(onWindow)
				emit("window.");
			
			emit(method.getEnclosingType().getJsFullName() + ".prototype." + method.getJsName());
		}
		
		emit(" = function(" + method.getParameterNames().stream().collect(Collectors.joining(", ")) + ") {};\n\n");
	}
}
