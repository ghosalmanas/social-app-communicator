$ErrorActionPreference = "Stop"
$baseDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$logFile = "$baseDir/logs/application.log"
$maxIterations = 10
$appUrl = "http://localhost:8082"

function Write-Header {
    param($message)
    Write-Host "`n" + ("=" * 80) -ForegroundColor Cyan
    Write-Host "$message" -ForegroundColor Cyan
    Write-Host ("=" * 80) -ForegroundColor Cyan
}

function Stop-AppProcesses {
    Write-Header "Stopping any running Java processes..."
    try {
        Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -like '*WA_TG_FB_SeleniumFreelancerAppV2*' } | Stop-Process -Force
        Start-Sleep -Seconds 2
        Write-Host "Stopped Java processes" -ForegroundColor Green
    } catch {
        Write-Host "No Java processes found or error stopping them" -ForegroundColor Yellow
    }
}

function Close-Browsers {
    Write-Header "Closing all browser processes..."
    try {
        Get-Process chrome -ErrorAction SilentlyContinue | Stop-Process -Force
        Get-Process chromedriver -ErrorAction SilentlyContinue | Stop-Process -Force
        Start-Sleep -Seconds 1
        Write-Host "Closed all browser processes" -ForegroundColor Green
    } catch {
        Write-Host "Error closing browser processes: $_" -ForegroundColor Yellow
    }
}

function Build-App {
    Write-Header "Building application..."
    try {
        Set-Location $baseDir
        & mvn clean install -DskipTests
        if ($LASTEXITCODE -ne 0) {
            throw "Build failed with exit code $LASTEXITCODE"
        }
        Write-Host "Build completed successfully" -ForegroundColor Green
    } catch {
        Write-Host "Error during build: $_" -ForegroundColor Red
        throw
    }
}

function Start-App {
    param($iteration)
    $logPath = "$baseDir/logs/run_${iteration}.log"
    Write-Header "Starting application (Iteration $iteration)..."
    
    # Start process in background
    $process = Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -PassThru -NoNewWindow -RedirectStandardOutput $logPath -RedirectStandardError "$logPath.error"
    
    # Wait for app to start (check health endpoint)
    $maxAttempts = 30
    $attempt = 0
    $isReady = $false
    
    while ($attempt -lt $maxAttempts -and -not $isReady) {
        try {
            $response = Invoke-WebRequest -Uri "${appUrl}/actuator/health" -UseBasicParsing -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                $isReady = $true
                Write-Host "Application started successfully" -ForegroundColor Green
                break
            }
        } catch {
            # Ignore errors while waiting for startup
        }
        
        $attempt++
        Write-Host "Waiting for application to start (attempt $attempt/$maxAttempts)..."
        Start-Sleep -Seconds 2
    }
    
    if (-not $isReady) {
        throw "Application failed to start within the expected time"
    }
    
    return $process
}

function Invoke-TestRequests {
    Write-Header "Sending test requests..."
    try {
        # First request
        Write-Host "Sending first request to /social-app/tg/traverse..."
        $response1 = Invoke-WebRequest -Uri "${appUrl}/social-app/tg/traverse" -UseBasicParsing
        Write-Host "First request completed with status: $($response1.StatusCode)" -ForegroundColor Green
        
        # Second request
        Write-Host "Sending second request to /social-app/tg/traverse..."
        $response2 = Invoke-WebRequest -Uri "${appUrl}/social-app/tg/traverse" -UseBasicParsing
        Write-Host "Second request completed with status: $($response2.StatusCode)" -ForegroundColor Green
        
        # Wait for 10 seconds to let any background processing complete
        Write-Host "Waiting 10 seconds for background processing..."
        Start-Sleep -Seconds 10
        
    } catch {
        Write-Host "Error during test requests: $_" -ForegroundColor Red
        throw
    }
}

function Get-LogErrors {
    param($iteration)
    Write-Header "Checking logs for errors (Iteration $iteration)..."
    
    try {
        $logContent = Get-Content -Path $logFile -Tail 500 -ErrorAction Stop
        $errors = $logContent | Select-String -Pattern "ERROR|Exception|Caused by" -CaseSensitive
        
        if ($errors) {
            Write-Host "Found errors in logs:" -ForegroundColor Red
            $errors | ForEach-Object { Write-Host $_ -ForegroundColor Red }
            return $true
        } else {
            Write-Host "No errors found in logs" -ForegroundColor Green
            return $false
        }
    } catch {
        Write-Host "Error reading log file: $_" -ForegroundColor Yellow
        return $false
    }
}

# Main execution
Write-Header "Starting test cycle ($maxIterations iterations)"

for ($i = 1; $i -le $maxIterations; $i++) {
    Write-Header "Starting Iteration $i of $maxIterations"
    $process = $null
    
    try {
        # Step 1: Stop any running app
        Stop-AppProcesses
        
        # Step 2: Close all browsers
        Close-Browsers
        
        # Step 3: Build the app
        Build-App
        
        # Step 4: Start the app
        $process = Start-App -iteration $i
        
        # Step 5: Send test requests
        Invoke-TestRequests
        
        # Step 6: Check logs for errors
        $hasErrors = Get-LogErrors -iteration $i
        
        if ($hasErrors) {
            Write-Host "Errors found in logs. Stopping test cycle." -ForegroundColor Red
            break
        }
        
        Write-Host "Iteration $i completed successfully" -ForegroundColor Green
        
    } catch {
        Write-Host "Error in iteration $i : $_" -ForegroundColor Red
        Write-Host $_.ScriptStackTrace -ForegroundColor Red
        break
    } finally {
        # Always stop the app process if it was started
        if ($process -and -not $process.HasExited) {
            Stop-AppProcesses
        }
        
        # Close any remaining browser processes
        Close-Browsers
        
        # Add a small delay between iterations
        if ($i -lt $maxIterations) {
            Write-Host "Waiting 5 seconds before next iteration..."
            Start-Sleep -Seconds 5
        }
    }
}

Write-Header "Test cycle completed"
