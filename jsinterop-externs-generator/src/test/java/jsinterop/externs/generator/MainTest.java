package jsinterop.externs.generator;

import org.junit.Test;

public class MainTest {
	@Test
	public void test() throws Exception {
		Main.main(new String[] {
			//"-s", 
			"-cp", "C:\\Users\\h151861\\git\\portal\\CP_ServerAPI\\bin;C:\\Users\\h151861\\.cp\\ivy_HEAD\\caches\\main\\com.github.gwtreact\\gwt-react\\jars\\gwt-react-1.0.0.jar;C:\\Users\\h151861\\.cp\\ivy_HEAD\\caches\\main\\com.github.gwtreact\\gwt-interop-utils\\jars\\gwt-interop-utils-1.0.0.jar;C:\\Users\\h151861\\.cp\\ivy_HEAD\\caches\\main\\com.google.elemental2\\elemental2-core\\jars\\elemental2-core-1.0.0-RC1.jar;C:\\Users\\h151861\\.cp\\ivy_HEAD\\caches\\main\\com.google.elemental2\\elemental2-dom\\jars\\elemental2-dom-1.0.0-RC1.jar;C:\\Users\\h151861\\.cp\\ivy_HEAD\\caches\\main\\com.google.gwt\\gwt-user\\jars\\gwt-user-2.8.2.jar",
			"-et", "C:\\Users\\h151861\\git\\portal\\CPGWT_Lib\\bin",
			"C:\\Users\\h151861\\externs.jar"});
	}
}
