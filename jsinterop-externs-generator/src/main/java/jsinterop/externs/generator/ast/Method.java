package jsinterop.externs.generator.ast;

import java.util.List;
import java.util.function.Function;

public interface Method extends TypeMember {
	String getReturnClosureType(Function<String, ? extends JsNamed> typesDictionary);

	List<String> getParameterNames();
	List<String> getParameterClosureTypes(Function<String, ? extends JsNamed> typesDictionary);
	
	List<String> getThrowsClosureTypes(Function<String, ? extends JsNamed> typesDictionary);
	
	boolean isConstructor();
}
