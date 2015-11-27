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
package org.openntf.base.logback.openlog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;

import org.openntf.base.logback.core.LoggingException;
import org.openntf.base.logback.utils.DominoRunner;
import org.openntf.base.logback.utils.StringUtils;
import org.openntf.base.logback.utils.Utils;
import org.openntf.base.logback.utils.DominoRunner.SessionRoutine;
import org.slf4j.MDC;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;

public class OpenLogAppender extends AppenderBase<ILoggingEvent> {

	private static final String DEFAULT_LOGDBPATH = "OpenLog.nsf"; 
	private static final int MAX_COUNT_QUEUE = 10;
	
	private String defaultApp;
	private String defaultAgent;
	
	private String targetDbServer = "";
	private String targetDbPath = "";

	private boolean suppressEventStack = false;
	private int logExpireDays = 0;
	private int debugLevel = 2;
	
	private List<OpenLogEntry> queue = new ArrayList<OpenLogEntry>();
	
	@Override
	public void start() {

		if(StringUtils.isEmpty(targetDbPath)) {
			addError("OpenLog database has not been set. OpenLog logger will fail.");
			return;
		}

		super.start();
		addInfo("OpenLog logging started.");
	}

	@Override
	protected void append(ILoggingEvent event) {
		if (!isStarted()) {
			return;
		}

		// TODO Needs Guarding against repetitive reentries.
		
		Object[] args = event.getArgumentArray();
		Document sourceDoc = null;
		Database sourceDb = null;
		
		try {
			if(args!=null && args.length>0) {
				for(int i=0; i<args.length; i++) {
					if(args[i] instanceof Document) {
						sourceDoc = (Document)args[i];
						args[i] = "[DocId:"+sourceDoc.getNoteID()+"]";
					} else if(args[i] instanceof Database) {
						sourceDb = (Database)args[i];
						args[i] = "[DB:"+sourceDb.getFilePath()+"]";
					}
				}
			}
		} catch (NotesException e) {
			addWarn("Unexpected error recovering SourceDoc", e);
		}
		
		addToOpenLog(event, sourceDoc, sourceDb);
	}

	protected void addToOpenLog(final ILoggingEvent event, final Document sourceDoc, final Database sourceDb) {
		boolean isEvent = (event.getLevel() != ch.qos.logback.classic.Level.ERROR);
		String message = event.getFormattedMessage();
		String severity = event.getLevel().levelStr;

		ThrowableProxy tp = (ThrowableProxy) event.getThrowableProxy();
		
		OpenLogEntry item = new OpenLogEntry(this);

		if (null != tp) {
			item.setBaseException(tp.getThrowable());

			if(StringUtils.isEmpty(message)) {
				message = (null != tp.getMessage()) ? tp.getMessage() : tp.getClass().getCanonicalName();
			}
		}

		item.setMessage(message);
		
		item.setLoggedDb(sourceDb);
		item.setLoggedDoc(sourceDoc);
		item.setEvent(isEvent);
		item.setEventSeverity(severity);
		item.setFromAgent(getAgent());
		item.setFromApp(getApp());
		
		queue.add(item);
		
		sendToLog();
		checkQueue();
	}

	private void sendToLog() {
		DominoRunner.runWithSession(false, new SessionRoutine<Boolean>() {

			@Override
			public Boolean doRun(Session session) {
				sendToLog(session);
				return null;
			}

			@Override
			public Boolean fallback() {
				addWarn("We can't have a session yet. Next time...");
				return false;
			}

			@Override
			public Boolean onException(Throwable t) {
				addError("Unable to write to OpenLog.", t);
				return false;
			}
		});
	}

	protected void sendToLog(Session session) {
		
		Database logDb = null;
		try {
			logDb = session.getDatabase(getTargetDbServer(), getTargetDbPath(), false);

			if(logDb!=null) {
				for (Iterator<OpenLogEntry> iterator = queue.iterator(); iterator.hasNext();) {
					OpenLogEntry item = (OpenLogEntry) iterator.next();
					
					if(item.save(logDb)) {
						iterator.remove();
					}
				}
			}
		
		} catch (NotesException e) {
			addError("Notes Error processing OpenLogEntry", e);
		} catch (LoggingException e) {
			addError("Error processing OpenLogEntry", e);
		} finally {
			Utils.recycleObject(logDb);
		}
		
	}

	protected void checkQueue() {
		if(queue.size()>MAX_COUNT_QUEUE) {
			addError("OpenLog has too much log entries in the queue. It will stop now.");
			stop();
		}
	}
	
	protected String getAgent() {
		String agentSet = MDC.get("agent");

		if(StringUtils.isNotEmpty(agentSet)) {
			return agentSet;
		}

		if(StringUtils.isNotEmpty(getDefaultAgent())) {
			return getDefaultAgent();
		}
		
		return null;
	}

	protected String getApp() {
		String appSet = MDC.get("app");
		
		if(StringUtils.isNotEmpty(appSet)) {
			return appSet;
		}
		
		if(StringUtils.isNotEmpty(getDefaultApp())) {
			return getDefaultApp();
		}
		
		return null;
	}
	
	public String getDefaultApp() {
		return defaultApp;
	}

	public void setDefaultApp(String defaultApp) {
		this.defaultApp = defaultApp;
	}

	public String getDefaultAgent() {
		return defaultAgent;
	}

	public void setDefaultAgent(String defaultAgent) {
		this.defaultAgent = defaultAgent;
	}

	public String getTargetDbServer() {
		return StringUtils.isEmpty(targetDbServer) ? targetDbServer : "";
	}

	/**
	 * OpenLog database server for log entries. 
	 * 
	 */
	public void setTargetDbServer(String targetDbServer) {
		this.targetDbServer = targetDbServer;
	}

	public String getTargetDbPath() {
		return StringUtils.isNotEmpty(targetDbPath) ? targetDbPath : DEFAULT_LOGDBPATH;
	}

	/**
	 * OpenLog database file path for log entries. 
	 * 
	 */
	public void setTargetDbPath(String targetDbPath) {
		this.targetDbPath = targetDbPath;
	}

	public boolean isSuppressEventStack() {
		return suppressEventStack;
	}

	/** 
	 * boolean whether or not to suppress stack trace for Events
	 * 
	 */
	public void setSuppressEventStack(boolean suppressEventStack) {
		this.suppressEventStack = suppressEventStack;
	}

	public int getLogExpireDays() {
		return logExpireDays;
	}

	/** 
	 * If set, next log entries will be set 'to be expired in XX days'
	 * 
	 */
	public void setLogExpireDays(int logExpireDays) {
		this.logExpireDays = logExpireDays;
	}

	public int getDebugLevel() {
		return debugLevel;
	}

	/**
	 * The "debug level" of all the methods. Right now the valid debug levels are:
	 * 
	 * 0 -- internal errors are discarded
	 * 1 -- Exception messages from internal errors are printed
	 * 2 -- stack traces from internal errors are also printed [Default]
	 * 
	 */
	public void setDebugLevel(int debugLevel) {
		this.debugLevel = debugLevel;
	}

}