package jsinterop.externs.generator.asm.ast;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import jsinterop.externs.generator.ClosureTypeFactory;
import jsinterop.externs.generator.ast.JsNamed;
import jsinterop.externs.generator.ast.Method;

public class AsmMethod extends AsmTypeMember implements Method {
	private String asmDescriptor;
	private String[] asmExceptions;

	private boolean property;
	private boolean overlay;
	
	public AsmMethod(AsmType type, int asmAccess, String asmName, String asmDescriptor, String asmSignature, String[] asmExceptions) {
		super(type, asmAccess, asmName, asmSignature);
		this.asmDescriptor = asmDescriptor;
		this.asmExceptions = asmExceptions != null? asmExceptions: new String[0];
	}
	
	@Override
	public boolean isJsExportable() {
		return isMemberItselfJsExportable()
			|| !isStatic() && getEnclosingType().isTypeItselfJsExportable() && !isOverlay() && !isConstructor();
	}

	@Override
	public boolean isMemberItselfJsExportable() {
		return !isGeneric() && isStatic() && isAnnotated() && isNative();
	}
	
	@Override
	public boolean isStatic() {
		return (asmAccess&Opcodes.ACC_STATIC) != 0;
	}
	
	@Override
	public String getJsName() {
		if(super.getJsName() != null)
			return super.getJsName();
		else {
			if(property) {
				if(asmName.startsWith("is"))
					return lowercase(asmName.substring(2));
				else if(asmName.startsWith("get") || asmName.startsWith("set"))
					return lowercase(asmName.substring(3));
			}
			
			return asmName;
		}
	}
	
	public boolean isProperty() {
		return property;
	}
	
	public void setProperty(boolean property) {
		this.property = property;
	}

	public boolean isOverlay() {
		return overlay;
	}
	
	public void setOverlay(boolean overlay) {
		this.overlay = overlay;
	}

	public boolean isNative() {
		return !getEnclosingType().isInterface() && (asmAccess&Opcodes.ACC_NATIVE) != 0;
	}

	@Override
	public boolean isConstructor() {
		return !isStatic() && asmName.equals("<init>");
	}
	
	private static String lowercase(String propName) {
		return propName.length() > 0? Character.toLowerCase(propName.charAt(0)) + propName.substring(1): propName;
	}

	@Override
	public String getReturnClosureType(Function<String, ? extends JsNamed> typeNamesDictionary) {
		return ClosureTypeFactory.toClosureType(Type.getReturnType(asmDescriptor).getClassName(), typeNamesDictionary, null);
	}

	@Override
	public List<String> getParameterNames() {
		return IntStream.range(1, Type.getArgumentTypes(asmDescriptor).length + 1)
			.mapToObj(index -> "p" + index)
			.collect(Collectors.toList());
	}

	@Override
	public List<String> getParameterClosureTypes(Function<String, ? extends JsNamed> typeNamesDictionary) {
		return Arrays.stream(Type.getArgumentTypes(asmDescriptor))
			.map(Type::getClassName)
			.map(type -> ClosureTypeFactory.toClosureType(type, typeNamesDictionary, null))
			.collect(Collectors.toList());	
	}

	@Override
	public List<String> getThrowsClosureTypes(Function<String, ? extends JsNamed> typeNamesDictionary) {
		return Arrays.stream(asmExceptions)
			.map(Type::getObjectType)
			.map(Type::getClassName)
			.map(className -> ClosureTypeFactory.toClosureType(className, typeNamesDictionary, null))
			.collect(Collectors.toList());	
	}
}
