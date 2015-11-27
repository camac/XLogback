## Configuration Options

Logback has two configuration modes:

### Configuration File (XML or Groovy)

Logback uses XML configuration or Groovy scripts for configuration. Normally it looks for specific files in the classpath.

***TODO: Logback site link***

XLogback ships with a sample XML file in the root folder (*logback-sample.xml*). There are several options to help Logback to find it.

#### Sample Logback.xml

```XML
<?xml version="1.0" encoding="UTF-8" ?>

<configuration debug="true">
<!--   <contextName>DOMINO</contextName> -->

  <appender name="console" class="org.openntf.base.logback.appender.DominoConsoleAppender">
    <layout class="ch.qos.logback.classic.PatternLayout">
  	<pattern>%-5level %msg%n%ex{1})</pattern>
    </layout>
  </appender>

  <appender name="openlog" class="org.openntf.base.logback.appender.OpenLogAppender">
  	<targetDbServer></targetDbServer>
	<targetDbPath>OpenLog.nsf</targetDbPath>

  	<suppressEventStack>true</suppressEventStack>
	<logExpireDays>0</logExpireDays>
	<debugLevel>2</debugLevel>
  </appender>

  <root level="debug">
    <appender-ref ref="console" />
    <appender-ref ref="openlog" />
  </root>
</configuration>
```

#### Provide a configuration file

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

#### Declare from your own plugin

This is a little bit trickier. Right now we don't support OSGi level seperation of logging context. So there has to be only one configuration file in the classpath. Otherwise it will throw some errors on the startup (it will work anyway but will select the first file it found inconsistently).

Therefore use this approach only if you are planning only one plugin providing configuration. For instance you may have a central configuration database where you want to get configuration files from an NSF database, or another repository.

The second trick is Eclipse-buddy mechanism. Normally OSGi uses separate classhpaths for each plugin. If you are providing logback.xml file from another plugin on your environment, add the following line into Manifest.mf file of your plugin, so OSGi will provides access for classpaths between both plugins.

```
Eclipse-RegisterBuddy: org.openntf.base.logback
```

#### XLogback Settings

XLogback uses several configuration parameters for auto-configuration. When started (the first logging attempt), it looks for several JVM settings and Notes.ini parameters to decide if autoconfiguration is enabled and other settings it needs for automatic configuration.

JVM settings always precede notes.ini parameters. This would be useful if you want to use separate settings for DOTS and XSP environments. In such a case JVM properties can be provided using a separate file and `DOTS_JavaOptionsFile` notes.ini parameter.

All XLogback parameters start with `Xlb_` prefix. Changing any parameters needs a platform restart (DOTS or HTTP).

| Parameter Name | Default Value | Notes |
| ------------- | ------------- | ----- |
| Xlb_Debug | 1 | 1: Debugging mode enabled. Will print everything about the config process |
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



***TODO Add links from logback***

### Custom Configuration


## Under the hood:

### Logback Configuration in Deep

When `LoggerFactory.getLogger(...)` called for the first time, LoggerFactory initializes a binding and an initialization process. We can't intercept the initialization unless if we rewrite the Factory. Until then, the LoggerContext will start appropriate configurator to configure Logback.

Joran will try 3 ways:
1. Look for `logback.groovy` (Uses Gaffer).
2. Look for `logback-test.xml` / `logback.xml` files (Uses Joran).
3. Use ServiceLoader for Configurator declarations.
4. Use BasicConfigurator to set up simple console logging.

This process is not so plugin-friendly. We don't want to deal with file-based configuration in plugin environments. ServiceLoader is not an ideal way to use. So eventually we need to refactor this process and make it more effective on the Domino environment.

XLogback autoconfiguration starts with the plugin start. Since we don't have any information about what configuration has been used at that point, duplicate configuration is not a good idea. If you provide file-based or service-based configuration, you need to turn auto configuration off. I will fix this behaviour in future versions.


## How to Contribute



## TODO

- Create a XPages encapsulation
  - XPages apps should be able to provide custom logging.
  - Designer plugin for custom logging configuration.
- Test Logback plugin for Liberty profile (OSGiWorlds)
- Change the LoggerFactory initialization
  - Prevent messages about multiple binder.
  - Prevent Classpath search for binder
  - Prevent initialization of Joran. We should do it in the plugin initiation
- Test Groovy configuration in XSP and DOTS environments
- Change Logback Autoconfiguration
  - Stop listing configuration files
  - Provide extension point for custom configuration
- AutoConfig should not fail in case of appender failures.



## Credits
