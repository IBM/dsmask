@echo off

call dcs-config.cmd

@echo on

cmd /c %IISROOT%\%IAADMIN% -user %IAUSER% -password %IAPASS% -url %IISURL% -undeployDataClasses %JARNAME%

pause
