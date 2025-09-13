@echo off
set SPRING_PROFILES_ACTIVE=local
set SERVER_PORT=8083

java -Dlogging.level.org.springframework=DEBUG -Dlogging.level.com.wtf.app=TRACE -jar target/WA_TG_FB_SeleniumFreelancerAppV2-0.0.1-SNAPSHOT.jar --debug
