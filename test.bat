@echo off
echo ====================================================
echo Running Automated Test Suite...
echo ====================================================

java -cp "bin;lib/*" com.college.eventmanager.AppTestSuite
