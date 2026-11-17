@echo off
setlocal

set "ROOT=%~dp0.."

call "%ROOT%\gradlew.bat" -p "%ROOT%" runCli -x test --quiet --console=plain --args="%*"

exit /b %ERRORLEVEL%