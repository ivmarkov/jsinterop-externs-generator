package jsinterop.externs.generator;

import java.util.Date;
import java.util.function.Function;

import jsinterop.externs.generator.ast.JsNamed;

public class ClosureTypeFactory {
	private ClosureTypeFactory() {}
	
	public static String toClosureType(String javaType, Function<String, ? extends JsNamed> typesDictionary, Boolean nullable) {
		if(javaType.endsWith("[]")) {
			String componentType = toClosureType(javaType.substring(0, javaType.length() - 2), typesDictionary, nullable);
			if(componentType.equals("Object"))
				return "Array";
			else
				return "Array<" +  componentType + ">";
		} else if(javaType.equals(void.class.getName()))
			return null;
		else if(
				javaType.equals(byte.class.getName())
				|| javaType.equals(short.class.getName())
				|| javaType.equals(char.class.getName())
				|| javaType.equals(int.class.getName())
				|| javaType.equals(float.class.getName())
				|| javaType.equals(double.class.getName()))
			return "number";
		if(
				javaType.equals(Byte.class.getName())
				|| javaType.equals(Short.class.getName())
				|| javaType.equals(Character.class.getName())
				|| javaType.equals(Integer.class.getName())
				|| javaType.equals(Float.class.getName())
				|| javaType.equals(Double.class.getName()))
			return toPrimitiveType("number", nullable);
		else if(javaType.equals(boolean.class.getName()))
			return "boolean";
		else if(javaType.equals(Boolean.class.getName()))
			return toPrimitiveType("boolean", nullable);
		else if(javaType.equals(Date.class.getName()))
			return toObjectType("Date", nullable);
		else if(javaType.equals(Object.class.getName()))
			return toObjectType("Object", nullable);
		else if(javaType.equals(String.class.getName()))
			return toPrimitiveType("string", nullable);
		else
			return toObjectType(typesDictionary.apply(javaType).getJsFullName(), nullable);
	}
	
	private static String toPrimitiveType(String type, Boolean nullable) {
		return (nullable == null || nullable? "?": "") + type;
	}

	private static String toObjectType(String type, Boolean nullable) {
		return (nullable != null && !nullable? "!": "") + type;
	}
}
