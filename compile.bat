@echo off
echo ====================================================
echo Compiling College Event and Workshop Manager...
echo ====================================================

if not exist bin mkdir bin

dir /s /B src\main\java\*.java > sources.txt
dir /s /B src\test\java\*.java >> sources.txt

javac -encoding UTF-8 -cp "lib/*" -d bin @sources.txt
del sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    exit /b %ERRORLEVEL%
)

xcopy /E /I /Y src\main\resources bin > nul
echo [SUCCESS] Compilation completed successfully into bin/
