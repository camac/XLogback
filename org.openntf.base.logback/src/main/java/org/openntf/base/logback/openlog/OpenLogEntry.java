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

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Name;
import lotus.domino.NotesException;
import lotus.domino.RichTextItem;
import lotus.domino.Session;

import org.openntf.base.logback.core.LoggingException;
import org.openntf.base.logback.utils.LogUtils;
import org.openntf.base.logback.utils.StringUtils;
import org.openntf.base.logback.utils.Utils;

/**
 * The original implementation of OpenLogEntry is based on;
 * 
 * - OpenLogItem class, XPages OpenLog Logger project, licensed under the Apache License.
 *   - http://www.openntf.org/main.nsf/project.xsp?r=project/XPages%20OpenLog%20Logger
 *   - Authors: Paul S. Withers, Nathan T. Freeman, Tim Tripcony
 * - OpenLog Project, licensed under the Apache License
 *   - http://www.openntf.org/main.nsf/project.xsp?r=project/OpenLog
 *   - Author: Julian Robichaux
 * 
 * Renamed for convenience.
 * 
 */

public class OpenLogEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_SEVERITY = "INFO";

	private static final String TYPE_EVENT = "Event";
	private static final String TYPE_ERROR = "Error";

	private static final String LOG_FORM_NAME = "LogEvent";
	private static final String AGENT_LANGUAGE = "JAVA";

	private final OpenLogAppender appender;

	private Throwable baseException = null;

	private String message = "";

	private String eventType = TYPE_EVENT;
	private String eventSeverity = DEFAULT_SEVERITY;

	private long timeStamp = System.currentTimeMillis();

	private String fromAgent = null;
	private String fromApp = null;

	private String marker = "";
	
	private String loggedDbUrl = "";
	private String loggedDbPath = "";
	private String loggedDbAccessLevel = "";

	private String loggedDocUrl = "";

	public OpenLogEntry(OpenLogAppender appender) {
		this.appender = appender;
	}

	public Throwable getBaseException() {
		return baseException;
	}

	public void setBaseException(Throwable baseException) {
		this.baseException = baseException;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public boolean isEvent() {
		return org.openntf.base.logback.utils.StringUtils.equalsIgnoreCase(eventType, TYPE_EVENT);
	}

	public void setEvent(boolean event) {
		eventType = event ? TYPE_EVENT : TYPE_ERROR;
	}

	public boolean isError() {
		return org.openntf.base.logback.utils.StringUtils.equalsIgnoreCase(eventType, TYPE_ERROR);
	}

	public void setError(boolean error) {
		eventType = error ? TYPE_ERROR : TYPE_EVENT;
	}

	public String getEventSeverity() {
		return eventSeverity;
	}

	public void setEventSeverity(String eventSeverity) {
		if(org.openntf.base.logback.utils.StringUtils.isNotEmpty(eventSeverity)) {
			this.eventSeverity = eventSeverity;
		}
	}
	
	public String getFromApp() {
		return fromApp;
	}

	public void setFromApp(String fromApp) {
		this.fromApp = fromApp;
	}

	public String getFromAgent() {
		return fromAgent;
	}

	public void setFromAgent(String fromAgent) {
		this.fromAgent = fromAgent;
	}
	
	public String getMarker() {
		return this.marker;
	}

	public void setMarker(String marker) {
		this.marker = marker;
	}

	public void setLoggedDoc(Document loggedDoc) {
		if(loggedDoc == null) return;
		
		try {
			this.loggedDocUrl = loggedDoc.getNotesURL();
		} catch (NotesException e) {
			// Nothing to do...
		}
	}
	
	public void setLoggedDb(Database loggedDb) {
		if(loggedDb == null) return;
		
		try {
			this.loggedDbUrl = loggedDb.getNotesURL();
			this.loggedDbPath = loggedDb.getFilePath();
			this.loggedDbAccessLevel = Utils.getAccessLevel(loggedDb);
		} catch (NotesException e) {
			// Nothing to do...
		}
	}

	public String getLoggedDbPath() {
		return loggedDbPath;
	}

	public String getLoggedDbAccessLevel() {
		return loggedDbAccessLevel;
	}

	public String getLoggedDocUrl() {
		return loggedDocUrl;
	}

	public String getLoggedDbUrl() {
		return loggedDbUrl;
	}

	public boolean save(Database logDb) throws LoggingException {

		Document logDoc = null;
		RichTextItem rtitem = null;
				
		try {
			Session session = logDb.getParent();
			
			String userName = session.getUserName(); 
			String effectiveUserName = session.getEffectiveUserName();
			
			Name serverNameName = session.createName(session.getServerName());
			String serverName = serverNameName.getCommon();
			Utils.recycleObject(serverNameName);
			
			logDoc = logDb.createDocument();
			rtitem = logDoc.createRichTextItem("LogDocInfo");

			logDoc.appendItemValue("Form", LOG_FORM_NAME);

			Throwable ee = getBaseException();

			if (null != ee) {
				StackTraceElement ste = ee.getStackTrace()[0];
				if (ee instanceof NotesException) {
					logDoc.replaceItemValue("LogErrorNumber", ((NotesException) ee).id);
					logDoc.replaceItemValue("LogErrorMessage", ((NotesException) ee).text);
				} else {
					if(StringUtils.isEmpty(getMessage())) {
						logDoc.replaceItemValue("LogErrorMessage", ee.getStackTrace()[0].toString());
					} else {
						logDoc.replaceItemValue("LogErrorMessage", getMessage());
					}
				}

				if (isError() || ! appender.isSuppressEventStack()) {
					logDoc.replaceItemValue("LogStackTrace", LogUtils.getStackTraceVector(ee));
				}

				logDoc.replaceItemValue("LogErrorLine", ste.getLineNumber());
				logDoc.replaceItemValue("LogFromMethod", ste.getClassName() + "." + ste.getMethodName());
			} else {
				logDoc.replaceItemValue("LogErrorMessage", getMessage());
			}

			Utils.saveDateField(logDoc, "LogEventTime", new Date(getTimeStamp()));
			Utils.saveDateField(logDoc, "LogAgentStartTime", new Date(getTimeStamp()));

			logDoc.replaceItemValue("LogEventType", eventType);

			// If greater than 32k, put in logDocInfo
			if (getMessage().length() > 32000) {
				rtitem.appendText(getMessage());
				rtitem.addNewLine();
			} else {
				logDoc.replaceItemValue("LogMessage", getMessage());
			}

			if(org.openntf.base.logback.utils.StringUtils.isNotEmpty(getFromApp())) {
				logDoc.replaceItemValue("LogFromDatabase", getFromApp());
			}
			
			logDoc.replaceItemValue("LogUserRoles", Utils.getUserRoles(session));
			logDoc.replaceItemValue("LogClientVersion", Utils.getClientVersion(session));
			logDoc.replaceItemValue("LogAgentLanguage", AGENT_LANGUAGE);

			logDoc.replaceItemValue("LogFromServer", serverName);
			logDoc.replaceItemValue("LogUserName", userName);
			logDoc.replaceItemValue("LogEffectiveName", effectiveUserName);

			logDoc.replaceItemValue("LogSeverity", getEventSeverity());
			logDoc.replaceItemValue("LogFromAgent", getFromAgent());
			logDoc.replaceItemValue("LogMarker", getMarker());
			
			if(org.openntf.base.logback.utils.StringUtils.isNotEmpty(getLoggedDbUrl())) {
				logDoc.replaceItemValue("LogFromDatabase", getLoggedDbPath());
				logDoc.replaceItemValue("LogAccessLevel", getLoggedDbAccessLevel());

				rtitem.appendText("The database associated with this event is:");
				rtitem.addNewLine(1);
				rtitem.appendText("Database Url: " + getLoggedDbUrl());
				rtitem.addNewLine(1);
			}
			
			if(org.openntf.base.logback.utils.StringUtils.isNotEmpty(getLoggedDocUrl())) {
				try {
					rtitem.appendText("The document associated with this event is:");
					rtitem.addNewLine(1);
					rtitem.appendText("Document Url: " + getLoggedDocUrl());
					rtitem.addNewLine(1);
				} catch (Throwable t) {
					// Ignoring errors here...
				} finally {
					// No recycle here. Developer is responsible 
				}
			}

			// Set expiry date, if defined
			if (appender.getLogExpireDays()>0) {
				try {
					Calendar expireDate = Calendar.getInstance();
					expireDate.setTimeInMillis(getTimeStamp());
					expireDate.add(Calendar.DATE, appender.getLogExpireDays());
					
					Utils.saveDateField(logDoc, "ExpireDate", expireDate);

				} catch (Throwable t) {
					//debugPrint(new RuntimeException("Non-numeric value for xsp.openlog.expireDate, so cannot be set to auto-expire", t));
				}
			}

			return logDoc.save(false, false);
			
		} catch (Throwable t) {
			throw new LoggingException("Unable to save OpenLog document", t);
		} finally {
			Utils.recycleObjects(rtitem, logDoc);
		}
	}

}
