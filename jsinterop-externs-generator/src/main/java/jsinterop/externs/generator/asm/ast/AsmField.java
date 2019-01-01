package jsinterop.externs.generator.asm.ast;

import java.util.function.Function;

import jsinterop.externs.generator.ClosureTypeFactory;
import jsinterop.externs.generator.ast.Field;
import jsinterop.externs.generator.ast.JsNamed;

public class AsmField extends AsmTypeMember implements Field {
	private String asmDescriptor;

	public AsmField(AsmType type, int asmAccess, String asmName, String asmDescriptor, String asmSignature) {
		super(type, asmAccess, asmName, asmSignature);
		this.asmDescriptor = asmDescriptor;
	}

	@Override
	public String getClosureType(Function<String, ? extends JsNamed> typeNamesDictionary) {
		return ClosureTypeFactory.toClosureType(org.objectweb.asm.Type.getType(asmDescriptor).getClassName(), typeNamesDictionary, null); 
	}
}
