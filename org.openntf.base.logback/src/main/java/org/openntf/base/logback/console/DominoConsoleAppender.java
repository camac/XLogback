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
package org.openntf.base.logback.console;

import org.eclipse.core.runtime.Platform;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

public class DominoConsoleAppender<E> extends AppenderBase<E> {

	private Layout<E> layout;
	private IConsoleLogger console;
	
	@Override
	public void start() {
		super.start();
		
		if (this.layout == null) {
			addWarn("No layout set for the appender named \"" + name + "\".");
	    }

		selectConsoleLogger();
		addInfo("Console logging started.");
	}

	private void selectConsoleLogger() {
		if(null != Platform.getBundle("com.ibm.dots")) {
			this.console = new DotsConsoleLogger();
			addInfo("DOTS environment detected. Switching to DotsConsoleLogger.");
		} else {
			this.console = new DefaultConsoleLogger();
		}
	}

	public void logMessage(String message) {
		if(null!=message) 
			console.logMessage(message.replace("%", "%%"));
	}
    
    public Layout<E> getLayout() {
		return layout;
	}

	public void setLayout(Layout<E> layout) {
		this.layout = layout;
	}

	@Override
	protected void append(E event) {
		if (!isStarted()) {
			return;
		}
		
		String message;
		if(null==layout) {
			message = event.toString();
		} else {
			message = layout.doLayout(event);
		}
		
		logMessage(message);
	}

}
