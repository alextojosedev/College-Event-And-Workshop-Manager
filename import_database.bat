@echo off
setlocal enabledelayedexpansion

echo ====================================================
echo Importing Database Schema and Sample Data...
echo ====================================================

:: 1. Check if mysql is in PATH
where mysql >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set "MYSQL_CMD=mysql"
    goto RUN_IMPORT
)

:: 2. Check XAMPP location
if exist "C:\xampp\mysql\bin\mysql.exe" (
    set "MYSQL_CMD=C:\xampp\mysql\bin\mysql.exe"
    goto RUN_IMPORT
)

:: 3. Check Program Files MySQL Server
for /d %%D in ("C:\Program Files\MySQL\MySQL Server *") do (
    if exist "%%D\bin\mysql.exe" (
        set "MYSQL_CMD=%%D\bin\mysql.exe"
        goto RUN_IMPORT
    )
)

for /d %%D in ("C:\Program Files (x86)\MySQL\MySQL Server *") do (
    if exist "%%D\bin\mysql.exe" (
        set "MYSQL_CMD=%%D\bin\mysql.exe"
        goto RUN_IMPORT
    )
)

:: 4. Check WAMP or Laragon
for /d %%D in ("C:\wamp64\bin\mysql\mysql*") do (
    if exist "%%D\bin\mysql.exe" (
        set "MYSQL_CMD=%%D\bin\mysql.exe"
        goto RUN_IMPORT
    )
)

if exist "C:\laragon\bin\mysql\current\bin\mysql.exe" (
    set "MYSQL_CMD=C:\laragon\bin\mysql\current\bin\mysql.exe"
    goto RUN_IMPORT
)

echo [WARNING] mysql command was not found in PATH or standard installation folders.
echo You can import db\setup_database.sql via phpMyAdmin or MySQL Workbench,
echo OR use the built-in "Initialize Tables" button directly inside the app GUI!
echo.
pause
exit /b 1

:RUN_IMPORT
echo Found MySQL at: "!MYSQL_CMD!"
echo Enter MySQL password if prompted (press ENTER if root has no password):
"!MYSQL_CMD!" -u root -p < "%~dp0db\setup_database.sql"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ====================================================
    echo [SUCCESS] Database 'college_event_db' and tables imported successfully!
    echo ====================================================
) else (
    echo.
    echo [ERROR] Import failed. Please verify MySQL service is running.
)

echo.
pause
