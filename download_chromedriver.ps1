# PowerShell script to download and setup ChromeDriver
$chromeVersion = "138.0.7204.183"
$chromeDriverVersion = "138.0.7204.183"
$url = "https://chromedriver.storage.googleapis.com/$chromeDriverVersion/chromedriver_win32.zip"
$zipFile = "$PSScriptRoot\chromedriver-win64.zip"
$extractPath = "$PSScriptRoot"
$chromedriverExe = "$PSScriptRoot\chromedriver.exe"

# Download ChromeDriver
Write-Host "Downloading ChromeDriver $chromeDriverVersion..."
try {
    # Remove existing files if they exist
    if (Test-Path $zipFile) { Remove-Item $zipFile }
    if (Test-Path $chromedriverExe) { Remove-Item $chromedriverExe }

    # Download
    Invoke-WebRequest -Uri $url -OutFile $zipFile

    # Extract
    Expand-Archive -Path $zipFile -DestinationPath $extractPath -Force

    # Clean up
    Remove-Item $zipFile

    Write-Host "ChromeDriver $chromeDriverVersion has been downloaded and extracted successfully."
    Write-Host "Location: $chromedriverExe"
}
catch {
    Write-Error "Failed to download ChromeDriver: $_"
    exit 1
}