# Simple WebDriver Pool Test Script
# Verifies basic WebDriver pool functionality

# Configuration
$baseUrl = "http://localhost:8082/api/test"  # Base URL of your Spring Boot application with /api/test prefix to make HTTP requests
$testIterations = 3

# Helper function to make HTTP requests
function Invoke-GetRequest {
    param (
        [string]$Url
    )
    try {
        Write-Host "  Calling: $Url" -ForegroundColor DarkGray
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -ErrorAction Stop
        $result = $response.Content | ConvertFrom-Json
        Write-Host "  Response: $($result | ConvertTo-Json -Depth 5 -Compress)" -ForegroundColor DarkGray
        return $result
    } catch {
        Write-Host "  [ERROR] Failed to call $Url" -ForegroundColor Red
        Write-Host "  Status: $($_.Exception.Response.StatusCode.value__) $($_.Exception.Response.StatusDescription)" -ForegroundColor Red
        Write-Host "  Message: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.ErrorDetails.Message) {
            Write-Host "  Details: $($_.ErrorDetails.Message)" -ForegroundColor Red
        }
        return $null
    }
}

function Test-WebDriverPool {
    param (
        [int]$iterations = 3
    )
    
    Write-Host "`n=== WebDriver Pool Test ===`n" -ForegroundColor Cyan
    
    # 0. Ensure clean state before starting tests
    Write-Host "0. Ensuring clean state..." -ForegroundColor Yellow
    $releaseUrl = "$baseUrl/webdriver/release-all"
    $releaseResult = Invoke-WebRequest -Uri $releaseUrl -Method Get -UseBasicParsing -ErrorAction SilentlyContinue
    if ($releaseResult) {
        $releaseJson = $releaseResult.Content | ConvertFrom-Json
        if ($releaseJson.status -eq "SUCCESS") {
            Write-Host "  - Released $($releaseJson.released) drivers. Active: $($releaseJson.activeDrivers)/$($releaseJson.maxDrivers)" -ForegroundColor Green
        } else {
            Write-Host "  [WARNING] Failed to release drivers: $($releaseJson.error)" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  [WARNING] Could not release drivers before test" -ForegroundColor Yellow
    }
    
    # Add a small delay to ensure all drivers are properly released
    Start-Sleep -Seconds 2
    
    $testPassed = $true
    
    for ($i = 1; $i -le $iterations; $i++) {
        Write-Host "`n--- Test Iteration $i/$iterations ---`n" -ForegroundColor Yellow
        
        # 1. Check current pool status
        Write-Host "1. Checking pool status..."
        $statusUrl = "$baseUrl/webdriver/pool"
        $status = Invoke-GetRequest $statusUrl
        if (-not $status -or $status.status -ne "SUCCESS") {
            Write-Host "  [ERROR] Failed to get pool status" -ForegroundColor Red
            return $false
        }
        
        # If there are active drivers, try to release them
        if ($status.activeDrivers -gt 0) {
            Write-Host "  - Found $($status.activeDrivers) active drivers. Attempting to release them..." -ForegroundColor Yellow
            $releaseResult = Invoke-WebRequest -Uri "$baseUrl/webdriver/release-all" -Method Get -UseBasicParsing -ErrorAction SilentlyContinue
            if ($releaseResult) {
                $releaseJson = $releaseResult.Content | ConvertFrom-Json
                if ($releaseJson.status -eq "SUCCESS") {
                    Write-Host "  - Released $($releaseJson.released) drivers. Active: $($releaseJson.activeDrivers)/$($status.maxDrivers)" -ForegroundColor Green
                    # Update status after release
                    $status = Invoke-GetRequest $statusUrl
                } else {
                    Write-Host "  [WARNING] Failed to release drivers: $($releaseJson.error)" -ForegroundColor Yellow
                }
            } else {
                Write-Host "  [WARNING] Could not release drivers" -ForegroundColor Yellow
            }
        }
        
        Write-Host "  - Current: $($status.activeDrivers)/$($status.maxDrivers) drivers" -ForegroundColor Green
        
        # 2. Test acquiring drivers up to max + 1
        Write-Host "`n2. Testing driver acquisition..."
        $maxDrivers = $status.maxDrivers
        $drivers = @()
        $success = $true
        
        for ($j = 1; $j -le $maxDrivers + 1; $j++) {
            $acquireUrl = "$baseUrl/webdriver/acquire"
            Write-Host "  Attempt $j/$($maxDrivers + 1): Acquiring driver..." -ForegroundColor DarkGray
            $result = Invoke-GetRequest $acquireUrl
            
            if ($result) {
                if ($result.status -eq "SUCCESS") {
                    $driverId = $result.driverHash
                    $drivers += $driverId
                    $activeCount = $result.activeDrivers
                    
                    Write-Host "  - Acquired driver $driverId ($activeCount/$maxDrivers)" -ForegroundColor Green
                    
                    # Check for duplicate driver hashes
                    $duplicateCount = ($drivers | Where-Object { $_ -eq $driverId }).Count
                    if ($duplicateCount -gt 1) {
                        Write-Host "  [WARNING] Duplicate driver hash detected: $driverId (seen $duplicateCount times)" -ForegroundColor Yellow
                    }
                    
                    # Verify we don't exceed max drivers
                    if ($activeCount -gt $maxDrivers) {
                        Write-Host "  [ERROR] Exceeded maximum number of drivers: $activeCount > $maxDrivers" -ForegroundColor Red
                        $success = $false
                        break
                    }
                }
                elseif ($result.status -eq "MAX_LIMIT_REACHED") {
                    $activeCount = $result.activeDrivers
                    $maxAllowed = $result.maxDrivers
                    Write-Host "  - [EXPECTED] Reached maximum concurrent drivers: $activeCount/$maxAllowed" -ForegroundColor Yellow
                    
                    # This is expected on the last acquisition attempt
                    if ($j -eq $maxDrivers + 1) {
                        Write-Host "  - [SUCCESS] Successfully prevented acquiring more than $maxAllowed drivers" -ForegroundColor Green
                        $success = $true
                    } else {
                        Write-Host "  [ERROR] Reached max drivers earlier than expected (attempt $j/$($maxDrivers + 1))" -ForegroundColor Red
                        $success = $false
                    }
                    break
                }
            } else {
                $errorMsg = if ($result.error) { $result.error } else { "Unknown error" }
                $statusCode = if ($result.status) { "Status: $($result.status) - " } else { "" }
                Write-Host ("  [ERROR] Failed to acquire driver {0}: {1}{2}" -f $j, $statusCode, $errorMsg) -ForegroundColor Red
                $success = $false
                break
            }
            
            # Add a small delay between acquisitions
            Start-Sleep -Milliseconds 500
        }
        
        if (-not $success) {
            $testPassed = $false
            break
        }
        
        # 3. Release all drivers
        Write-Host "`n3. Releasing all drivers..."
        
        try {
            $releaseAllUrl = "$baseUrl/webdriver/release-all"
            Write-Host "  Calling: $releaseAllUrl" -ForegroundColor DarkGray
            $releaseResponse = Invoke-WebRequest -Uri $releaseAllUrl -Method Get -UseBasicParsing -ErrorAction Stop
            $release = $releaseResponse.Content | ConvertFrom-Json
            
            if ($release.status -ne "SUCCESS") {
                Write-Host "  [ERROR] Failed to release drivers: $($release.error)" -ForegroundColor Red
                $testPassed = $false
            } else {
                Write-Host "  Released $($release.released) drivers. Active: $($release.activeDrivers)/$maxDrivers" -ForegroundColor Green
                
                # Verify all drivers were released
                Start-Sleep -Seconds 1  # Give some time for the release to complete
                $verifyStatus = Invoke-GetRequest "$baseUrl/webdriver/pool"
                if ($verifyStatus -and $verifyStatus.status -eq "SUCCESS") {
                    if ($verifyStatus.activeDrivers -gt 0) {
                        Write-Host "  [ERROR] Not all drivers were released. Active: $($verifyStatus.activeDrivers)" -ForegroundColor Red
                        $testPassed = $false
                    } else {
                        Write-Host "  - All drivers released successfully" -ForegroundColor Green
                    }
                } else {
                    Write-Host "  [WARNING] Could not verify driver release status" -ForegroundColor Yellow
                }
            }
        } catch {
            Write-Host "  [ERROR] Failed to release drivers: $($_.Exception.Message)" -ForegroundColor Red
            Write-Host "  Status: $($_.Exception.Response.StatusCode.value__) $($_.Exception.Response.StatusDescription)" -ForegroundColor Red
            
            # Try to get response body if available
            try {
                $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
                $reader.BaseStream.Position = 0
                $reader.DiscardBufferedData()
                $responseBody = $reader.ReadToEnd()
                if ($responseBody) {
                    Write-Host "  Response: $responseBody" -ForegroundColor Red
                }
            } catch {
                Write-Host "  Could not read response body: $($_.Exception.Message)" -ForegroundColor DarkRed
            }
            
            $testPassed = $false
            Write-Host "  [WARNING] Cannot continue testing without releasing drivers" -ForegroundColor Yellow
            return $false
        }

        Write-Host "`n--- End of Iteration $i ---`n" -ForegroundColor Yellow
    }
    
    return $testPassed
}

# Run the test
$result = Test-WebDriverPool -iterations 3
Write-Host "`n=== Test Completed: $(if ($result) { 'PASSED' } else { 'FAILED' }) ===" -ForegroundColor $(if ($result) { 'Green' } else { 'Red' })
