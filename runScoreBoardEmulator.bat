
set JAVA_HOME=D:\Java\jdk1.6.0_24_32bit
set CLASSPATH=ScoreboardEmulator.jar;log4j.properties;lib\comm-2.0.jar;lib\log4j-1.2.17.jar;lib\slf4j-api-1.7.5.jar;lib\slf4j-log4j12-1.7.5.jar
set JAVA_OPTS="-Xmx1024m"

echo "javahome %JAVA_HOME%"

echo "%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% -cp "%CLASSPATH%" nl.salland.scoreboard.ScoreBoardEmulator
"%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% -cp "%CLASSPATH%" nl.salland.scoreboard.ScoreBoardEmulator %*
