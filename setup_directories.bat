@echo off
REM Create required directories
mkdir C:\temp\chrome-profiles 2>nul
mkdir logs 2>nul

echo Directories have been created:
echo - Chrome profiles: C:\temp\chrome-profiles
echo - Logs: %CD%\logs

pause
