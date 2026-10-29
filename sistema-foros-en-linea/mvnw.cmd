@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0herramientas\ejecutar-maven.ps1" %*
exit /b %ERRORLEVEL%
