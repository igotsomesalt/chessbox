@echo off
setlocal

set "ROOT=%~dp0.."
set "JAR=%ROOT%\app\build\libs\app-0.1.0-all.jar"

java -jar "%JAR%" %*

exit /b %ERRORLEVEL%