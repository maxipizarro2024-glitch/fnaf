@echo off
REM Requiere JDK 17 y Gradle instalados y en el PATH
call gradle wrapper --gradle-version 8.1.1
call gradlew.bat build
echo.
echo Listo: tu mod esta en build\libs\ (usa el .jar SIN "-sources")
pause
