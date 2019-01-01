package jsinterop.externs.generator.ast;

import java.util.function.Function;

public interface Field extends TypeMember {
	String getClosureType(Function<String, ? extends JsNamed> typesDictionary);
}
