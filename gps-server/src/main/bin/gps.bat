@echo off

if "%JAVA_OPTS%" == "" goto DEFAULT_JAVA_OPTS

:INVOKE
echo JAVA_HOME=%JAVA_HOME%
echo JAVA_OPTS=%JAVA_OPTS%
echo GPS_HOME=%GPS_HOME%

if "%GPS_HOME%" == "" goto GPS_HOME_NOT_SET

setlocal ENABLEDELAYEDEXPANSION

set GPS_DIST=%~dp0..
echo GPS_DIST=%GPS_DIST%

rem Java 6 supports wildcard classpaths
rem http://download.oracle.com/javase/6/docs/technotes/tools/windows/classpath.html
set CLASSPATH=%GPS_HOME%\conf;%GPS_DIST%\lib\*

set JAVA_DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n

IF NOT EXIST "%GPS_HOME%\logs" mkdir "%GPS_HOME%\logs"
  rem Add %JAVA_DEBUG% to this line to enable remote JVM debugging (for developers)
  java %JAVA_OPTS% -cp "%CLASSPATH%" -DGPS_HOME="%GPS_HOME%" -DGPS_DIST=%GPS_DIST% ca.on.oicr.gps.server.GPSJettyServer %*
goto :END

:DEFAULT_JAVA_OPTS
set JAVA_OPTS=-Xmx2G -XX:MaxPermSize=128M
goto :INVOKE

:JAVA_HOME_NOT_SET
echo JAVA_HOME not set
goto :END

:GPS_HOME_NOT_SET
echo GPS_HOME not set
goto :END

:END
