package jsinterop.externs.generator;

import jsinterop.externs.generator.ast.JsNamed;

public class JsNamedImpl implements JsNamed {
	private boolean annotated;
	private String namespace;
	private String name;

	public JsNamedImpl() {
	}
	
	public JsNamedImpl(String fullName) {
		int pos = fullName.lastIndexOf('.');
		if(pos >= 0) {
			this.namespace = fullName.substring(0, pos);
			this.name = fullName.substring(pos + 1);
		} else {
			this.namespace = GLOBAL;
			this.name = fullName;
		}
	}
	
	public JsNamedImpl(String namespace, String name) {
		this.namespace = namespace;
		this.name = name;
	}
	
	public boolean isAnnotated() {
		return annotated;
	}
	
	public void setAnnotated() {
		this.annotated = true;
	}
	
	@Override
	public String getJsNamespace() {
		return namespace;
	}
	
	public void setJsNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	@Override
	public String getJsName() {
		return name;
	}
	
	public void setJsName(String name) {
		this.name = name;
	}
}
