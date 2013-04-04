::BATCH file for windows
ECHO will run the client

set BATDIR=%~dp0
set LIBDIR=%BATDIR%..\..\..\build\libs

java  -Dlogback.configurationFile=logback.xml -Djava.ext.dirs=%LIBDIR% org.openiec61850.sample.SampleClient 127.0.0.1 10002
