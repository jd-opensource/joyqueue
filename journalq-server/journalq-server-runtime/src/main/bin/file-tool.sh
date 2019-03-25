#!/bin/sh

MAIN_CLASS="com.jd.jmq.store.cli.FileTool"

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

