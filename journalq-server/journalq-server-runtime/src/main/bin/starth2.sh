#!/bin/sh

BASEDIR=`dirname $0`/..
BASEDIR=`(cd "$BASEDIR"; pwd)`
CONFIG_FILE="$BASEDIR/conf/laf-jmq.properties"
JAVA_HOME=`grep -ios 'JAVA_HOME=.*$' ${CONFIG_FILE} | cut -d '=' -f 2`
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

CLASSPATH="$BASEDIR"/conf/:"$BASEDIR"/lib/*
echo "$CLASSPATH"

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi

nohup "$JAVACMD" -classpath "$CLASSPATH" -Dbasedir="$BASEDIR" -Dfile.encoding="UTF-8" org.h2.tools.Server -tcp -baseDir ./h2-db-jmq \
"$@" >/dev/null 2>/dev/null &
echo $! > h2.pid