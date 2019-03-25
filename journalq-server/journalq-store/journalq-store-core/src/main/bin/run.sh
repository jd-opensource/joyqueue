#!/bin/sh
BASEDIR=`dirname $0`/..
BASEDIR=`(cd "$BASEDIR"; pwd)`
ROOTDIR=`dirname $BASEDIR`/..

APP_NAME="jmq_store"
MAIN_CLASS="com.jd.jmq.store.cli.LocalFakeBroker"

APP_HOME="$( cd "$(dirname "$0")" ; cd .. ; pwd -P )"

PIDPROC=`ps -ef | grep "$APP_NAME" | grep -v 'grep'| awk '{print $2}'`
 if [ -z "$PIDPROC" ];then
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

  if [ -z "$OPTS_MEMORY" ] ; then
      OPTS_MEMORY="-Xms2G -Xmx2G  -server -Xss256K"
  fi
  CLASSPATH="$APP_HOME/conf/:$APP_HOME/lib/*"
  "$JAVACMD" $JAVA_OPTS \
    $OPTS_MEMORY \
    -XX:MaxDirectMemorySize=2g \
    -classpath "$CLASSPATH" \
    -Dbasedir="$BASEDIR" \
    -Dfile.encoding="UTF-8" \
    -Dapp.name="$APP_NAME" \
    ${MAIN_CLASS}
 else
  echo "$ROOTDIR(pid:$PIDPROC) is running"
fi