@echo off
setlocal enabledelayedexpansion

:: Set Java Home - update this path if your Java installation is different
set JAVA_HOME="C:\\Program Files\\Java\\jdk-21"
set PATH=!JAVA_HOME!\\bin;%PATH%

:: Change to script directory
cd /d "%~dp0"

:: Compile source files if needed
if not exist "target\\classes" (
    echo Compiling source files...
    mvn compile -DskipTests
    if errorlevel 1 (
        echo Compilation failed
        exit /b 1
    )
)

:: Build classpath
set CLASSPATH=target\\classes

:: Add all JARs from lib directory
if exist "lib\\*.jar" (
    for %%i in (lib\\*.jar) do (
        set CLASSPATH=!CLASSPATH!;%%~fi
    )
)

:: Add Maven dependencies if they exist
if exist "target\\dependency\\*.jar" (
    for %%i in (target\\dependency\\*.jar) do (
        set CLASSPATH=!CLASSPATH!;%%~fi
    )
)

echo Starting application...
echo Classpath: !CLASSPATH!

java -cp "!CLASSPATH!" com.wtf.app.WaTgFbSeleniumFreelancerAppV2Application

endlocal
