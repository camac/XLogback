package test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

public class JavaTest01 {

	private static Logger logger = LoggerFactory.getLogger(JavaTest01.class);
	
	public static void testError() {
		logger.error("Some Error from a static method inside a Java class inside an NSF!", new Throwable("Test detail message for error"));
	}
	
	public static void testWarningWithMarker() {
		logger.warn(MarkerFactory.getMarker("TestMarker"), "Warning message from a static method inside a Java class inside an NSF with a Marker");
	}
	
}
