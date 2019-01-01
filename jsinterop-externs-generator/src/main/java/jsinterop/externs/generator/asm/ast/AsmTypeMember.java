package jsinterop.externs.generator.asm.ast;

import org.objectweb.asm.Opcodes;

import jsinterop.externs.generator.JsNamedImpl;
import jsinterop.externs.generator.ast.TypeMember;

public abstract class AsmTypeMember extends JsNamedImpl implements TypeMember {
	private AsmType type;
	protected final int asmAccess;
	protected final String asmName;
	protected final String asmSignature;
	
	protected AsmTypeMember(AsmType type, int asmAccess, String asmName, String asmSignature) {
		this.type = type;
		this.asmAccess = asmAccess;
		this.asmName = asmName;
		this.asmSignature = asmSignature;
		type.add(this);
	}
	
	@Override
	public AsmType getEnclosingType() {
		return type;
	}

	@Override
	public boolean isGeneric() {
		return asmSignature != null;
	}
	
	@Override
	public boolean isStatic() {
		return (asmAccess&Opcodes.ACC_STATIC) != 0;
	}

	@Override
	public String getJsNamespace() {
		if(isStatic() && super.getJsNamespace() != null)
			return super.getJsNamespace();
		else
			return getEnclosingType().getJsFullName();
	}

	@Override
	public String getJsName() {
		if(super.getJsName() != null)
			return super.getJsName();
		else
			return asmName;
	}
}
