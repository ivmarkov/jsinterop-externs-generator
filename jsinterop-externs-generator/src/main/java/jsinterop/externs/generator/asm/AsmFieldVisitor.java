package jsinterop.externs.generator.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import jsinterop.externs.generator.asm.ast.AsmField;

public class AsmFieldVisitor extends FieldVisitor {
	private AsmField field;

	public AsmFieldVisitor(AsmField field) {
		super(Opcodes.ASM7);
		this.field = field;
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
		if(visible && Type.getType(descriptor).getClassName().equals("jsinterop.annotations.JsProperty"))
			return new AsmNameAnnotationVisitor(field);
		else
			return super.visitAnnotation(descriptor, visible);
	}
}
