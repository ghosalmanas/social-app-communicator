@echo off
echo Closing all Chrome browser and ChromeDriver instances...
taskkill /F /IM chrome.exe /T
taskkill /F /IM chromedriver.exe /T
echo All Chrome and ChromeDriver processes have been terminated.
pause