@echo off
setlocal enabledelayedexpansion

:: Configuration
set SPRING_PROFILES_ACTIVE=local
set SERVER_PORT=8083
set LOG_FILE=application.log

:: JVM Memory Settings
set JAVA_OPTS=-Xms2g -Xmx6g -XX:MaxMetaspaceSize=1g -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./heapdump.hprof

:: Start application
echo [%DATE% %TIME%] Starting application with JVM options: %JAVA_OPTS% >> %LOG_FILE%

java %JAVA_OPTS% ^
     -Dspring.profiles.active=%SPRING_PROFILES_ACTIVE% ^
     -Dserver.port=%SERVER_PORT% ^
     -Dlogging.file.name=%LOG_FILE% ^
     -cp "target/WA_TG_FB_SeleniumFreelancerAppV2-0.0.1-SNAPSHOT.jar;target/dependency/*" ^
     com.wtf.app.WaTgFbSeleniumFreelancerAppV2Application

if %ERRORLEVEL% NEQ 0 (
    echo [%DATE% %TIME%] Application failed to start with error code %ERRORLEVEL% >> %LOG_FILE%
    exit /b %ERRORLEVEL%
)

echo [%DATE% %TIME%] Application started successfully on port %SERVER_PORT% >> %LOG_FILE%
