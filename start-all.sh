#!/bin/bash

# DS Finance Bank - Complete Startup Script
# Startet Backend (WildFly) und Frontend (React) in separaten Terminals

echo "=========================================="
echo "DS Finance Bank - Starting All Services"
echo "=========================================="

# Prüfen ob WildFly bereits läuft
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo "⚠️  Backend läuft bereits auf Port 8080"
else
    echo "🚀 Starting Backend (WildFly)..."
    osascript -e 'tell application "Terminal" to do script "cd '"$PWD"' && ./start-backend.sh"'
    echo "✓ Backend Terminal geöffnet"
fi

# Kurz warten
sleep 2

# Prüfen ob Frontend bereits läuft
if lsof -Pi :3000 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo "⚠️  Frontend läuft bereits auf Port 3000"
else
    echo "🚀 Starting Frontend (React)..."
    osascript -e 'tell application "Terminal" to do script "cd '"$PWD"' && ./start-frontend.sh"'
    echo "✓ Frontend Terminal geöffnet"
fi

echo ""
echo "=========================================="
echo "✅ Services werden gestartet!"
echo "=========================================="
echo ""
echo "📍 Backend:  http://localhost:8080/ds-finance-bank-web"
echo "📍 Frontend: http://localhost:3000"
echo ""
echo "🔐 Login Credentials:"
echo "   Kunde:      example_c@banking.de / customerpass"
echo "   Mitarbeiter: example_e@banking.de / employeepass"
echo ""
echo "⏱️  Warte 10 Sekunden bis Services bereit sind..."
sleep 10

echo ""
echo "🌐 Öffne Frontend im Browser..."
open http://localhost:3000

echo ""
echo "✅ Fertig! Viel Erfolg!"
