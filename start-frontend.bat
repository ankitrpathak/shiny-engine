@echo off
echo =====================================================================
echo  STARTING ON-DEMAND CAR WASH ANGULAR FRONTEND (PORT 4200)
echo =====================================================================

cd Frontend\odc-frontend

if not exist node_modules (
    echo Installing npm dependencies, please wait...
    call npm install
)

echo Starting Angular Development Server on http://localhost:4200 ...
call npm start
pause
