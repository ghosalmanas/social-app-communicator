@echo off
setlocal enabledelayedexpansion

:: Configuration
set SPRING_PROFILES_ACTIVE=local
set SERVER_PORT=8082
set LOG_FILE=build.log
set APP_JAR=target/WA_TG_FB_SeleniumFreelancerAppV2-0.0.1-SNAPSHOT.jar

:: JVM Memory Settings
set JAVA_OPTS=-Xms2g -Xmx6g -XX:MaxMetaspaceSize=1g -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./heapdump.hprof

:: Initialize log file
echo =========================================== > %LOG_FILE%
echo Build and Run Script Started: %DATE% %TIME% >> %LOG_FILE%
echo =========================================== >> %LOG_FILE%

echo [%DATE% %TIME%] Stopping any running Java processes... >> %LOG_FILE%
taskkill /F /IM java.exe >nul 2>&1

:: Build the project
echo [%DATE% %TIME%] Cleaning and building the project... >> %LOG_FILE%
call mvn clean install -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo [%DATE% %TIME%] ERROR: Build failed with code %ERRORLEVEL% >> %LOG_FILE%
    exit /b %ERRORLEVEL%
)

echo [%DATE% %TIME%] Build completed successfully >> %LOG_FILE%

:: Start the application
echo [%DATE% %TIME%] Starting application with JVM options: %JAVA_OPTS% >> %LOG_FILE%

java %JAVA_OPTS% ^
     -Dspring.profiles.active=%SPRING_PROFILES_ACTIVE% ^
     -Dserver.port=%SERVER_PORT% ^
     -Dlogging.file.name=application.log ^
     -jar %APP_JAR%

if %ERRORLEVEL% NEQ 0 (
    echo [%DATE% %TIME%] ERROR: Application failed to start with code %ERRORLEVEL% >> %LOG_FILE%
    exit /b %ERRORLEVEL%
)

echo [%DATE% %TIME%] Application started successfully on port %SERVER_PORT% >> %LOG_FILE%
echo Application started successfully. Check logs at %CD%\application.log

exit /b 0
