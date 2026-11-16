@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------
@echo off

set MAVEN_OPTS=-Xmx512m

set MVN_CMD=C:\tools\maven\bin\mvn.cmd
if exist "%MVN_CMD%" (
    "%MVN_CMD%" %*
    goto end
)

@REM Try system Maven
where mvn >nul 2>&1
if %ERRORLEVEL% == 0 (
    mvn %*
    goto end
)

echo.
echo ERROR: Maven not found.
echo Please install Maven and add it to PATH, or install to C:\tools\maven\
echo Download: https://maven.apache.org/download.cgi
echo.
exit /B 1

:end
