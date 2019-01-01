package jsinterop.externs.generator;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jsinterop.externs.generator.ast.Field;
import jsinterop.externs.generator.ast.JsNamed;
import jsinterop.externs.generator.ast.Method;
import jsinterop.externs.generator.ast.Type;
import jsinterop.externs.generator.ast.TypeMember;

public class TypeGenerator extends AbstractGenerator {
	private Type type;
	private boolean onWindow;
	private Function<String, Type> typesDictionary;
	private Function<String, ? extends JsNamed> typeNamesDictionary;
	private Collection<String> generatedNamespaces;

	public TypeGenerator(
			Type type,
			boolean onWindow,
			Function<String, Type> typesDictionary,
			Function<String, ? extends JsNamed> typeNamesDictionary,
			Collection<String> generatedNamespaces,
			PrintWriter writer) {
		super(writer);
		this.type = type;
		this.onWindow = onWindow;
		this.typesDictionary = typesDictionary;
		this.typeNamesDictionary = typeNamesDictionary;
		this.generatedNamespaces = generatedNamespaces;
	}
	
	@Override
	public void emit() {
		if(type.isTypeItselfJsExportable()) {
			new NamespaceGenerator(generatedNamespaces, type.getJsNamespace(), onWindow, true/*recursive*/, writer).emit();
			
			Method constructor;
			
			emitJSDocOpen();
			
			if(type.isInterface() || type.isPseudoClass()) {
				emitJSDocAnnotation("record");
				
				if(type.isPseudoClass()) {
					String superType = type.getSuperClosureType(typesDictionary);
					if(!superType.equals("Object"))
						emitJSDocAnnotation("extends {" + superType + "}");
				}
				
				type.getInterfaceClosureTypes(typesDictionary)
					.stream()
					.forEach(type -> emitJSDocAnnotation("extends {" + type + "}"));
				
				constructor = null;
			} else {
				emitJSDocAnnotation("constructor");

				String superType = type.getSuperClosureType(typesDictionary);
				if(!superType.equals("Object"))
					emitJSDocAnnotation("extends {" + superType + "}");
				
				type.getInterfaceClosureTypes(typesDictionary)
					.stream()
					.forEach(type -> emitJSDocAnnotation("implements {" + type + "}"));
			
				constructor = type.getMembers().stream()
					.filter(member -> member instanceof Method)
					.map(member -> (Method)member)
					.filter(Method::isConstructor)
					.findAny()
					.orElse(null);
				
				if(constructor != null) {
					IntStream.range(0, constructor.getParameterNames().size())
						.forEach(index -> {
							String type = constructor.getParameterClosureTypes(typeNamesDictionary).get(index);
							String name = constructor.getParameterNames().get(index);
							
							emitJSDocAnnotation("param {" + type + "} " + name);
						});
				}
			}
			
			emitJSDocClose();
			
			if(onWindow)
				emit("window.");
			else if(type.isInJsGlobalNamespace())
				emit("var ");
			
			emit(type.getJsFullName() + " = function(" + (constructor != null? constructor.getParameterNames().stream().collect(Collectors.joining(", ")): "") + ") {};\n\n");
		}

		type.getMembers().stream()
			.filter(TypeMember::isJsExportable)
			.forEach(member -> {
				if(member instanceof Method)
					new MethodGenerator((Method)member, onWindow, typeNamesDictionary, writer).emit();
				else
					new FieldGenerator((Field)member, onWindow, typeNamesDictionary, writer).emit();
			});
	}
}
