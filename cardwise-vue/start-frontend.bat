@echo off
echo ========================================
echo   CardWise Frontend - Starting...
echo ========================================

echo Installing dependencies...
call npm install

echo.
echo Starting dev server on http://localhost:5173...
echo.
npm run dev

pause
