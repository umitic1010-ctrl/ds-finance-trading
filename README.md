# 🏦 DS Finance Bank - REST API Project

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Jakarta EE](https://img.shields.io/badge/Jakarta%20EE-10-blue.svg)](https://jakarta.ee/)
[![WildFly](https://img.shields.io/badge/WildFly-28.0.1-red.svg)](https://www.wildfly.org/)

Eine vollständige Banking-Anwendung mit REST API, entwickelt mit Java EE / Jakarta EE auf WildFly Application Server.

## 🎯 Projekt-Übersicht

DS Finance Bank ist ein Bankensystem, das Kunden ermöglicht, Aktien zu kaufen und zu verkaufen. Die Bank nutzt einen externen Trading Service (SOAP Web Service) und verwaltet Depots für ihre Kunden.

### ✨ Features

- ✅ **REST API** mit JAX-RS
- ✅ **JPA/Hibernate** Datenpersistierung
- ✅ **EJB** Business Logic
- ✅ **Role-based Security** (Mitarbeiter & Kunden)
- ✅ **Transaktionsmanagement**
- ✅ **H2 Datenbank**
- ✅ **Web Test Client**
- ✅ **Postman Collection**

## 🚀 Quick Start

### 1. Setup ausführen
```powershell
# Automatisches Setup (empfohlen)
.\quick-setup.ps1

# Oder manuelles Setup (siehe SETUP_GUIDE.md)
```

### 2. WildFly starten
```powershell
cd C:\Programs\wildfly-28.0.1.Final-dev\bin
.\standalone.bat
```

### 3. API testen
Browser öffnen: `http://localhost:8080/ds-finance-bank-web/api-test.html`

**Test-Benutzer:**
- Employee: `employee1` / `employeepass`
- Customer: `customer1` / `customerpass`

## 📁 Projekt-Struktur

```
ds-finance-bank/
├── ds-finance-bank-common/       # DTOs und gemeinsame Klassen
├── ds-finance-bank-ejb/          # Backend (Entities, Services, REST API)
├── ds-finance-bank-web/          # Web Module (Test Client)
├── ds-finance-bank-ear/          # Enterprise Archive
├── ds-finance-bank-client/       # Optional: Desktop Client
├── ds-finance-bank-frontend/     # 🎨 React Frontend (NEU!)
│
├── ZUSAMMENFASSUNG.md            # 📝 Vollständige Projekt-Dokumentation
├── REST_API_DOKUMENTATION.md    # 📚 API Referenz
├── SETUP_GUIDE.md                # 🛠️ Setup Anleitung
├── DEPLOYMENT_CHECKLIST.md      # ✅ Test & Deployment Checkliste
│
├── quick-setup.ps1               # 🚀 Automatisches Setup
├── setup-users.ps1               # 👥 Benutzer-Setup
└── DS_Finance_Bank_API.postman_collection.json  # 📮 Postman Tests
```

## 📖 Dokumentation

| Dokument | Beschreibung |
|----------|--------------|
| **[ZUSAMMENFASSUNG.md](ZUSAMMENFASSUNG.md)** | Vollständige Projekt-Übersicht mit Architektur |
| **[REST_API_DOKUMENTATION.md](REST_API_DOKUMENTATION.md)** | API Endpoints, Requests & Responses |
| **[SETUP_GUIDE.md](SETUP_GUIDE.md)** | Schritt-für-Schritt Setup-Anleitung |
| **[DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md)** | Checkliste für Deployment & Testing |

## 🔧 Technologie-Stack

- **Backend**: Jakarta EE 10, EJB 4.0, JPA 3.1, JAX-RS 3.1
- **Server**: WildFly 28.0.1
- **Datenbank**: H2 (embedded)
- **Build**: Maven 3.x
- **Java**: JDK 17
- **Frontend**: HTML/JavaScript (Test Client)

## 🌐 REST API Endpoints

### Bank Management (Employee only)
```http
POST   /api/bank/init              # Bank initialisieren
GET    /api/bank/volume            # Aktuelles Volumen
```

### Customer Management (Employee only)
```http
POST   /api/customers              # Kunde anlegen
GET    /api/customers              # Alle Kunden
GET    /api/customers/search       # Kunden suchen
GET    /api/customers/{number}     # Einzelner Kunde
```

### Trading (Employee & Customer)
```http
GET    /api/trading/stocks/search  # Aktien suchen
GET    /api/trading/depot/{number} # Depot anzeigen
POST   /api/trading/buy            # Aktien kaufen
POST   /api/trading/sell           # Aktien verkaufen
```

**Basis-URL**: `http://localhost:8080/ds-finance-bank-web/api`

## 🔐 Security

- **Basic Authentication** mit WildFly Application Realm
- **2 Rollen**:
  - `employee` - Voller Zugriff auf alle Funktionen
  - `customer` - Nur eigene Daten und Trading

## 📊 Architektur

```
Frontend (HTML/JS)
      ↓
REST API Layer (JAX-RS)
      ↓
Business Logic (EJB)
      ↓
Persistence Layer (JPA)
      ↓
Database (H2)
```

## 🧪 Testing

### Mit Web Client
1. Öffne `http://localhost:8080/ds-finance-bank-web/api-test.html`
2. Login als `employee1` / `employeepass`
3. Teste alle Funktionen

### Mit Postman
1. Importiere `DS_Finance_Bank_API.postman_collection.json`
2. Wähle Environment oder passe baseUrl an
3. Führe Requests aus

### Mit cURL
```bash
# Bank initialisieren
curl -X POST http://localhost:8080/ds-finance-bank-web/api/bank/init \
  -u employee1:employeepass

# Kunde anlegen
curl -X POST http://localhost:8080/ds-finance-bank-web/api/customers \
  -H "Content-Type: application/json" \
  -u employee1:employeepass \
  -d '{"customerNumber":"CUST001","firstName":"Max","lastName":"Mustermann"}'

# Depot abrufen
curl -X GET http://localhost:8080/ds-finance-bank-web/api/trading/depot/CUST001 \
  -u employee1:employeepass
```

## 🛠️ Development

### Build
```bash
mvn clean install
```

### Deploy
```bash
# Kopiere .ear file ins WildFly deployments Verzeichnis
copy ds-finance-bank-ear\target\*.ear C:\Programs\wildfly-28.0.1.Final-dev\standalone\deployments\
```

### Hot Reload
WildFly erkennt automatisch neue Deployments im deployments Verzeichnis.

## ⚠️ TODO / Nächste Schritte

- [ ] **SOAP Integration**: TradingService mit echtem Web Service verbinden
  - WSDL: https://edu.dedisys.org/ds-finance/ws/TradingService?wsdl
  - WSDL in `wsdl-consumed/` ablegen
  - Maven Build → Auto-Generate Java Klassen
  
- [ ] **Frontend**: React/Angular/Vue.js entwickeln
- [ ] **Testing**: Unit & Integration Tests
- [ ] **Validation**: Bean Validation hinzufügen
- [ ] **Account Management**: Automatisches Anlegen von WildFly-Usern

## 📝 Projektanforderungen

| Anforderung | Status |
|-------------|--------|
| Kunden verwalten | ✅ |
| Aktien suchen | ✅ |
| Aktien kaufen/verkaufen | ✅ |
| Depot verwalten | ✅ |
| Bank-Volumen tracking | ✅ |
| Mitarbeiter-Client | ✅ |
| Kunden-Client | ✅ |
| Security | ✅ |
| Persistierung (JPA) | ✅ |
| Web Service Call | ⚠️ (TODO) |

## 🤝 Team

Projekt für: Distributed Systems - Finance Bank
Universität: [Deine Uni]
Semester: WS 2024/25

## 📄 Lizenz

Dieses Projekt ist nur für Lehrzwecke bestimmt.
Trading Service Daten dürfen nur im Rahmen dieser Lehrveranstaltung verwendet werden.

## 🆘 Support

Bei Problemen:
1. Prüfe [SETUP_GUIDE.md](SETUP_GUIDE.md) → Troubleshooting
2. Prüfe WildFly Logs: `WILDFLY_HOME/standalone/log/server.log`
3. Prüfe [DEPLOYMENT_CHECKLIST.md](DEPLOYMENT_CHECKLIST.md)

## 🎓 Weiterführende Links

- [WildFly Documentation](https://docs.wildfly.org/)
- [Jakarta EE Tutorial](https://jakarta.ee/learn/)
- [JAX-RS Specification](https://jakarta.ee/specifications/restful-ws/)
- [JPA Specification](https://jakarta.ee/specifications/persistence/)

---

**Viel Erfolg mit dem Projekt!** 🚀

