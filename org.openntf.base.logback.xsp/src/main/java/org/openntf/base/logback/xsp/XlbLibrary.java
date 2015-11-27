package org.openntf.base.logback.xsp;

import org.openntf.base.logback.xsp.plugin.XspPlugin;

import com.ibm.xsp.library.AbstractXspLibrary;

public class XlbLibrary extends AbstractXspLibrary {

	public XlbLibrary() {
	}
		
	@Override
	public String getLibraryId() {
		return XspPlugin.class.getPackage().getName()+".library";
	}

	@Override
	public String getPluginId() {
		return XspPlugin.getContext().getBundle().getSymbolicName();
	}

    @Override
    public String[] getDependencies() {
        return new String[] {
        };
    }
	
	@Override
	public String[] getFacesConfigFiles() {
		return new String[] {
				"META-INF/xlb-faces-config.xml",
		};
	}

	@Override
	public String[] getXspConfigFiles() {
		return new String[] {
				"META-INF/xlb.xsp-config",
		};
	}

}
