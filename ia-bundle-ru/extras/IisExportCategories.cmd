@echo off

call ../dcs-config.cmd

cmd /c %IISROOT%\%ISTOOL%  glossary export -dom %IISDOM% -u %IAUSER% -p %IAPASS% ^
  -filename %CD%\categories-export.xml -allcategories
