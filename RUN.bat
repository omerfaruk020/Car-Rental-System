@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul
title Car Rental System
cd /d "%~dp0projev3"

echo.
echo  ============================================
echo            CAR RENTAL SYSTEM
echo  ============================================
echo.

rem ---------- 1) Locate a JDK (javac) ----------
set "JAVAC="

where javac >nul 2>&1
if %errorlevel%==0 set "JAVAC=javac"

if not defined JAVAC if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javac.exe" set "JAVAC=%JAVA_HOME%\bin\javac.exe"
)

if not defined JAVAC (
    for /d %%D in (
        "C:\Program Files\Eclipse Adoptium\jdk*"
        "C:\Program Files\Java\jdk*"
        "C:\Program Files\Microsoft\jdk*"
        "C:\Program Files\Amazon Corretto\jdk*"
        "C:\Program Files\Zulu\zulu*"
    ) do (
        if exist "%%~D\bin\javac.exe" set "JAVAC=%%~D\bin\javac.exe"
    )
)

if not defined JAVAC (
    echo  [ERROR] No JDK found on this computer.
    echo.
    echo  This project needs JDK 17 or newer to compile.
    echo  A Java 8 JRE is not enough: it has no compiler and the
    echo  version is too old for the language features used here.
    echo.
    echo  To fix it ^(about 2 minutes^):
    echo    1. Open  https://adoptium.net/temurin/releases/?version=21
    echo    2. Download the Windows x64 .msi installer
    echo    3. During setup, TICK "Set JAVA_HOME variable"
    echo    4. Double-click this file again
    echo.
    pause
    exit /b 1
)

rem ---------- 2) Use java.exe from the same JDK ----------
if /i "%JAVAC%"=="javac" (
    set "JAVACMD=java"
) else (
    for %%F in ("%JAVAC%") do set "JAVACMD=%%~dpFjava.exe"
)

echo  Compiler: %JAVAC%
echo  Compiling...
echo.

rem ---------- 3) Compile ----------
if not exist out mkdir out
dir /s /b src\*.java > "%TEMP%\carrental_src.txt"
"%JAVAC%" -encoding UTF-8 -d out "@%TEMP%\carrental_src.txt"

if not %errorlevel%==0 (
    echo.
    echo  [ERROR] Compilation failed. See the messages above.
    pause
    exit /b 1
)

echo  Build successful. Starting the application...
echo.

rem ---------- 4) Run ----------
rem The working directory must be projev3: the CSV paths are relative ("src/").
"%JAVACMD%" -Dfile.encoding=UTF-8 -cp out com.carrental.main.Main

echo.
echo  ============================================
echo   Application closed.
echo  ============================================
pause
