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
package org.openntf.base.logback.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class LogUtils {

	public static String getStackTrace(Throwable ee) {
		if (ee == null)
			return "";

		try {
			StringWriter sw = new StringWriter();
			ee.printStackTrace(new PrintWriter(sw));
			return sw.toString();
		} catch (Exception e) {
		}

		return "";
	}

	public static Vector<String> getStackTraceVector(Throwable ee) {
		return getStackTraceVector(ee, 0);
	}

	public static Vector<String> getStackTraceVector(Throwable ee, int skip) {
		Vector<String> v = new Vector<String>(32);
		try {
			StringTokenizer st = new StringTokenizer(getStackTrace(ee), "\n");
			int count = 0;
			while (st.hasMoreTokens()) {
				if (skip <= count++) {
					v.addElement(st.nextToken().trim());
				} else {
					st.nextToken();
				}
			}

		} catch (Exception e) {
		}

		return v;
	}

	public static String getPlatformName() {
		String platform = System.getProperty("dots.mq.name");
		return StringUtils.defaultIfEmpty(platform, "XSP");
	}

	public static void reloadDefaultConfiguration() {

		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

		ContextInitializer ci = new ContextInitializer(loggerContext);
		URL url = ci.findURLOfDefaultConfigurationFile(true);

		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(loggerContext);
			loggerContext.reset();
			configurator.doConfigure(url);
		} catch (JoranException je) {
			// StatusPrinter will handle this
		}
		StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);

	}

}
