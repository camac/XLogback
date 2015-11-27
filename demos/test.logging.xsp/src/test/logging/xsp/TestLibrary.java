package test.logging.xsp;

import com.ibm.xsp.library.AbstractXspLibrary;

public class TestLibrary extends AbstractXspLibrary {

	public TestLibrary() {
	}
		
	@Override
	public String getLibraryId() {
		return "test.logging.xsp.library";
	}

	@Override
	public String getPluginId() {
		return "test.logging.xsp";
	}

	@Override
	public String[] getFacesConfigFiles() {
		return new String[] {
				"META-INF/example-faces-config.xml",
		};
	}

	@Override
	public String[] getXspConfigFiles() {
		return new String[] {
				"META-INF/exampleControl.xsp-config",
		};
	}

}
