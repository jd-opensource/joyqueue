
setlocal enabledelayedexpansion

PUSHD %~dp0..\..
SET "APP_HOME=%CD%"
POPD

IF ["%JAVA_HOME%"] EQU [""] (
	SET JAVA_CMD="java"
) ELSE (
	SET JAVA_CMD="%JAVA_HOME%\bin\java"
)

SET "JAVA_OPTS=-server -Xms1024m -Xmx1024m -Xmn256m -Xss256k"
SET LIBPATH="%APP_HOME%\lib\*"
SET CLASSPATH="%APP_HOME%\conf"
SET "LIB_CLASSPATH=%CLASSPATH%;%LIBPATH%"

SET run=%JAVA_CMD% %JAVA_OPTS% -classpath %LIB_CLASSPATH% -Dfile.encoding=UTF-8 %*

%run%