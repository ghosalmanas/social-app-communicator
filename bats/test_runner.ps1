$ErrorActionPreference = "Stop"
$testUrl = "http://localhost:8082/social-app/tg/traverse"
$maxRetries = 10
$waitSeconds = 10

function Start-Application {
    Write-Host "Building application..."
    & mvn clean package -DskipTests
    
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to build application"
    }
    
    $jarFile = "target\WA_TG_FB_SeleniumFreelancerAppV2-0.0.1-SNAPSHOT.jar"
    if (-not (Test-Path $jarFile)) {
        throw "JAR file not found: $jarFile"
    }
    
    Write-Host "Starting application..."
    $process = Start-Process -FilePath "java" -ArgumentList "-jar", "`"$jarFile`"", "--spring.profiles.active=dev" -PassThru -NoNewWindow
    
    # Wait for application to start
    $started = $false
    for ($i = 0; $i -lt 30; $i++) {
        try {
            $response = Invoke-WebRequest -Uri $testUrl -Method Head -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                $started = $true
                break
            }
        } catch {
            Start-Sleep -Seconds 1
        }
    }
    
    if (-not $started) {
        $process | Stop-Process -Force -ErrorAction SilentlyContinue
        throw "Application failed to start"
    }
    
    return $process
}

function Stop-Application {
    param($process)
    if ($process -ne $null) {
        $process | Stop-Process -Force -ErrorAction SilentlyContinue
    }
}

try {
    for ($i = 1; $i -le $maxRetries; $i++) {
        Write-Host "`n===== Starting Test Run $i of $maxRetries ====="
        
        # Stop any existing Java processes
        Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
        
        $process = $null
        try {
            $process = Start-Application
            
            Write-Host "Waiting $waitSeconds seconds before test..."
            Start-Sleep -Seconds $waitSeconds
            
            Write-Host "Sending request to $testUrl"
            $response = Invoke-WebRequest -Uri $testUrl -UseBasicParsing
            Write-Host "Response ($($response.StatusCode)): $($response.Content)"
            
            Write-Host "Waiting $waitSeconds seconds before cleanup..."
            Start-Sleep -Seconds $waitSeconds
            
        } catch {
            Write-Host "Error: $_" -ForegroundColor Red
            if (Test-Path "app.log") {
                Write-Host "Application log:"
                Get-Content "app.log" -ErrorAction SilentlyContinue
            }
        } finally {
            Stop-Application -process $process
        }
    }
} catch {
    Write-Host "Fatal error: $_" -ForegroundColor Red
} finally {
    Write-Host "`n===== Test completed ====="
    Pause
}
