package jsinterop.externs.generator;

import java.io.PrintWriter;
import java.util.Collection;

import jsinterop.externs.generator.ast.JsNamed;

public class NamespaceGenerator extends AbstractGenerator {
	private Collection<String> generatedNamespaces;
	private String namespace;
	private boolean onWindow;
	private boolean recursive;

	public NamespaceGenerator(Collection<String> generatedNamespaces, String namespace, boolean onWindow, boolean recursive, PrintWriter writer) {
		super(writer);
		this.generatedNamespaces = generatedNamespaces;
		this.namespace = namespace;
		this.onWindow = onWindow;
		this.recursive = recursive;
	}
	
	@Override
	public void emit() {
		if(namespace.equals(JsNamed.GLOBAL) || generatedNamespaces.contains(namespace))
			return;
		
		if(recursive) {
			int pos = namespace.lastIndexOf('.');
			if(pos >= 0)
				new NamespaceGenerator(generatedNamespaces, namespace.substring(0, pos), onWindow, true/*recursive*/, writer).emit();
		}
		
		System.out.println("  Emitting externs for namespace " + namespace);
		
		emitJSDocOpen();

		emitJSDocAnnotation("const");

		emitJSDocClose();
		
		if(onWindow)
			emit("window.");
		else if(namespace.indexOf('.') == -1)
			emit("var ");
		
		emit(namespace);
		emit(" = {};\n\n");
		
		generatedNamespaces.add(namespace);
	}
}
