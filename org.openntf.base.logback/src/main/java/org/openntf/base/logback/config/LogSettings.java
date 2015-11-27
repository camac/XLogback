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
package org.openntf.base.logback.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.openntf.base.logback.utils.LogUtils;
import org.openntf.base.logback.utils.StringUtils;
import org.openntf.base.logback.utils.Utils;

import ch.qos.logback.classic.Level;

/**
 * This class contains all internal configuration for the XLogback.
 * 
 * We use notes.ini for most of the settings. However, since we don't want to deal with Notes Sessions,
 * this class utilized file-based access for the notes.ini file.
 *  
 * We try to do our best to find the notes.ini file. Tested with standard Windows and Linux installations.
 * 
 * @author sbasegmez
 *
 */

public class LogSettings {
	
	public static final String SETTING_AUTO = "Auto";
	public static final String SETTING_DEBUG = "Debug";
	
	public static final String SETTING_CONSOLE_PATTERN = "ConsolePattern";
	public static final String SETTING_CONSOLE_LOGLEVEL = "ConsoleLogLevel";
	
	public static final String SETTING_OPENLOG_DBSERVER = "OpenLogDbServer";
	public static final String SETTING_OPENLOG_DBPATH = "OpenLogDbPath";
	public static final String SETTING_OPENLOG_SUPPRESSEVENTSTACK = "OpenLogSuppressEventStack";
	public static final String SETTING_OPENLOG_EXPIREDAYS = "OpenLogExpireDays";
	public static final String SETTING_OPENLOG_DEBUGLEVEL = "OpenLogDebugLevel";
	public static final String SETTING_OPENLOG_LOGLEVEL = "OpenLogLogLevel";
	public static final String SETTING_OPENLOG_DEFAULTAPP = "OpenLogDefaultApp";
	public static final String SETTING_OPENLOG_DEFAULTAGENT = "OpenLogDefaultAgent";

	public static final String SETTING_FILE_PATH = "FilePath";
	public static final String SETTING_FILE_MAXINDEX = "FileMaxIndex";
	public static final String SETTING_FILE_MAXSIZE = "FileMaxSize";
	public static final String SETTING_FILE_PATTERN = "FilePattern";
	public static final String SETTING_FILE_LOGLEVEL = "FileLogLevel";
	
	private static final String SETTING_DOMINO_LOGGING = "_DominoLogging";
	private static final String SETTING_DOMINO_DATA = "_DominoData";
	private static final String SETTING_DOMINO_PROGRAM = "_DominoPath";
	private static final String SETTING_XLB_PREFIX = "Xlb_";
	
	private static LogSettings instance = null;
	
	private Map<String, String> settings;
	
	private static final Map<String,String> defaultSettings = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
	static {
		defaultSettings.put(SETTING_AUTO, "1");
		defaultSettings.put(SETTING_DEBUG, "0");
		defaultSettings.put(SETTING_CONSOLE_PATTERN, "%-5level %msg%n%ex{1}");
		defaultSettings.put(SETTING_CONSOLE_LOGLEVEL, "INFO");
		defaultSettings.put(SETTING_OPENLOG_DBPATH, "OpenLog.nsf");
		defaultSettings.put(SETTING_OPENLOG_SUPPRESSEVENTSTACK, "1");
		defaultSettings.put(SETTING_OPENLOG_EXPIREDAYS, "0");
		defaultSettings.put(SETTING_OPENLOG_DEBUGLEVEL, "2");
		defaultSettings.put(SETTING_OPENLOG_LOGLEVEL, "INFO");
		defaultSettings.put(SETTING_OPENLOG_DEFAULTAPP, LogUtils.getPlatformName());
		defaultSettings.put(SETTING_FILE_MAXINDEX, "20");
		defaultSettings.put(SETTING_FILE_MAXSIZE, "2MB");
		defaultSettings.put(SETTING_FILE_PATTERN, "%date{dd/MM;HH:mm:ss}%level%msg%mdc{app}%marker%logger{26}");
		defaultSettings.put(SETTING_FILE_LOGLEVEL, "INFO");
	}
	
	public static LogSettings getDefaultInstance() {
		if(instance==null) {
			instance = new LogSettings();
			instance.loadNotesIniVars(getNotesIniFile());
		}
		
		return instance;
	}
		
	private LogSettings() {
		// Map implementation should be case insensitive.
		settings = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
	}
	
	private static File getNotesIniFile() {
		// FUTURE AIX/SOLARIS SUPPORT
		
		String progpath = System.getProperty("notes.binary");
	
		File iniFile = new File(Utils.toSafeFolder(progpath) + "notes.ini");
		
		if (!iniFile.exists()) {
			// This is for linux
			progpath = System.getProperty("user.dir");
			iniFile = new File(Utils.toSafeFolder(progpath) + "notes.ini");
		}
		
		if (!iniFile.exists()) {
			progpath = System.getProperty("java.home");
			if (progpath.endsWith("jvm")) {
				iniFile = new File(Utils.toSafeFolder(progpath) + ".." + Utils.FILE_SEPARATOR + "notes.ini");
			} else {
				iniFile = new File(Utils.toSafeFolder(progpath)  + "notes.ini");
			}
		}
	
		return iniFile;
	}

	private void loadNotesIniVars(File notesIniFile) {
		if(notesIniFile != null && notesIniFile.exists()) {
			FileReader reader = null;
			BufferedReader buffer = null;
			
			try {
				reader = new FileReader(notesIniFile);
				buffer = new BufferedReader(reader);

				String line = null;

				while((line = buffer.readLine())!=null) {
					processLine(line);
				}

			} catch (IOException e) {
				System.err.println("I/O error loading notes.ini content.");
				e.printStackTrace();
			} finally {
				try {
					buffer.close();
					reader.close();
				} catch (Throwable t) {
					// Nothing to do
				}
			}

		} else {
			System.err.println("Unable to find notes.ini file!");
		}
	}

	private void processLine(String line) {
		if(line == null || line.trim().equals("")) {
			// Nothing to process...
			return;
		}
		
		line = line.trim();
		int pos = line.indexOf("=");
		
		if(pos<0) {
			// No "=" sign. Skip the line
			return;
		}
		
		String param = line.substring(0, pos);
		String value = line.substring(pos+1);

		if(StringUtils.isEmpty(param)) {
			return;
		}
		
		if(StringUtils.equalsIgnoreCase("NotesProgram", param)) {
			setValue(SETTING_DOMINO_PROGRAM, value);
		} 
		
		if(StringUtils.equalsIgnoreCase("Directory", param)) {
			setValue(SETTING_DOMINO_DATA, value);
		} 
		
		if(StringUtils.equalsIgnoreCase("LogFile_Dir", param)) {
			setValue(SETTING_DOMINO_LOGGING, value);
		}
		
		if(param.toLowerCase(Locale.ENGLISH).startsWith(SETTING_XLB_PREFIX.toLowerCase(Locale.ENGLISH))) {
			setValue(param.substring(SETTING_XLB_PREFIX.length()), value);
		}
	}

	private void setValue(String param, String value) {
		if(StringUtils.isEmpty(param) || StringUtils.isEmpty(value)) {
			return;
		}

		settings.put(param, value);
	}

	private static Map<String, String> getSettings() {
		return getDefaultInstance().settings;
	}

	/**
	 * Returns value with any name from the settings. Setting will be effective in the following order:
	 * 
	 * 1. JVM Properties (Case sensitive)
	 * 2. Notes.ini parameters (Case insensitive)
	 * 3. Default values
	 * 
	 * XLB prefix will be added automatically. To get "Xlb_Auto" parameter, name should be "Auto".
	 * 
	 * @param name
	 * @return null if name is empty or value does not exist.
	 */
	public static String getStringValue(String name) {
		if(StringUtils.isEmpty(name)) return null;

		String sysValue = System.getProperty(SETTING_XLB_PREFIX+name);
		if(StringUtils.isNotEmpty(sysValue)) {
			return sysValue;
		}
		
		String value = getSettings().get(name);
		
		return StringUtils.defaultIfEmpty(value, defaultSettings.get(name));
	}

	/**
	 * Returns value with any name from the settings. Setting will be effective in the following order:
	 * 
	 * 1. JVM Properties (Case sensitive)
	 * 2. Notes.ini parameters (Case insensitive)
	 * 3. Default values
	 * 4. Given default value
	 * 
	 * XLB prefix will be added automatically. To get "Xlb_Auto" parameter, name should be "Auto".
	 * 
	 * @param name
	 * @param defaultIfEmpty 
	 * @return null if name is empty or value does not exist.
	 */
	public static String getStringValue(String name, String defaultIfEmpty) {
		String value = getStringValue(name);		
		return StringUtils.isEmpty(value) ? defaultIfEmpty : value;
	}

	/**
	 * Returns value with any name from the settings. Setting will be effective in the following order:
	 * 
	 * 1. JVM Properties (Case sensitive)
	 * 2. Notes.ini parameters (Case insensitive)
	 * 3. Default values
	 * 
	 * XLB prefix will be added automatically. To get "Xlb_Auto" parameter, name should be "Auto".
	 * 
	 * @param name
	 * @return null if name is empty, value does not exist or not integer.
	 */
	public static Integer getIntegerValue(String name) {
		if(StringUtils.isEmpty(name)) return null;

		String sysValue = System.getProperty(SETTING_XLB_PREFIX+name);
		if(Utils.isInteger(sysValue)) {
			return Integer.parseInt(sysValue);
		}
		
		String value = getSettings().get(name);
		if(Utils.isInteger(value)) {
			return Integer.parseInt(value);
		}
		
		String defValue = defaultSettings.get(name);
		if(Utils.isInteger(defValue)) {
			return Integer.parseInt(defValue);
		}
		
		return null;
	}

	/**
	 * Returns value with any name from the settings. Setting will be effective in the following order:
	 * 
	 * 1. JVM Properties (Case sensitive)
	 * 2. Notes.ini parameters (Case insensitive)
	 * 3. Default values
	 * 4. Given default value
	 * 
	 * XLB prefix will be added automatically. To get "Xlb_Auto" parameter, name should be "Auto".
	 * 
	 * @param name
	 * @param defaultIfEmpty 
	 * @return null if name is empty, value does not exist or not integer.
	 */
	public static int getIntegerValue(String name, int defaultIfEmpty) {
		Integer value = getIntegerValue(name);
		
		return value==null ? defaultIfEmpty : value;
	}

	/**
	 * Returns value with any name from the settings. Setting will be effective in the following order:
	 * 
	 * 1. JVM Properties (Case sensitive)
	 * 2. Notes.ini parameters (Case insensitive)
	 * 3. Default values
	 * 
	 * XLB prefix will be added automatically. To get "Xlb_Auto" parameter, name should be "Auto".
	 * 
	 * @param name
	 * @return null if name is empty, value does not exist or not boolean.
	 */
	public static Boolean getBooleanValue(String name) {
		if(StringUtils.isEmpty(name)) return null;

		String sysValue = System.getProperty(SETTING_XLB_PREFIX+name);
		if(Utils.isBoolean(sysValue)) {
			return StringUtils.equalsIgnoreCase(sysValue, "true");
		}
		
		String value = getSettings().get(name);
		if(Utils.isBoolean(value)) {
			return StringUtils.equalsIgnoreCase(value, "true");
		}
		
		String defValue = defaultSettings.get(name);
		if(Utils.isBoolean(defValue)) {
			return StringUtils.equalsIgnoreCase(defValue, "true");
		}
		
		return null;
	}

	/**
	 * Returns value with any name from the settings. Setting will be effective in the following order:
	 * 
	 * 1. JVM Properties (Case sensitive)
	 * 2. Notes.ini parameters (Case insensitive)
	 * 3. Default values
	 * 4. Given default value
	 * 
	 * XLB prefix will be added automatically. To get "Xlb_Auto" parameter, name should be "Auto".
	 * 
	 * @param name
	 * @param defaultIfEmpty 
	 * @return null if name is empty, value does not exist or not boolean.
	 */
	public static Boolean getBooleanValue(String name, boolean defaultIfEmpty) {
		Boolean value = getBooleanValue(name);
		
		return value==null ? defaultIfEmpty : value;
	}

	/**
	 * Returns value with any name from the settings. Setting will be effective in the following order:
	 * 
	 * 1. JVM Properties (Case sensitive)
	 * 2. Notes.ini parameters (Case insensitive)
	 * 3. Default values
	 * 
	 * XLB prefix will be added automatically. To get "Xlb_Auto" parameter, name should be "Auto".
	 * 
	 * @param name
	 * @return null if name is empty, value does not exist or not a log level.
	 */
	public static Level getLogLevelValue(String name) {
		if(StringUtils.isEmpty(name)) return null;

		String sysValue = System.getProperty(SETTING_XLB_PREFIX+name);
		if(Utils.isLogLevel(sysValue)) {
			return Level.toLevel(sysValue);
		}
		
		String value = getSettings().get(name);
		if(Utils.isLogLevel(value)) {
			return Level.toLevel(value);
		}
		
		return Level.toLevel(defaultSettings.get(name), null);
	}

	/**
	 * Returns value with any name from the settings. Setting will be effective in the following order:
	 * 
	 * 1. JVM Properties (Case sensitive)
	 * 2. Notes.ini parameters (Case insensitive)
	 * 3. Default values
	 * 4. Given default value
	 * 
	 * XLB prefix will be added automatically. To get "Xlb_Auto" parameter, name should be "Auto".
	 * 
	 * @param name
	 * @param defaultIfEmpty 
	 * @return null if name is empty, value does not exist or not a log level.
	 */
	public static Level getLogLevelValue(String name, Level defaultIfEmpty) {
		Level value = getLogLevelValue(name);
		
		return value==null ? defaultIfEmpty : value;
	}

	/**
	 * Domino program path should normally exist on the notes.ini file. In case it's missing, we use the related
	 * system property.
	 * 
	 * @return domino program directory, ends with '/' or '\' depending on the OS.
	 */
	public static String getDominoProgramPath() {
		return Utils.toSafeFolder(getStringValue(SETTING_DOMINO_PROGRAM, System.getProperty("notes.binary")));
	}

	/**
	 * Domino data path should normally exist on the notes.ini file. In case it's missing, we use different
	 * possibilities. Tested on Windows and Linux standard installations.
	 * 
	 * @return domino data directory, ends with '/' or '\' depending on the OS.
	 */
	public static String getDominoDataPath() {
		String pathData = getStringValue(SETTING_DOMINO_DATA);
		
		if(StringUtils.isEmpty(pathData)) {
			// This is compatible with Linux as well.
			pathData = System.getProperty("user.dir");
			
			if(StringUtils.isNotEmpty(pathData) && !pathData.toLowerCase(Locale.ENGLISH).endsWith("data")) {
				pathData += Utils.toSafeFolder(pathData) + "data";
			}
	
		}
		return Utils.toSafeFolder(pathData);
	}

	/**
	 * Domino logging path would be defined with a notes.ini parameter. If not defined, it will be 
	 * the "IBM_TECHNICAL_SUPPORT" folder under the data directory.
	 * 
	 * @return domino logging directory, ends with '/' or '\' depending on the OS.
	 */
	public static String getDominoLoggingPath() {
		String pathLogging = getStringValue(SETTING_DOMINO_LOGGING);
		
		if(StringUtils.isEmpty(pathLogging)) {
			pathLogging = Utils.toSafeFolder(getDominoDataPath()) + "IBM_TECHNICAL_SUPPORT";
		}
		return Utils.toSafeFolder(pathLogging);
	}

	/**
	 * Logback logging path would be defined with a notes.ini parameter. If not defined, it will be 
	 * the "xlogback" folder under the logging directory.
	 * 
	 * In case we have a configuration problem, we will use the RCP logging directory.
	 * 
	 * @return xlogback logging directory, ends with '/' or '\' depending on the OS.
	 */
	public static String getLogbackLoggingPath() {
		String logPath = getDominoLoggingPath();
		
		if(StringUtils.isEmpty(logPath)) {
			return Utils.toSafeFolder(System.getProperty("rcp.data"))+"logs";
		}
		
		return Utils.toSafeFolder(logPath)+"xlogback"+Utils.FILE_SEPARATOR;
	}

	/**
	 * Check for DEBUG parameter.
	 * 
	 * @return true if we are in debug mode.
	 */
	public static boolean inDebugMode() {
		int debug = getIntegerValue(SETTING_DEBUG, 0);

		return debug==1;
	}
	
	public static void main(String[] args) {
		// Should be moved in the test fragment.		
		System.out.println(LogSettings.getDominoDataPath());
		System.out.println(LogSettings.getDominoLoggingPath());
		System.out.println(LogSettings.getDominoProgramPath());
		System.out.println(LogSettings.getDefaultInstance().settings);
		
		// Check case insensitivity.
		System.out.println(getSettings().get("CONSOLELOGLEVEL"));		
		System.out.println(getSettings().get("_DOMINOPATH"));		
		
	}
	
}
