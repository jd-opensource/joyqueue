@REM
@REM Copyright 2019 The JoyQueue Authors.
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM     http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM


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