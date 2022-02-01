@echo off

call dcs-config.cmd

@echo on

copy /Y %JARSRC% %IISROOT%\%IISJAR1%\%JARDST%
copy /Y %JARSRC% %IISROOT%\%IISJAR2%\%JARDST%

cmd /c %IISROOT%\%IAADMIN% -user %IAUSER% -password %IAPASS% -url %IISURL% -updateDataClasses %JARNAME%

cmd /c %IISROOT%\%IAADMIN% -installClassifiers %JARNAME%

pause
