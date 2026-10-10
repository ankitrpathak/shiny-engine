@echo off
echo =====================================================================
echo  STARTING ON-DEMAND CAR WASH BACKEND MICROSERVICES
echo =====================================================================

echo [1/8] Starting Eureka Discovery Server on port 8761...
start "1. Eureka Discovery Server (8761)" cmd /k "cd eureka-server && mvn spring-boot:run"
timeout /t 12 /nobreak > nul

echo [2/8] Starting API Gateway on port 8080...
start "2. API Gateway (8080)" cmd /k "cd api-gateway && mvn spring-boot:run"
timeout /t 8 /nobreak > nul

echo [3/8] Starting User Service on port 8081...
start "3. User Service (8081)" cmd /k "cd OnDemandCarWash-user-service && mvn spring-boot:run"

echo [4/8] Starting Booking Service on port 8083...
start "4. Booking Service (8083)" cmd /k "cd OnDemandCarWash-booking-service && mvn spring-boot:run"

echo [5/8] Starting Payment Service on port 8084...
start "5. Payment Service (8084)" cmd /k "cd OnDemandCarWash-payment-service && mvn spring-boot:run"

echo [6/8] Starting Review Service on port 8085...
start "6. Review Service (8085)" cmd /k "cd OnDemandCarWash-review-service && mvn spring-boot:run"

echo [7/8] Starting Notification Service on port 8086...
start "7. Notification Service (8086)" cmd /k "cd OnDemandCarWash-notification-service && mvn spring-boot:run"

echo [8/8] Starting Admin Service on port 8082...
start "8. Admin Service (8082)" cmd /k "cd adminservice && mvn spring-boot:run"

echo =====================================================================
echo  All 8 backend microservices have been launched in separate windows!
echo  Wait ~30 seconds for them to register with Eureka.
echo =====================================================================
pause
