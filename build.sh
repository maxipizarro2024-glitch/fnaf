#!/bin/sh
# Requiere JDK 17 y Gradle instalados
gradle wrapper --gradle-version 8.1.1 && ./gradlew build
echo "Listo: tu mod esta en build/libs/"
