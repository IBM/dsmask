@echo off

call dcs-config.cmd

cmd /c %IISROOT%\%ISTOOL%  glossary import -dom %IISDOM% -u %IAUSER% -p %IAPASS% ^
  -filename %CD%\extras\categories.xml -mergemethod ignore
