@echo off
echo Stopping any running Java processes...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr LISTENING ^| findstr :8081') do (
    echo Killing process with PID: %%a
    taskkill /F /PID %%a
)

echo Starting the application...
cd /d %~dp0
call mvn spring-boot:run

pause
