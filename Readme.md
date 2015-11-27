**XLogback Project**  
*Version 0.90 Preview*

OPENNTF

    This project is an OpenNTF project, and is available under the Apache License V2.0.  
    All other aspects of the project, including contributions, defect reports, discussions,
    feature requests and reviews are subject to the OpenNTF Terms of Use - available at
    http://openntf.org/Internal/home.nsf/dx/Terms_of_Use.

<!-- TOC depth:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Introduction](#introduction)
	- [Features](#features)
	- [Why Logback?](#why-logback)
	- [XPages OpenLog Logger vs. XLogback](#xpages-openlog-logger-vs-xlogback)
- [How to start?](#how-to-start)
- [Configuration Options](#configuration-options)
	- [Automatic Configuration](#automatic-configuration)
		- [Console Appender:](#console-appender)
		- [Rolling File Appender with HTML Layout](#rolling-file-appender-with-html-layout)
		- [OpenLog Appender](#openlog-appender)
		- [Automatic Configuration Options](#automatic-configuration-options)
	- [Configuration File (XML or Groovy)](#configuration-file-xml-or-groovy)
		- [Sample Logback.xml](#sample-logbackxml)
		- [Provide a configuration file](#provide-a-configuration-file)
		- [Declare from your own plugin](#declare-from-your-own-plugin)
	- [Custom Configuration](#custom-configuration)
- [Under the hood:](#under-the-hood)
	- [Logback Configuration in Deep](#logback-configuration-in-deep)
- [How to Contribute](#how-to-contribute)
<!-- /TOC -->


# Introduction

XLogback is a plugin project to integrate [Logback Project](http://logback.qos.ch/) into the Domino OSGi environment.

The purpose of the project is to provide universal logging platform for all Java codes in the IBM Domino OSGi platform, including XPages apps/plugins, servlets and DOTS (Domino OSGi Tasklet Services) with a single implementation.

## Features

- Self-contained plugin without any third party dependencies for all of your OSGi projects,
- The core plugin will work for HTTP and DOTS contexts at the same time.
- OpenLogAppender implementation for logging into OpenLog database.
- Automatic Configuration using Notes.ini parameters.
- XSP and Designer plugin for logging inside XPages Applications (under Development)

## Why Logback?

[Logback](http://logback.qos.ch/) is a reliable, generic, fast and flexible logging library based on SLF4J implementation. Refer to the project page for more information.

## XPages OpenLog Logger vs. XLogback

[XPages OpenLog Logger](http://www.openntf.org/main.nsf/project.xsp?r=project/XPages%20OpenLog%20Logger) is another OpenNTF project for utilizing OpenLog database by [Paul S. Withers](http://www.intec.co.uk/author/paulwithers/). If you are an XPages developer and want to use OpenLog as your primary logging vehicle, both projects do not differ a lot.

XPages OpenLog Logger has a great advantage of simplicity for XPages developers. If you are using OpenNTF Domino API, you can already use it by now. Even you don't want to install any plugins for your environment, you can easily import necessary libraries into your NSF and it will work.

On the other hand, XLogback provides more *JAVA-ish* way for logging. So XLogback provides more universal logging mechanism for any Java library you have developed, with the ability to move your code into totally different Java environment. Also appender support is significant. Different types of appenders can be defined in parallel to OpenLog. Check Logback web site for very interesting scenarios.

Here are a comparison between two projects:

| Feature |XPages OpenLog Logger| XLogback |
| ------- | :-------------------: | :------: |
| Runs from NSF alone (w/out plugin) | yes | no |
| OpenNTF Domino API support | yes | not tested |
| XPages App support | yes | In Progress |
| DOTS Support | not implemented yet | yes |
| Custom error pages | yes | In Progress |
| SSJS Logging | yes | In Progress |
| Additional Appenders | no | yes |
| Custom Configuration from Another Plugin | no | Partially |
| Log Level differentiation by class/package | no | yes |

# How to start?

## For XPages Developers

- XLogback ships with two update sites for Domino Server and Designer. Designer client includes source bundles.
- Install update sites into your Domino Server and Domino Designer.
  - You can follow [this](http://www-10.lotus.com/ldd/ddwiki.nsf/xpDocViewer.xsp?lookupName=Domino+Designer+XPages+Extension+Library#action=openDocument&res_title=Installing_the_OpenNTF_update_site_in_Domino_Designer_ddxl853&content=pdcontent) and [this](http://www-10.lotus.com/ldd/ddwiki.nsf/xpDocViewer.xsp?lookupName=Domino+Designer+XPages+Extension+Library#action=openDocument&res_title=Installing_the_OpenNTF_update_site_on_the_Domino_server_ddxl853&content=pdcontent) wiki pages to learn how to do it.
- Restart your server and Designer clients.
- Open your Notes application in Domino Designer and go to **Page Generation** section under **XSP.Properties**.
  - If the installation is successful, you should see "**org.openntf.base.logback.xsp.plugin.library**" in XPage Libraries section.
  - Click that.
- Now you can use logging from any Java class. Here is an example:

```java
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
```

## For Plugin Developers

You might either import `org.openntf.base.logback` project into your workplace or add the plugin into your Target platform.

Soon we will mavenize the project for easier installation into the Eclipse IDE.

# Configuration Options

XLogback has several different configuration modes:

## Automatic Configuration

If you don't do anything, XLogback initiates an automatic configuration itself. Automatic configuration starts with three appenders:

### Console Appender:

There are two different console appender in XLogback.

For plugins written for HTTP platform, `DefaultConsoleAppender` will use `System.out.println()`.

For plugins running on DOTS environment `System.out.println()` does not provide a consistent output for server console and `Log.nsf`. So `DOTSConsoleAppender` will use `ServerConsole` object coming with DOTS plugins.

Plugin picks the right appender in runtime.

### Rolling File Appender with HTML Layout

By default, XLogback creates an HTML log file under `IBM_TECHNICAL_SUPPORT\xlogback` directory. The log file will be rolled by size according to the configuration.

We could use the same file for HTTP and DOTS plugins at the same time. But there would be a minor performance cost using the same file for different VMs. Therefore the log file created in the HTTP will be named as `XSP.html`. For DOTS, the file name will be the same with mq name (e.g. DOTS by default). So multiple DOTS profiles are supported :)

### OpenLog Appender

XLogback will log into `OpenLog.nsf` file on the server data root. Of course that would be overridden.

### Automatic Configuration Options

XLogback uses several configuration parameters for auto-configuration. When started (the first logging attempt), it looks for several JVM settings and Notes.ini parameters to decide if autoconfiguration is enabled and other settings it needs for automatic configuration.

JVM settings always precede notes.ini parameters. This would be useful if you want to use separate settings for DOTS and XSP environments. In such a case JVM properties can be provided using a separate file and `DOTS_JavaOptionsFile` notes.ini parameter.

All XLogback parameters start with `Xlb_` prefix. Changing any parameters needs a platform restart (DOTS or HTTP).

| Parameter Name | Default Value | Notes |
| ------------- | ------------- | ----- |
| Xlb_Debug | 1 | 1: Debugging mode enabled. Will print everything about the Logback |
| Xlb_Auto | 1 | 1: Automatic Configuration is enabled |
| Xlb_ConsolePattern | *See logback-sample.xml* | Pattern to be used for Console appender|
| Xlb_ConsoleLogLevel | INFO | Minimum levels for Console events |
| Xlb_OpenLogDbServer | Empty | The server name of the OpenLog database |
| Xlb_OpenLogDbPath | OpenLog.nsf | The path name of the OpenLog database |
| Xlb_OpenLogSuppressEventStack | 1 | 1: Stack trace of given Throwable in event logging will be supressed |
| Xlb_OpenLogExpireDays | 0 | If a positive value given, OpenLog entries will be marked as expired after specified number of days. <br>OpenLog database needs to run a proper agent for this feature. |
| Xlb_OpenLogDebugLevel | 2 | 0: OpenLog internal errors will be discarded. <br>1: Exception messages from internal errors are printed. <br>2: Stack traces from internal errors are also printed |
| Xlb_OpenLogLogLevel | INFO | Minimum level for OpenLog events |
| Xlb_OpenLogDefaultApp | *Platform (DOTS, XSP, etc.)* | Default application value for OpenLog entries |
| Xlb_OpenLogDefaultAgent | *Empty* | Default agent value for OpenLog entries |
| Xlb_FilePath | [Log folder]\xlogback | Folder for file logging |
| Xlb_FileMaxIndex | 20 | Maximum number of rolling files |
| Xlb_FileMaxSize | 2MB | Maximum size for each log file |
| Xlb_FilePattern | *See logback-sample.xml* | Pattern for each row in the log file |
| Xlb_FileLogLevel | INFO | Minimum level for file events |


## Configuration File (XML or Groovy)

Logback uses XML configuration or Groovy scripts for configuration. Normally it looks for specific files in the classpath.

Remember to disable Automatic Configuration if you want to customize logging configuration.

XLogback ships with a sample XML file in the `src\main\resources` directory (*logback-sample.xml*). There are several options to help Logback to find it.

### Sample Logback.xml

```XML
<?xml version="1.0" encoding="UTF-8" ?>

<configuration debug="false">
	<appender
		name="console"
		class="org.openntf.base.logback.appender.DominoConsoleAppender">
		<layout class="ch.qos.logback.classic.PatternLayout">
			<pattern>%-5level %msg%n%ex{1}</pattern>
		</layout>
	</appender>

	<appender name="openlog" class="org.openntf.base.logback.appender.OpenLogAppender">
		<targetDbServer></targetDbServer>
		<targetDbPath>OpenLog.nsf</targetDbPath>

		<suppressEventStack>true</suppressEventStack>
		<logExpireDays>0</logExpireDays>
		<debugLevel>2</debugLevel>
	</appender>

	<define
		name="LOGPATH"
		class="org.openntf.base.logback.properties.LogPathProperty"></define>
	<define
		name="PLATFORM"
		class="org.openntf.base.logback.properties.PlatformProperty"></define>

	<appender name="rollingFile" class="ch.qos.logback.core.rolling.RollingFileAppender">
		<file>${LOGPATH}${PLATFORM}.html</file>

		<rollingPolicy class="ch.qos.logback.core.rolling.FixedWindowRollingPolicy">
			<fileNamePattern>${LOGPATH}${PLATFORM}.%i.html</fileNamePattern>
			<minIndex>1</minIndex>
			<maxIndex>20</maxIndex>
		</rollingPolicy>

		<triggeringPolicy class="ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy">
			<maxFileSize>2MB</maxFileSize>
		</triggeringPolicy>
		<encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
			<layout class="ch.qos.logback.classic.html.HTMLLayout">
				<pattern>%date{dd/MM;HH:mm:ss}%level%msg%mdc{app}%marker%logger{26}</pattern>
			</layout>
		</encoder>
	</appender>

	<root level="debug">
		<appender-ref ref="console" />
		<appender-ref ref="openlog" />
		<appender-ref ref="rollingFile" />
	</root>
</configuration>
```

### Provide a configuration file

You might provide logback.xml file using JVM properties. The easiest way to do that for Domino environments is using Java options file to be declared in notes.ini file. Here are steps to do that:

- Create your logback.xml file in your Domino server.
- Create a file for Java Options and add JVM properties;

```
-Dlogback.configurationFile=C:/path/to/config.xml
```

- Tell Domino to have that java options file for the proper environment.
  - For HTTP: `JavaOptionsFile=C:/path/to/options/file.txt`
  - For DOTS: `DOTS_JavaOptionsFile=C:/path/to/options/file.txt`

- Restart the server.

### Declare from your own plugin

This is a little bit trickier. Right now we don't support OSGi level seperation of logging context. So there has to be only one configuration file in the classpath. Otherwise it will throw some errors on the startup (it will work anyway but will select the first file it found inconsistently).

Therefore use this approach only if you are planning only one plugin providing configuration. For instance you may have a central configuration database where you want to get configuration files from an NSF database, or another repository.

The second trick is Eclipse-buddy mechanism. Normally OSGi uses separate classhpaths for each plugin. If you are providing logback.xml file from another plugin on your environment, add the following line into Manifest.mf file of your plugin, so OSGi will provides access for classpaths between both plugins.

```
Eclipse-RegisterBuddy: org.openntf.base.logback
```


## Custom Configuration

Logback supports JVM ServiceLoader to let you to implement your own configurator. However, it's something difficult to use from OSGi plugins.

In the upcoming versions, I'm planning to utilize extension points for this purpose.

# Under the hood:

## Logback Configuration in Deep

When `LoggerFactory.getLogger(...)` called for the first time, LoggerFactory initializes a binding and an initialization process. We can't intercept the initialization unless if we rewrite the Factory. Until then, the LoggerContext will start appropriate configurator to configure Logback.

Joran will try 3 ways:
1. Look for `logback.groovy` (Uses Gaffer).
2. Look for `logback-test.xml` / `logback.xml` files (Uses Joran).
3. Use ServiceLoader for Configurator declarations.
4. Use BasicConfigurator to set up simple console logging.

This process is not so plugin-friendly. We don't want to deal with file-based configuration in plugin environments. ServiceLoader is not an ideal way to use. So eventually we need to refactor this process and make it more effective on the Domino environment.

XLogback autoconfiguration starts with the plugin start. Since we don't have any information about what configuration has been used at that point, duplicate configuration is not a good idea. If you provide file-based or service-based configuration, you need to turn auto configuration off. I will fix this behaviour in future versions.

# How to Contribute

Submit your feature requests and bug reports into [XLogback Jira Project Page](https://jira.openntf.org/projects/XLB).

Let me know if you want to contribute in any way :)

# Known Issues

- In Designer, autocomplete is not working properly for SLF4J classes.
  - I don't know why ;-)
- When logging from XPages applications, OpenLog does not provide database and agent values yet.
  - This is easy but there are several different options. So I have to pick up the most effective method.
