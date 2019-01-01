package jsinterop.externs.generator.asm;

import jsinterop.externs.generator.asm.ast.AsmType;

public class AsmJsTypeAnnotationVisitor extends AsmNameAnnotationVisitor {
	private AsmType type;
	
	public AsmJsTypeAnnotationVisitor(AsmType type) {
		super(type);
		this.type = type;
	}
	
	@Override
	public void visit(String name, Object value) {
		if(name.equals("isNative"))
			type.setIsNative((Boolean)value);
		
		super.visit(name, value);
	}
}
