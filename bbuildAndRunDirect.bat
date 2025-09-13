call taskkill /F /IM java.exe >nul 2>&1
call taskkill /F /IM chrome.exe /T
call taskkill /F /IM chromedriver.exe /T
call taskkill /F /IM Rambox.exe /T
start "SunAweray" /min "C:\Program Files\Aweray\AweSun\AweSun.exe"
rem ghosalmanas80@gmail.com
timeout /t 10

call mvn clean package -DskipTests;
call cls;
rem pause
set JAVA_OPTS=-Xms2g -Xmx6g -XX:MaxMetaspaceSize=1g -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./heapdump.hprof -XX:+AlwaysPreTouch -XX:+ParallelRefProcEnabled -XX:+DisableExplicitGC -XX:+UseCompressedOops -XX:+UseCompressedClassPointers
rem start java %JAVA_OPTS% -jar target/WA_TG_FB_SeleniumFreelancerAppV2-0.0.1-SNAPSHOT.jar
start java %JAVA_OPTS% -jar target/WA_TG_FB_SeleniumFreelancerAppV2-0.0.1-SNAPSHOT.jar
timeout /T 5 >nul
timeout /T 20 /NOBREAK >nul
rem start curl -X POST http://localhost:8082/api/automation/start
start "cURL Command" /min curl -X POST http://localhost:8082/api/automation/start
pause