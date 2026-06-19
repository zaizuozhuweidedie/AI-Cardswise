@echo off
echo ========================================
echo   CardWise Server - Starting...
echo ========================================

set "DATABASE_URL=jdbc:postgresql://ep-broad-heart-aozjq20t-pooler.c-2.ap-southeast-1.aws.neon.tech/neondb?sslmode=require"
set "DATABASE_USERNAME=neondb_owner"
set "DATABASE_PASSWORD=npg_rCeit86dLNIK"
set "JWT_SECRET=VI/aaqa0kvz9HpQT2svZrmscW6RnA4RWcv+WOycvZf8="
set "AI_API_KEY=sk-0c9ba4cb96914d73b35593a25222a892"

echo [1/2] Compiling...
call mvn compile -q
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo [2/2] Starting server on http://localhost:8080...
echo.
mvn spring-boot:run

pause
