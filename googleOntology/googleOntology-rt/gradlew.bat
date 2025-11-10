@rem Gradle Wrapper for Windows

@if "%DEBUG%" == "" @echo off
@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0

@rem Check if gradle is installed
where gradle >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: Gradle not found. Please install Gradle 8.5 or higher.
    echo.
    echo Download from: https://gradle.org/releases/
    exit /b 1
)

@rem Execute Gradle
gradle %*

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
if  not "" == "%GRADLE_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
