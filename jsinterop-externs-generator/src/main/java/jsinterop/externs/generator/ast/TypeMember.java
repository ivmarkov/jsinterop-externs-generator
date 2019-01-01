package jsinterop.externs.generator.ast;

public interface TypeMember extends JsNamed {
	Type getEnclosingType();

	boolean isGeneric();
	
	boolean isStatic();

	default boolean isJsExportable() {
		return isMemberItselfJsExportable()
			|| !isStatic() && getEnclosingType().isTypeItselfJsExportable();
	}

	default boolean isMemberItselfJsExportable() {
		return false;
	}
}
