package org.openntf.base.logback.xsp.plugin;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class XspPlugin extends Plugin implements BundleActivator {

	private static BundleContext context;

	public static BundleContext getContext() {
		return context;
	}

	private static void setContext(BundleContext paramContext) {
		context = paramContext;
	}
	
	public XspPlugin() {
	}
	
	public void start(BundleContext bundleContext) throws Exception {
		setContext(bundleContext);
		super.start(bundleContext);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		setContext(null);
		super.stop(bundleContext);
	}
}
