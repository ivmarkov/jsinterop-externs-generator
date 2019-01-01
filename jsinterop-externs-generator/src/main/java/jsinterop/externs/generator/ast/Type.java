package jsinterop.externs.generator.ast;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public interface Type extends JsNamed {
	String getClassName();
	
	String getSuperClosureType(Function<String, Type> typesDictionary);

	List<String> getInterfaceClosureTypes(Function<String, Type> typesDictionary);
	
	Collection<? extends TypeMember> getMembers();
	
	boolean isGeneric();
	
	default boolean isJsExportable() {
		return 
			isTypeItselfJsExportable() 
			|| getMembers().stream().anyMatch(TypeMember::isJsExportable);
	}
	
	default boolean isTypeItselfJsExportable() {
		return isAnnotated() && isNative() && !isPseudoClass();
	}
	
	boolean isInterface();
	
	boolean isPseudoClass();
	
	boolean isNative();
}
