#!/bin/bash

# DS Finance Bank - Backend Startup Script
# Startet WildFly mit Java 17

echo "=========================================="
echo "DS Finance Bank - Starting Backend"
echo "=========================================="

# Java 17 setzen (WildFly 28 benötigt Java 17)
export JAVA_HOME="/Users/urosmitic/Library/Java/JavaVirtualMachines/ms-17.0.16/Contents/Home"

echo "✓ Java Version:"
java -version

echo ""
echo "Starting WildFly Application Server..."
echo "Backend will be available at: http://localhost:8080/ds-finance-bank-web"
echo ""
echo "Press Ctrl+C to stop"
echo "=========================================="

# WildFly starten
"/Users/urosmitic/Downloads/wildfly-28.0.1.Final 2/bin/standalone.sh"
