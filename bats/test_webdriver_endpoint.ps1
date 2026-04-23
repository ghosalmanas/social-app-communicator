# Test script to verify WebDriver pool functionality

$baseUrl = "http://localhost:8082"
$endpoints = @(
    "/social-app/wa/traverse",
    "/social-app/tg/traverse",
    "/social-app/fb/traverse"
)

# Function to send a request and log the result
function Test-WebDriverEndpoint {
    param($endpoint, $requestNum)
    $url = "${baseUrl}${endpoint}"
    try {
        $startTime = Get-Date
        $response = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 30
        $duration = ((Get-Date) - $startTime).TotalMilliseconds
        Write-Host "[SUCCESS] Request ${requestNum} to ${endpoint} completed in ${duration}ms"
        return $true
    } catch {
        $errorMsg = $_.Exception.Message
        Write-Host "[ERROR] Request ${requestNum} to ${endpoint} failed: ${errorMsg}"
        return $false
    }
}

# Test each endpoint sequentially
Write-Host "Testing WebDriver endpoints sequentially..."
$results = @()
$count = 1
foreach ($endpoint in $endpoints) {
    $result = Test-WebDriverEndpoint -endpoint $endpoint -requestNum $count
    $results += [PSCustomObject]@{
        Request = $count
        Endpoint = $endpoint
        Success = $result
    }
    $count++
    Start-Sleep -Seconds 1
}

# Display results
Write-Host "`nTest Results:"
$results | Format-Table -AutoSize

# Test concurrent requests
Write-Host "`nTesting concurrent WebDriver requests..."
$numConcurrent = 10
$jobs = @()

for ($i = 1; $i -le $numConcurrent; $i++) {
    $endpoint = $endpoints[($i - 1) % $endpoints.Count]
    $job = Start-Job -ScriptBlock {
        param($url, $reqNum)
        $startTime = Get-Date
        try {
            $response = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 60
            $duration = ((Get-Date) - $startTime).TotalMilliseconds
            return @{ 
                Success = $true
                Message = "Request ${reqNum} to $url completed in ${duration}ms"
            }
        } catch {
            return @{ 
                Success = $false
                Message = "Request ${reqNum} to $url failed: $($_.Exception.Message)"
            }
        }
    } -ArgumentList "${baseUrl}${endpoint}", $i
    $jobs += $job
    Write-Host "Started request $i to $endpoint"
    Start-Sleep -Milliseconds 100
}

# Wait for all jobs to complete
$jobResults = $jobs | Wait-Job | Receive-Job
$jobs | Remove-Job

# Display concurrent test results
Write-Host "`nConcurrent Test Results:"
$jobResults | ForEach-Object { 
    $status = if ($_.Success) { "[SUCCESS]" } else { "[FAILED] " }
    Write-Host "${status} $($_.Message)"
}
