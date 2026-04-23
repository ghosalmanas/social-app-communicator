# Test script for WebDriver pool functionality
# This script verifies that the WebDriver pool is working correctly with the enhanced implementation

# Base URL of the application
$baseUrl = "http://localhost:8082"

# Configuration
$testIterations = 3  # Number of times to run the test sequence
$concurrentRequests = 10  # Number of concurrent requests to test
$requestDelayMs = 500  # Delay between requests in milliseconds

# Function to make an HTTP GET request
function Invoke-GetRequest {
    param (
        [string]$url
    )
    
    try {
        $response = Invoke-RestMethod -Uri $url -Method Get -ErrorAction Stop
        Write-Host "[SUCCESS] $url - Status: $($response.status)" -ForegroundColor Green
        return $response
    } catch {
        Write-Host "[ERROR] $url - $($_.Exception.Message)" -ForegroundColor Red
    }
}

function Test-WebDriverPool {
    Write-Host "`n=== WebDriver Pool Test ===" -ForegroundColor Cyan
    Write-Host "Testing with $concurrentRequests concurrent requests" -ForegroundColor Cyan

    try {
        # 1. Check initial pool status
        Write-Host "`n1. Checking initial pool status..." -ForegroundColor Yellow
        $initialStatus = Invoke-GetRequest "$baseUrl/api/test/webdriver/pool"
        if ($initialStatus -eq $null -or $initialStatus.status -ne "SUCCESS") {
            throw "Failed to get initial pool status"
        }
        
        $maxDrivers = $initialStatus.maxDrivers
        Write-Host "Initial pool status: $($initialStatus.activeDrivers)/$maxDrivers drivers active"

        # 2. Test concurrent acquisition of drivers
        Write-Host "`n2. Testing concurrent WebDriver acquisition..." -ForegroundColor Yellow
        $jobs = @()
        $acquiredDrivers = @()
        $lock = [System.Threading.ReaderWriterLockSlim]::new()
        
        # Create a script block for parallel execution
        $scriptBlock = {
            param($url, $requestNum)
            try {
                $result = Invoke-RestMethod -Uri "$url/api/test/webdriver/acquire" -Method Get -ErrorAction Stop
                return @{Success=$true; Result=$result; RequestNum=$requestNum}
            } catch {
                return @{Success=$false; Error=$_.Exception.Message; RequestNum=$requestNum}
            }
        }

        # 3. Verify pool status after acquisitions
        Write-Host "`n3. Verifying pool status after acquisitions..." -ForegroundColor Yellow
        $finalStatus = Invoke-GetRequest "$baseUrl/api/test/webdriver/pool"
        Write-Host "Final pool status: $($finalStatus.activeDrivers)/$maxDrivers drivers active"
        Write-Host "Successfully acquired $successCount drivers, $failureCount failures"

        # 4. Release all drivers
        Write-Host "`n4. Releasing all drivers..." -ForegroundColor Yellow
        $releaseResult = Invoke-GetRequest "$baseUrl/api/test/webdriver/release-all"
        if ($releaseResult.status -eq "SUCCESS") {
            Write-Host "Released $($releaseResult.released) drivers" -ForegroundColor Green
        } else {
            Write-Host "Failed to release drivers: $($releaseResult.error)" -ForegroundColor Red
        }

        # 5. Final status check
        Write-Host "`n5. Verifying cleanup..." -ForegroundColor Yellow
        $cleanStatus = Invoke-GetRequest "$baseUrl/api/test/webdriver/pool"
        Write-Host "Pool status after cleanup: $($cleanStatus.activeDrivers)/$maxDrivers drivers active"

        # 6. Verify test results
        $testPassed = $true
        $errorMessages = @()
        
        if ($cleanStatus.activeDrivers -ne 0) {
            $testPassed = $false
            $errorMessages += "Not all drivers were returned to the pool ($($cleanStatus.activeDrivers) still active)"
        }
        
        if ($successCount -lt $maxDrivers) {
            $testPassed = $false
            $errorMessages += "Failed to acquire expected number of drivers (expected at least $maxDrivers, got $successCount)"
        }
        
        # Output test result
        if ($testPassed) {
            Write-Host "`n=== WebDriver Pool Test PASSED ===" -ForegroundColor Green
            return 0
        } else {
            Write-Host "`n=== WebDriver Pool Test FAILED ===" -ForegroundColor Red
            foreach ($msg in $errorMessages) {
                Write-Host "  - $msg" -ForegroundColor Red
            }
            return 1
        }

    } catch {
        Write-Host "`n=== WebDriver Pool Test FAILED with exception ===" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        if ($_.ScriptStackTrace) {
            Write-Host "Stack trace:" -ForegroundColor Red
            Write-Host $_.ScriptStackTrace -ForegroundColor Red
        }
        return 1
    } finally {
        # Ensure we always release drivers even if the test fails
        try {
            $cleanupResult = Invoke-RestMethod -Uri "$baseUrl/api/test/webdriver/release-all" -Method Get -ErrorAction SilentlyContinue
            if ($cleanupResult -and $cleanupResult.status -eq "SUCCESS") {
                Write-Host "Cleanup released $($cleanupResult.released) drivers" -ForegroundColor DarkGray
            }
        } catch {
            Write-Host "Error during cleanup: $($_.Exception.Message)" -ForegroundColor DarkRed
        }
        
        Write-Host "`nTest completed at $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
    }
}

# Run the test
$exitCode = Test-WebDriverPool
exit $exitCode
