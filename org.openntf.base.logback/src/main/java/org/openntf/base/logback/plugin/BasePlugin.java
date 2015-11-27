/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.base.logback.plugin;

import org.eclipse.core.runtime.Plugin;
import org.openntf.base.logback.config.AutoConfig;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

public class BasePlugin extends Plugin implements BundleActivator {

	private static BundleContext context;

    // FUTURE Multiple Context support should be added here. 
	//static {
	//	System.getProperties().setProperty("logback.ContextSelector", DominoContextSelector.class.getCanonicalName());
	//}
	
	public static BundleContext getContext() {
		return context;
	}

	private static void setContext(BundleContext paramContext) {
		context = paramContext;
	}
	
	public BasePlugin() {
	}
	
	public void start(BundleContext bundleContext) throws Exception {
		setContext(bundleContext);
		super.start(bundleContext);
		
		AutoConfig.init();
		
	}

	public void stop(BundleContext bundleContext) throws Exception {
		setContext(null);
		super.stop(bundleContext);
		
		try {
			LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
			lc.stop();
		} catch(Throwable t) {
			System.out.println("Unable to stop logger context: "+t.getMessage());
		}
		
	}
}
