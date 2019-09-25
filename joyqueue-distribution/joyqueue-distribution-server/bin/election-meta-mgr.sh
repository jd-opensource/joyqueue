#!/bin/sh
#
# Copyright 2019 The JoyQueue Authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


MAIN_CLASS="MetadataManager"

APP_HOME="$( cd "$(dirname "$0")" ; cd .. ; pwd -P )"

   # If a specific java binary isn't specified search for the standard 'java' binary
  if [ -z "$JAVACMD" ] ; then
    if [ -n "$JAVA_HOME"  ] ; then
      if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
      else
        JAVACMD="$JAVA_HOME/bin/java"
      fi
    else
      JAVACMD=`which java`
    fi
  fi


  if [ ! -x "$JAVACMD" ] ; then
    echo "Error: JAVA_HOME is not defined correctly."
    echo "  We cannot execute $JAVACMD"
    exit 1
  fi

  CLASSPATH="$APP_HOME/conf/:$APP_HOME/lib/*"
  "$JAVACMD" $JAVA_OPTS \
    -classpath "$CLASSPATH" \
    -Dfile.encoding="UTF-8" \
    ${MAIN_CLASS} $@

