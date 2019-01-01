package jsinterop.externs.generator.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

import jsinterop.externs.generator.JsNamedImpl;

public class AsmNameAnnotationVisitor extends AnnotationVisitor {
	private JsNamedImpl named;
	
	public AsmNameAnnotationVisitor(JsNamedImpl named) {
		super(Opcodes.ASM7);
		this.named = named;
		this.named.setAnnotated();
	}
	
	@Override
	public void visit(String name, Object value) {
		if(name.equals("namespace"))
			named.setJsNamespace((String)value);
		else if(name.equals("name"))
			named.setJsName((String)value);
		
		super.visit(name, value);
	}
}
