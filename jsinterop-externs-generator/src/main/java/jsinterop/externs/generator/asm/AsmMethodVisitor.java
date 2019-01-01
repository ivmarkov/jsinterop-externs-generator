package jsinterop.externs.generator.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import jsinterop.externs.generator.asm.ast.AsmMethod;

public class AsmMethodVisitor extends MethodVisitor {
	private AsmMethod method;

	public AsmMethodVisitor(AsmMethod method) {
		super(Opcodes.ASM7);
		this.method = method;
	}
	
	@Override
	public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
		if(visible) {
			String className = Type.getType(descriptor).getClassName();			

			if(className.equals("jsinterop.annotations.JsMethod"))
				return new AsmNameAnnotationVisitor(method);
			else if(className.equals("jsinterop.annotations.JsProperty")) {
				method.setProperty(true);
				return new AsmNameAnnotationVisitor(method);
			} else if(className.equals("jsinterop.annotations.JsOverlay")) {
				method.setOverlay(true);
				return super.visitAnnotation(descriptor, visible);
			}
		}

		return super.visitAnnotation(descriptor, visible);
	}
}
