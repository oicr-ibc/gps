@echo off

if "%JAVA_OPTS%" == "" goto DEFAULT_JAVA_OPTS

:INVOKE
echo JAVA_HOME=%JAVA_HOME%
echo JAVA_OPTS=%JAVA_OPTS%
echo HELIOTROPE_HOME=%HELIOTROPE_HOME%

if "%HELIOTROPE_HOME%" == "" goto HELIOTROPE_HOME_NOT_SET

setlocal ENABLEDELAYEDEXPANSION

set HELIOTROPE_DIST=%~dp0..
echo HELIOTROPE_DIST=%HELIOTROPE_DIST%

rem Java 6 supports wildcard classpaths
rem http://download.oracle.com/javase/6/docs/technotes/tools/windows/classpath.html
set CLASSPATH=%HELIOTROPE_HOME%\conf;%HELIOTROPE_DIST%\lib\*

set JAVA_DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n

IF NOT EXIST "%HELIOTROPE_HOME%\logs" mkdir "%HELIOTROPE_HOME%\logs"
  rem Add %JAVA_DEBUG% to this line to enable remote JVM debugging (for developers)
  java %JAVA_OPTS% -cp "%CLASSPATH%" -DHELIOTROPE_HOME="%HELIOTROPE_HOME%" -DHELIOTROPE_DIST=%HELIOTROPE_DIST% org.obiba.heliotrope.server.HeliotropeServer %*
goto :END

:DEFAULT_JAVA_OPTS
set JAVA_OPTS=-Xmx2G -XX:MaxPermSize=128M
goto :INVOKE

:JAVA_HOME_NOT_SET
echo JAVA_HOME not set
goto :END

:HELIOTROPE_HOME_NOT_SET
echo HELIOTROPE_HOME not set
goto :END

:END
