# Update Chrome Browser
Write-Host "Updating Chrome browser to latest stable version..."
$chromeUpdateUrl = "https://dl.google.com/chrome/install/chrome_installer.exe"
$installerPath = "$env:TEMP\chrome_installer.exe"

# Download Chrome installer
Invoke-WebRequest -Uri $chromeUpdateUrl -OutFile $installerPath

# Install/Update Chrome
Start-Process -FilePath $installerPath -Args "/silent /install" -Wait

# Update ChromeDriver using WebDriverManager
Write-Host "Updating ChromeDriver to match Chrome version..."
$webDriverManagerPath = "D:\.m2\repository\io\github\bonigarcia\webdrivermanager\5.8.0\webdrivermanager-5.8.0.jar"

# If WebDriverManager JAR exists, use it to update ChromeDriver
if (Test-Path $webDriverManagerPath) {
    java -jar $webDriverManagerPath update
} else {
    Write-Host "WebDriverManager not found at $webDriverManagerPath"
    Write-Host "Please run 'mvn clean install' to download dependencies"
}

# Verify versions
Write-Host "`nCurrent versions:"
$chromeVersion = (Get-ItemProperty "HKCU:\Software\Google\Chrome\BLBeacon").version
Write-Host "Chrome version: $chromeVersion"

try {
    $driverVersion = & chromedriver --version
    Write-Host "ChromeDriver version: $driverVersion"
} catch {
    Write-Host "ChromeDriver not found in PATH"
}

Write-Host "`nUpdate complete!"
