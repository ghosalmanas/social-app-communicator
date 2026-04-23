@echo off
set SPRING_PROFILES_ACTIVE=local
set SERVER_PORT=8083

set JAVA_OPTS=-Xms2g -Xmx6g -XX:MaxMetaspaceSize=1g -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./heapdump.hprof
java %JAVA_OPTS% -jar target/WA_TG_FB_SeleniumFreelancerAppV2-0.0.1-SNAPSHOT.jar
