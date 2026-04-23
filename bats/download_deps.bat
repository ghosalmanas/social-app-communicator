@echo off
setlocal

set MAVEN_REPO=%USERPROFILE%\.m2\repository

if not exist "lib" mkdir lib

:: Copy Log4j2 dependencies
copy "%MAVEN_REPO%\org\apache\logging\log4j\log4j-api\2.20.0\log4j-api-2.20.0.jar" lib\
copy "%MAVEN_REPO%\org\apache\logging\log4j\log4j-core\2.20.0\log4j-core-2.20.0.jar" lib\
copy "%MAVEN_REPO%\org\apache\logging\log4j\log4j-slf4j-impl\2.20.0\log4j-slf4j-impl-2.20.0.jar" lib\
copy "%MAVEN_REPO%\org\slf4j\slf4j-api\2.0.7\slf4j-api-2.0.7.jar" lib\

echo Dependencies downloaded to lib directory

endlocal
