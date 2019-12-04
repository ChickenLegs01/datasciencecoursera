#!/bin/bash

JAVA_HOME=/mnt/d/Java/jdk1.6.0_24_32bit
CLASSPATH=./ScoreboardEmulator.jar:./log4j.properties
LIB=./lib
JAVA_OPTS="-Xmx1024m"

## Project libs
for i in $LIB/*.jar; do
CLASSPATH="${CLASSPATH}:$i";
done

echo "javahome $JAVA_HOME"

echo "$JAVA_HOME/bin/java.exe" "$JAVA_OPTS" -cp "$CLASSPATH" nl.salland.scoreboard.ScoreBoardEmulator
"$JAVA_HOME/bin/java.exe" "$JAVA_OPTS" -cp "$CLASSPATH" nl.salland.scoreboard.ScoreBoardEmulator
exit 0 