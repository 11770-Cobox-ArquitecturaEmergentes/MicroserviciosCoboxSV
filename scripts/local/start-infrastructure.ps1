<#
.SYNOPSIS
    Automates the build and deployment process for the Cobox Microservices architecture.

.DESCRIPTION
    This script performs a full Maven multi-module build from the project root,
    packaging all microservices into executable JARs. Subsequently, it utilizes
    Docker Compose to build the corresponding Docker images and orchestrate the
    containers. It includes error handling to ensure pipeline integrity.

.NOTES
    Author: Cobox Smart Vision Team
    Version: 1.0.0
    Prerequisites: Java 17+, Docker, Docker Compose
#>

param (
    [switch]$EnableSSL
)

# 1. Pipeline Configuration: Fail-fast methodology
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# 2. Standardized Logging Function
function Write-Log {
    param (
        [Parameter(Mandatory=$true)][string]$Message,
        [ValidateSet("INFO", "SUCCESS", "WARNING", "ERROR")][string]$Level = "INFO"
    )
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $color = switch ($Level) { "INFO"{"Cyan"} "SUCCESS"{"Green"} "WARNING"{"Yellow"} "ERROR"{"Red"} }
    Write-Host "[$timestamp] [$Level] $Message" -ForegroundColor $color
}

Write-Log "Starting the automated build pipeline for Cobox Microservices..." "INFO"

# 3. Maven Multi-Module Build Phase
Write-Log "Executing Maven multi-module compilation (Reactor)..." "INFO"
try {
    # Using cmd.exe to ensure proper execution of the batch wrapper in PowerShell
    cmd.exe /c "mvnw.cmd -q -T 1C -DskipTests clean package"

    if ($LASTEXITCODE -ne 0) {
        Write-Log "Maven build failed with exit code $LASTEXITCODE. Aborting pipeline." "ERROR"
        exit $LASTEXITCODE
    }
    Write-Log "Maven build completed successfully." "SUCCESS"
}
catch {
    Write-Log "An unexpected error occurred during the Maven build phase: $_" "ERROR"
    exit 1
}

# 4. Docker Infrastructure Provisioning Phase
Write-Log "Provisioning infrastructure via Docker Compose..." "INFO"
try {
    $composeCommand = "docker compose -f docker-compose.yml"

    # Evaluate optional profiles
    if ($EnableSSL) {
        Write-Log "SSL Profile explicitly enabled. Certbot will be triggered." "WARNING"
        $composeCommand += " --profile ssl"
    }

    # CRITICAL FIX: Limit concurrent builds to prevent Docker Desktop (grpc/timeout) crashes
    Write-Log "Setting concurrent build limit to 2..." "INFO"
    $env:COMPOSE_PARALLEL_LIMIT = "2"

    # Execute dynamic command
    Invoke-Expression "$composeCommand up -d --build"

    if ($LASTEXITCODE -ne 0) {
        Write-Log "Docker deployment failed with exit code $LASTEXITCODE. Aborting pipeline." "ERROR"
        exit $LASTEXITCODE
    }
    Write-Log "Docker infrastructure successfully provisioned in detached mode." "SUCCESS"
}
catch {
    Write-Log "An unexpected error occurred during the Docker deployment phase: $_" "ERROR"
    exit 1
}

Write-Log "Pipeline executed successfully! (Note: Services may take 1-3 minutes to become fully healthy due to dependencies)" "SUCCESS"