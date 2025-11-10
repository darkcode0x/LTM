@echo off
REM Video Converter - Quick Start Script
REM This script helps you verify all prerequisites before running the application

echo ========================================
echo Video Converter - Quick Start Checker
echo ========================================
echo.

REM Check 1: Java
echo [1/6] Checking Java...
java -version 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Java not found! Please install JDK 11 or higher.
    echo Download: https://adoptium.net/
    goto :error
)
echo OK: Java found
echo.

REM Check 2: Maven
echo [2/6] Checking Maven...
mvn -version 2>nul
if %errorlevel% neq 0 (
    echo WARNING: Maven not found!
    echo Install Maven or use IDE to build project
    echo Download: https://maven.apache.org/download.cgi
) else (
    echo OK: Maven found
)
echo.

REM Check 3: MySQL
echo [3/6] Checking MySQL...
mysql --version 2>nul
if %errorlevel% neq 0 (
    echo WARNING: MySQL not found in PATH
    echo Make sure MySQL Server is running!
    echo Download: https://dev.mysql.com/downloads/mysql/
) else (
    echo OK: MySQL found
)
echo.

REM Check 4: FFmpeg
echo [4/6] Checking FFmpeg...
ffmpeg -version 2>nul
if %errorlevel% neq 0 (
    echo ERROR: FFmpeg not found!
    echo Please install FFmpeg and add to PATH
    echo See FFMPEG_SETUP.md for instructions
    goto :error
)
echo OK: FFmpeg found
echo.

REM Check 5: FFprobe
echo [5/6] Checking FFprobe...
ffprobe -version 2>nul
if %errorlevel% neq 0 (
    echo ERROR: FFprobe not found!
    echo Please install FFmpeg (includes ffprobe)
    goto :error
)
echo OK: FFprobe found
echo.

REM Check 6: Project Structure
echo [6/6] Checking Project Structure...
if not exist "pom.xml" (
    echo ERROR: pom.xml not found!
    echo Run this script from project root directory
    goto :error
)
if not exist "src\main\java" (
    echo ERROR: src\main\java directory not found!
    goto :error
)
if not exist "database.sql" (
    echo ERROR: database.sql not found!
    goto :error
)
echo OK: Project structure verified
echo.

echo ========================================
echo All Prerequisites Check PASSED!
echo ========================================
echo.
echo Next Steps:
echo.
echo 1. Setup Database:
echo    - Start MySQL Server
echo    - Run: mysql -u root -p ^< database.sql
echo    - Or import database.sql via MySQL Workbench
echo.
echo 2. Build Project:
echo    - Run: mvn clean package
echo    - Or use your IDE to build
echo.
echo 3. Deploy to Tomcat:
echo    - Copy target\VideoConverter.war to Tomcat webapps\
echo    - Start Tomcat
echo.
echo 4. Access Application:
echo    - Open browser: http://localhost:8080/VideoConverter
echo.
echo Read TESTING_GUIDE.md for detailed testing instructions!
echo.
pause
goto :end

:error
echo.
echo ========================================
echo Prerequisites Check FAILED!
echo ========================================
echo.
echo Please fix the errors above and run this script again.
echo.
echo For detailed setup instructions, read:
echo - FFMPEG_SETUP.md (for FFmpeg installation)
echo - TESTING_GUIDE.md (for complete setup guide)
echo.
pause
exit /b 1

:end
exit /b 0
