package jsinterop.externs.generator.ast;

public interface JsNamed {
	String GLOBAL = "<global>";

	default boolean isInJsGlobalNamespace() {
		return GLOBAL.equals(getJsNamespace());
	}
	
	default String getJsFullName() {
		if(isInJsGlobalNamespace())
			return getJsName();
		else
			return getJsNamespace() + "." + getJsName();
	}
	
	boolean isAnnotated();
	
	String getJsNamespace();
	
	String getJsName();
}
