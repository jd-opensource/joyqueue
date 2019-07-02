@echo off
rem Licensed to the Apache Software Foundation (ASF) under one or more
rem contributor license agreements.  See the NOTICE file distributed with
rem this work for additional information regarding copyright ownership.
rem The ASF licenses this file to You under the Apache License, Version 2.0
rem (the "License"); you may not use this file except in compliance with
rem the License.  You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

setlocal enabledelayedexpansion

SET APP_HOME=%~dp0

IF ["%JAVA_HOME%"] EQU [""] (
	set JAVA_CMD="java"
) ELSE (
	set JAVA_CMD="%JAVA_HOME%/bin/java"
)

SET CLASSPATH=%APP_HOME%/conf/:%APP_HOME%/lib/*
SET run=%JAVA_CMD% %JAVA_OPTS% -classpath %CLASSPATH% -Dfile.encoding=UTF-8 %*

%run%