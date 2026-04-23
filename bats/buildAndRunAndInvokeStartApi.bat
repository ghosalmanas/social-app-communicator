@echo off

:: Kill any existing Java processes
taskkill /F /IM java.exe >nul 2>&1

:: Build the project
call mvn clean install -DskipTests

:: Set optimized JVM options
echo "Setting up JVM options..."
set JAVA_OPTS=-Xms2048m -Xmx6144m -XX:MaxMetaspaceSize=1024m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./heapdump.hprof

:: Start the application
echo "Starting application..."
start "" java %JAVA_OPTS% -jar target/WA_TG_FB_SeleniumFreelancerAppV2-0.0.1-SNAPSHOT.jar --spring.profiles.active=local --server.port=8082

:: Wait for the application to initialize
echo "Waiting for application to start..."
timeout /T 15 /NOBREAK >nul

:: Trigger automation
echo "Invoking health and start endpoints..."
curl -X GET http://localhost:8082/health >nul 2>&1
curl -X POST http://localhost:8082/api/automation/start >nul 2>&1

pause