package jsinterop.externs.generator.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import jsinterop.externs.generator.asm.ast.AsmField;
import jsinterop.externs.generator.asm.ast.AsmMethod;
import jsinterop.externs.generator.asm.ast.AsmType;

public class AsmTypeVisitor extends ClassVisitor {
	private AsmType type;
	
	public AsmTypeVisitor() {
		super(Opcodes.ASM7);
	}

	public AsmType getType() {
		return type;
	}
	
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		type = new AsmType(
			access,
			name,
			signature,
			superName,
			interfaces);
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
		if(visible && Type.getType(descriptor).getClassName().equals("jsinterop.annotations.JsType"))
			return new AsmJsTypeAnnotationVisitor(type);
		else
			return null;
	}
	
	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
		return new AsmFieldVisitor(new AsmField(type, access, name, descriptor, signature));
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		if(!name.startsWith("lambda$")) // TODO: Figure out a better way to filter out javac's auto-generated lambda methods
			return new AsmMethodVisitor(new AsmMethod(type, access, name, descriptor, signature, exceptions));
		else
			return super.visitMethod(access, name, descriptor, signature, exceptions);
	}
}
