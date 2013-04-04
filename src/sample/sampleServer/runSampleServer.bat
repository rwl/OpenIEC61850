::BATCH file to windows
ECHO will run the server

set BATDIR=%~dp0
set LIBDIR=%BATDIR%..\..\..\build\libs

java -Dlogback.configurationFile=logback.xml -Djava.ext.dirs=%LIBDIR% org.openiec61850.StandaloneServer sampleServer.properties
