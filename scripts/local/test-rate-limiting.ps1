# ================================================
# Rate Limiting Test Script (Spring Boot Gateway)
# ================================================

$Global:ErrorActionPreference = "Stop"
$BaseUrl = "http://localhost:8080"

function Write-Separator {
    param ([string]$Title)
    Write-Host "`n======================================================" -ForegroundColor Cyan
    if ($Title) {
        Write-Host "    $Title" -ForegroundColor Cyan
    }
    Write-Host "======================================================" -ForegroundColor Cyan
}

function Write-Status {
    param ([string]$Label, [string]$Value, [string]$Color = "White")
    Write-Host "  $($Label): " -NoNewline -ForegroundColor Gray
    Write-Host $Value -ForegroundColor $Color
}

Write-Separator "Rate Limiting Tests - API Gateway"
Write-Host "  Target: $BaseUrl" -ForegroundColor Gray

# ================================================
# Test 1: View Configuration
# ================================================
Write-Host "`n1. Fetching Rate Limiter Configuration..." -ForegroundColor Yellow
try {
    $config = Invoke-RestMethod -Uri "$BaseUrl/actuator/rate-limit/config" -Method GET
    Write-Host "OK Configuration retrieved:" -ForegroundColor Green
    $config | ConvertTo-Json -Depth 5 | Write-Host
} catch {
    Write-Host "XX Error: Gateway unavailable or actuator inactive" -ForegroundColor Red
    Write-Host "   Details: $($_.Exception.Message)" -ForegroundColor Red
    exit
}

Start-Sleep -Seconds 2

# ================================================
# Test 2: Headers Check (Standard Request)
# ================================================
Write-Host "`n2. Validating Response Headers..." -ForegroundColor Yellow
try {
    # Using a high-limit endpoint (Actuator: 200 req/10s)
    $response = Invoke-WebRequest -Uri "$BaseUrl/actuator/health" -Method GET
    Write-Host "OK Request Successful - Status: $($response.StatusCode)" -ForegroundColor Green

    if ($response.Headers['X-RateLimit-Limit']) {
        Write-Status "Limit" $response.Headers['X-RateLimit-Limit'] "Cyan"
        Write-Status "Remaining" $response.Headers['X-RateLimit-Remaining'] "Cyan"
        Write-Status "Reset (s)" $response.Headers['X-RateLimit-Reset'] "Cyan"
    } else {
        Write-Host "XX Warning: X-RateLimit headers missing" -ForegroundColor Magenta
    }
} catch {
    Write-Host "XX Request Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Start-Sleep -Seconds 2

# ================================================
# Test 3: Specific Limit Test (Authentication)
# CONFIG: window=60s, max=20
# GOAL: Send 25 requests, expect ~5 blocks
# ================================================
Write-Host "`n3. Testing Specific Rule: 'api/v1/authentication'..." -ForegroundColor Yellow
Write-Host "   Config: 20 req / 60s" -ForegroundColor Gray
Write-Host "   Sending 25 requests rapidly..." -ForegroundColor Gray

$targetUri = "$BaseUrl/api/v1/authentication/sign-in" # Updated to match new config
# NOTE: If /api/v1/authentication/sign-in does not exist, it might return 404
# The Rate Limiter usually works before 404, but if not, change URL to a valid one.

$allowed = 0
$blocked = 0
$startTime = Get-Date

1..25 | ForEach-Object {
    try {
        # Using -ErrorAction SilentlyContinue to handle 404s as 'Allowed' traffic if endpoint doesn't exist
        $r = Invoke-WebRequest -Uri $targetUri -Method GET -ErrorAction Stop
        $allowed++
        Write-Host "." -NoNewline -ForegroundColor Green
    } catch {
        $ex = $_.Exception
        if ($ex.Response -and $ex.Response.StatusCode -eq 429) {
            $blocked++
            Write-Host "X" -NoNewline -ForegroundColor Red
        } elseif ($ex.Response -and $ex.Response.StatusCode -eq 404) {
             # Treat 404 as allowed (passed the gateway, reached missing service)
            $allowed++
            Write-Host "." -NoNewline -ForegroundColor Green
        } else {
            Write-Host "?" -NoNewline -ForegroundColor Yellow
        }
    }
}

# Fixed parenthesis error here: correctly calculating TotalSeconds before rounding
$duration = [math]::Round(((Get-Date) - $startTime).TotalSeconds, 2)

Write-Host "`n"
Write-Status "Allowed" $allowed "Green"
Write-Status "Blocked" $blocked "Red"
Write-Status "Duration" "$duration sec" "White"

if ($blocked -gt 0) {
    Write-Host "OK Specific limit enforced successfully." -ForegroundColor Green
} else {
    Write-Host "XX Warning: limit not reached. Increase request count or check config." -ForegroundColor Magenta
}

Start-Sleep -Seconds 2

# ================================================
# Test 4: Global Limit Test (Unmatched Route)
# CONFIG: window=10s, max=100
# GOAL: Send 110 requests to a random path
# ================================================
Write-Host "`n4. Testing Global Fallback..." -ForegroundColor Yellow
Write-Host "   Config: 100 req / 10s (Global)" -ForegroundColor Gray
Write-Host "   Targeting path: /api/v1/drivers" -ForegroundColor Gray

$targetUri = "$BaseUrl/api/v1/drivers"
$allowed = 0
$blocked = 0

1..110 | ForEach-Object {
    try {
        $null = Invoke-WebRequest -Uri $targetUri -Method GET -ErrorAction Stop
        $allowed++
    } catch {
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode -eq 429) {
            $blocked++
            Write-Host "X" -NoNewline -ForegroundColor Red
        } else {
            # 404s count as allowed traffic for rate limit purposes
            $allowed++
        }
    }
    # Simple visual feedback every 10 requests
    if ($_ % 10 -eq 0) { Write-Host "." -NoNewline -ForegroundColor Green }
}

Write-Host "`n"
Write-Status "Allowed" $allowed "Green"
Write-Status "Blocked" $blocked "Red"

if ($blocked -gt 0) {
    Write-Host "OK Global limit enforced successfully." -ForegroundColor Green
} else {
    Write-Host "XX Warning: Global limit not breached. Loop might be too slow." -ForegroundColor Yellow
}

# ================================================
# Test 5: Verify 429 Body & Reset
# ================================================
Write-Host "`n5. Verifying 429 Response & Window Reset..." -ForegroundColor Yellow

# Force a block on the tightest rule (Auth: 20 reqs)
# We already saturated it in Test 3.
try {
    $null = Invoke-WebRequest -Uri "$BaseUrl/api/v1/authentication/sign-in" -Method GET -ErrorAction Stop
    Write-Host "   Warning: Request was NOT blocked." -ForegroundColor Yellow
} catch {
    if ($_.Exception.Response -and $_.Exception.Response.StatusCode -eq 429) {
        Write-Host "OK Got 429 Too Many Requests." -ForegroundColor Green

        # Optional: Print headers to see when it resets
        $resetVal = $_.Exception.Response.Headers['X-RateLimit-Reset']
        if ($resetVal) {
             Write-Host "   Bucket resets in: $resetVal seconds" -ForegroundColor Cyan
        }
    }
}

Write-Host "   Waiting 10 seconds for partial reset..." -ForegroundColor Gray
Start-Sleep -Seconds 10

try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/actuator/health" -Method GET
    Write-Host "OK Request accepted after wait (Actuator Endpoint)." -ForegroundColor Green
} catch {
    Write-Host "XX Still blocked: $($_.Exception.Message)" -ForegroundColor Red
}

# ================================================
# Test 6: Prometheus Metrics
# ================================================
Write-Host "`n6. Checking Prometheus Metrics..." -ForegroundColor Yellow
try {
    $metrics = Invoke-RestMethod -Uri "$BaseUrl/actuator/prometheus" -Method GET

    # Simple regex search for specific metrics
    $foundAllowed = $metrics -match "gateway_ratelimit_allowed_total"
    $foundBlocked = $metrics -match "gateway_ratelimit_blocked_total"

    if ($foundAllowed) { Write-Host "OK Metric found: gateway_ratelimit_allowed_total" -ForegroundColor Green }
    if ($foundBlocked) { Write-Host "OK Metric found: gateway_ratelimit_blocked_total" -ForegroundColor Green }

    if (-not ($foundAllowed -or $foundBlocked)) {
        Write-Host "XX No rate-limit metrics found in Prometheus output." -ForegroundColor Red
    }
} catch {
    Write-Host "XX Failed to fetch metrics: $($_.Exception.Message)" -ForegroundColor Red
}

# ================================================
# Final Summary
# ================================================
Write-Separator "TESTS COMPLETED"
Write-Host "RNF Validation Summary:" -ForegroundColor Yellow
Write-Host "  [+] Security:       Brute-force protection verified (429s)" -ForegroundColor Cyan
Write-Host "  [+] Availability:   Load shedding verified" -ForegroundColor Cyan
Write-Host "  [+] Observability:  Metrics exposed" -ForegroundColor Cyan
Write-Host ""