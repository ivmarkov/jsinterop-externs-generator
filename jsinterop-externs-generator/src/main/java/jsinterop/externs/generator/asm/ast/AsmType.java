package jsinterop.externs.generator.asm.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;

import jsinterop.externs.generator.ClosureTypeFactory;
import jsinterop.externs.generator.JsNamedImpl;
import jsinterop.externs.generator.ast.Type;
import jsinterop.externs.generator.ast.TypeMember;

public class AsmType extends JsNamedImpl implements Type {
	private int asmAccess;
	private String asmName;
	private String asmSignature;
	private String asmSuperName;
	private String[] asmInterfaces;
	
	private boolean isNative;

	private Collection<AsmTypeMember> members;
	
	public AsmType(int asmAccess, String asmName, String asmSignature, String asmSuperName, String[] asmInterfaces) {
		this.asmAccess = asmAccess;
		this.asmName = asmName;
		this.asmSignature = asmSignature;
		this.asmSuperName = asmSuperName;
		this.asmInterfaces = asmInterfaces;
		
		this.members = new ArrayList<>();
	}

	void add(AsmTypeMember member) {
		members.add(member);
	}

	@Override
	public boolean isGeneric() {
		return asmSignature != null;
	}
	
	@Override
	public String getClassName() {
		return org.objectweb.asm.Type.getObjectType(asmName).getClassName();
	}
	
	@Override
	public String getSuperClosureType(Function<String, Type> typesDictionary) {
		return ClosureTypeFactory.toClosureType(org.objectweb.asm.Type.getObjectType(asmSuperName).getClassName(), typesDictionary, null);
	}
	
	@Override
	public List<String> getInterfaceClosureTypes(Function<String, Type> typesDictionary) {
		return Arrays.stream(asmInterfaces)
			.map(org.objectweb.asm.Type::getObjectType)
			.map(org.objectweb.asm.Type::getClassName)
			.map(className -> ClosureTypeFactory.toClosureType(className, typesDictionary, null))
			.collect(Collectors.toList());
	}
	
	@Override
	public Collection<? extends TypeMember> getMembers() {
		return members;
	}

	@Override
	public boolean isInterface() {
		return (asmAccess&Opcodes.ACC_INTERFACE) != 0;
	}
	
	@Override
	public boolean isPseudoClass() {
		return !isInterface() && isAnnotated() && isNative() && "Object".equals(super.getJsName()) && GLOBAL.equals(super.getJsNamespace());
	}
	
	@Override
	public boolean isNative() {
		return isNative;
	}
	
	public void setIsNative(boolean isNative) {
		this.isNative = isNative;
	}
	
	@Override
	public String getJsNamespace() {
		if(/*!isPseudoClass() &&*/ super.getJsNamespace() != null)
			return super.getJsNamespace();
		else {
			String className = getClassName().replace('$', '.');
			
			int pos = className.lastIndexOf('.');
			if(pos < 0)
				return GLOBAL;
			else
				return className.substring(0, pos);
		}
	}
	
	@Override
	public String getJsName() {
		if(/*!isPseudoClass() &&*/ super.getJsName() != null)
			return super.getJsName();
		else {
			String className = getClassName().replace('$', '.');
			
			int pos = className.lastIndexOf('.');
			if(pos < 0)
				return className;
			else
				return className.substring(pos + 1);
		}
	}
}
