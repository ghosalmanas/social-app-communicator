# Test cycle script for WA_TG_FB_SeleniumFreelancerAppV2
$maxRetries = 5
$baseUrl = "http://localhost:8082"
$logFile = "test_cycle_$(Get-Date -Format 'yyyyMMdd_HHmmss').log"

function Write-Log {
    param([string]$message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMessage = "[$timestamp] $message"
    Write-Host $logMessage
    Add-Content -Path $logFile -Value $logMessage
}

function Test-HealthEndpoint {
    try {
        $response = Invoke-WebRequest -Uri "$baseUrl/health" -Method GET -UseBasicParsing -ErrorAction Stop
        return $response.StatusCode -eq 200
    } catch {
        return $false
    }
}

function Start-Application {
    # Kill any existing Java processes
    Write-Log "Stopping any existing Java processes..."
    Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -like '*WA_TG_FB_SeleniumFreelancerAppV2*' } | Stop-Process -Force
    
    # Build the application
    Write-Log "Building the application..."
    & mvn clean install -DskipTests
    
    if ($LASTEXITCODE -ne 0) {
        throw "Build failed with exit code $LASTEXITCODE"
    }
    
    # Start the application in background
    Write-Log "Starting the application..."
    Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -NoNewWindow -PassThru -WorkingDirectory $PWD
    
    # Wait for application to start (max 60 seconds)
    $timeout = 60
    $elapsed = 0
    $interval = 2
    
    while ($elapsed -lt $timeout) {
        if (Test-HealthEndpoint) {
            Write-Log "Application started successfully"
            return $true
        }
        Start-Sleep -Seconds $interval
        $elapsed += $interval
    }
    
    throw "Application failed to start within $timeout seconds"
}

function Test-TraverseEndpoint {
    try {
        Write-Log "Hitting $baseUrl/social-app/tg/traverse..."
        $response = Invoke-WebRequest -Uri "$baseUrl/social-app/tg/traverse" -Method GET -UseBasicParsing -ErrorAction Stop
        Write-Log "Response status: $($response.StatusCode) $($response.StatusDescription)"
        return $true
    } catch {
        Write-Log "Error hitting traverse endpoint: $_"
        return $false
    }
}

function Get-LatestLogs {
    $logDir = "$PWD/logs"
    $logFile = Get-ChildItem -Path $logDir -Filter "*.log" | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if ($logFile) {
        return Get-Content -Path $logFile.FullName -Tail 20 -ErrorAction SilentlyContinue
    }
    return "No log files found in $logDir"
}

try {
    Write-Log "Starting test cycle at $(Get-Date)"
    
    for ($i = 1; $i -le $maxRetries; $i++) {
        Write-Log "`n--- Starting iteration $i of $maxRetries ---"
        
        try {
            # Start the application
            Start-Application
            
            # Test the traverse endpoint
            $success = Test-TraverseEndpoint
            
            # Wait 10 seconds
            Write-Log "Waiting 10 seconds..."
            Start-Sleep -Seconds 10
            
            # Check logs for errors
            $logs = Get-LatestLogs
            $errorLogs = $logs | Select-String -Pattern "ERROR|Exception" -SimpleMatch
            
            if ($errorLogs) {
                Write-Log "Found errors in logs:"
                $errorLogs | ForEach-Object { Write-Log "  $_" }
                throw "Errors detected in logs during iteration $i"
            } else {
                Write-Log "No errors found in logs after iteration $i"
            }
            
        } catch {
            Write-Log "Error during iteration $i : $_"
            Write-Log "Stack trace: $($_.ScriptStackTrace)"
            throw "Test failed during iteration $i"
        } finally {
            # Ensure we clean up the application process
            Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -like '*WA_TG_FB_SeleniumFreelancerAppV2*' } | Stop-Process -Force -ErrorAction SilentlyContinue
        }
        
        Write-Log "--- Completed iteration $i of $maxRetries ---`n"
    }
    
    Write-Log "`nAll $maxRetries iterations completed successfully!"
    
} catch {
    Write-Log "Test cycle failed: $_"
    Write-Log "Stack trace: $($_.ScriptStackTrace)"
    exit 1
} finally {
    # Final cleanup
    Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -like '*WA_TG_FB_SeleniumFreelancerAppV2*' } | Stop-Process -Force -ErrorAction SilentlyContinue
    Write-Log "Test cycle completed at $(Get-Date)"
}
