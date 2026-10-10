@echo off
setlocal enabledelayedexpansion

echo =====================================================================
echo  STARTING ON-DEMAND CAR WASH BACKEND MICROSERVICES
echo =====================================================================

:: 1. Load .env file if present
if exist "%~dp0.env" (
    echo [CONFIG] Found .env file, exporting configuration variables...
    for /f "usebackq eol=# tokens=1* delims==" %%A in ("%~dp0.env") do (
        set "%%A=%%B"
    )
)

:: 2. Check for Maven or Maven Wrapper
where mvn >nul 2>nul
if %errorlevel% equ 0 (
    set "MVN_CMD=mvn"
    echo [OK] Found installed Apache Maven in PATH.
) else if exist "%~dp0mvnw.cmd" (
    set "MVN_CMD=%~dp0mvnw.cmd"
    echo [OK] Maven not found in PATH; using project Maven Wrapper (mvnw.cmd).
) else (
    echo.
    echo =====================================================================
    echo [ERROR] Maven was not detected!
    echo Neither 'mvn' in PATH nor 'mvnw.cmd' in the project root was found.
    echo.
    echo EASY ALTERNATIVES:
    echo 1. Open this project in IntelliJ IDEA / Eclipse and run each of the 8
    echo    @SpringBootApplication main classes directly.
    echo 2. OR download and install Apache Maven and add its bin folder to PATH.
    echo =====================================================================
    echo.
    pause
    exit /b 1
)

echo.
echo [1/8] Starting Eureka Discovery Server on port 8761...
start "1. Eureka Discovery Server (8761)" cmd /k "cd /d "%~dp0eureka-server" && call "%MVN_CMD%" spring-boot:run"
timeout /t 12 /nobreak > nul

echo [2/8] Starting API Gateway on port 8080...
start "2. API Gateway (8080)" cmd /k "cd /d "%~dp0api-gateway" && call "%MVN_CMD%" spring-boot:run"
timeout /t 8 /nobreak > nul

echo [3/8] Starting User Service on port 8081...
start "3. User Service (8081)" cmd /k "cd /d "%~dp0OnDemandCarWash-user-service" && call "%MVN_CMD%" spring-boot:run"

echo [4/8] Starting Booking Service on port 8083...
start "4. Booking Service (8083)" cmd /k "cd /d "%~dp0OnDemandCarWash-booking-service" && call "%MVN_CMD%" spring-boot:run"

echo [5/8] Starting Payment Service on port 8084...
start "5. Payment Service (8084)" cmd /k "cd /d "%~dp0OnDemandCarWash-payment-service" && call "%MVN_CMD%" spring-boot:run"

echo [6/8] Starting Review Service on port 8085...
start "6. Review Service (8085)" cmd /k "cd /d "%~dp0OnDemandCarWash-review-service" && call "%MVN_CMD%" spring-boot:run"

echo [7/8] Starting Notification Service on port 8086...
start "7. Notification Service (8086)" cmd /k "cd /d "%~dp0OnDemandCarWash-notification-service" && call "%MVN_CMD%" spring-boot:run"

echo [8/8] Starting Admin Service on port 8082...
start "8. Admin Service (8082)" cmd /k "cd /d "%~dp0adminservice" && call "%MVN_CMD%" spring-boot:run"

echo =====================================================================
echo  All 8 backend microservices have been launched in separate windows!
echo  Wait ~30 seconds for them to register with Eureka.
echo =====================================================================
pause
