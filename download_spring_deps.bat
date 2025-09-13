@echo off
setlocal

set MAVEN_REPO=%USERPROFILE%\.m2\repository

if not exist "lib" mkdir lib

:: Spring Boot Starter Dependencies
copy "%MAVEN_REPO%\org\springframework\boot\spring-boot\3.2.10\spring-boot-3.2.10.jar" lib\
copy "%MAVEN_REPO%\org\springframework\boot\spring-boot-autoconfigure\3.2.10\spring-boot-autoconfigure-3.2.10.jar" lib\
copy "%MAVEN_REPO%\org\springframework\boot\spring-boot-starter\3.2.10\spring-boot-starter-3.2.10.jar" lib\ncopy "%MAVEN_REPO%\org\springframework\boot\spring-boot-starter-logging\3.2.10\spring-boot-starter-logging-3.2.10.jar" lib\
copy "%MAVEN_REPO%\org\springframework\boot\spring-boot-starter-web\3.2.10\spring-boot-starter-web-3.2.10.jar" lib\

:: Spring Core Dependencies
copy "%MAVEN_REPO%\org\springframework\spring-core\6.1.11\spring-core-6.1.11.jar" lib\
copy "%MAVEN_REPO%\org\springframework\spring-context\6.1.11\spring-context-6.1.11.jar" lib\
copy "%MAVEN_REPO%\org\springframework\spring-beans\6.1.11\spring-beans-6.1.11.jar" lib\
copy "%MAVEN_REPO%\org\springframework\spring-web\6.1.11\spring-web-6.1.11.jar" lib\
copy "%MAVEN_REPO%\org\springframework\spring-webmvc\6.1.11\spring-webmvc-6.1.11.jar" lib\

:: Logging Dependencies
copy "%MAVEN_REPO%\org\apache\logging\log4j\log4j-core\2.20.0\log4j-core-2.20.0.jar" lib\
copy "%MAVEN_REPO%\org\apache\logging\log4j\log4j-slf4j-impl\2.20.0\log4j-slf4j-impl-2.20.0.jar" lib\n
echo Spring Boot dependencies downloaded to lib directory

endlocal
