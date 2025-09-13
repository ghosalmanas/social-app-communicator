@echo off
setlocal enabledelayedexpansion

set "APP_PID="
set "TEST_URL=http://localhost:8082/social-app/tg/traverse"
set "MAX_RETRIES=10"
set "WAIT_TIME=10"

:start_test
set /a "count+=1"
echo.
echo ===== Starting Test Run !count! of %MAX_RETRIES% =====
echo.

:: Kill any existing Java process
taskkill /F /IM java.exe /T >nul 2>&1

echo Building application...
call mvn clean package -DskipTests >nul 2>&1

if %ERRORLEVEL% NEQ 0 (
    echo Failed to build application
    exit /b 1
)

set "JAR_FILE=target\WA_TG_FB_SeleniumFreelancerAppV2-0.0.1-SNAPSHOT.jar"

if not exist "%JAR_FILE%" (
    echo Error: JAR file not found: %JAR_FILE%
    exit /b 1
)

echo Starting application...
set JAVA_OPTS=-Xms2g -Xmx6g -XX:MaxMetaspaceSize=1g -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./heapdump.hprof
start "" /B /MIN cmd /c "java %JAVA_OPTS% -jar "%JAR_FILE%" --spring.profiles.active=dev > app.log 2>&1"

:: Wait for application to start
echo Waiting for application to start...
timeout /t 30 /nobreak >nul

:: Get the process ID
set "APP_PID="
for /f "tokens=1,2" %%a in ('jps -l') do (
    if "%%b"=="%JAR_FILE%" set "APP_PID=%%a"
)

if "%APP_PID%"=="" (
    echo Error: Could not find running application
    if exist app.log type app.log
    exit /b 1
)

if "!APP_PID!"=="" (
    echo Error: Application failed to start
    type app.log
    goto :cleanup
)

echo Application started with PID: !APP_PID!
echo Waiting %WAIT_TIME% seconds before test...
timeout /t %WAIT_TIME% /nobreak >nul

echo Sending request to %TEST_URL%
curl -s -o response.txt %TEST_URL%

echo Response:
type response.txt
echo.

echo Waiting %WAIT_TIME% seconds before next test...
timeout /t %WAIT_TIME% /nobreak >nul

:cleanup
echo Cleaning up...
if defined APP_PID (
    taskkill /F /PID !APP_PID! >nul 2>&1
    set "APP_PID="
)

timeout /t 5 /nobreak >nul

if !count! lss %MAX_RETRIES% (
    goto :start_test
) else (
    echo.
    echo ===== All test runs completed =====
    echo.
)

pause
