# 🎨 React Frontend Setup

## Vollständiges React Frontend für DS Finance Bank!

### ✅ Was wurde erstellt:

**16 Dateien** für ein vollständiges React Frontend:

1. **package.json** - Dependencies & Scripts
2. **src/App.js** - Haupt-App mit Routing
3. **src/index.js** - Entry Point
4. **src/config/api.js** - API Konfiguration
5. **src/services/apiClient.js** - HTTP Client mit Auth
6. **src/services/api.js** - API Service Functions
7. **src/context/AuthContext.js** - Authentication State
8. **src/pages/Login.js** - Login-Seite
9. **src/pages/EmployeeDashboard.js** - Mitarbeiter Dashboard
10. **src/pages/CustomerDashboard.js** - Kunden Dashboard
11. **src/components/Layout.js** - App Layout
12. **src/components/CustomerManagement.js** - Kundenverwaltung
13. **src/components/TradingPanel.js** - Trading Interface
14. **public/index.html** - HTML Template
15. **.gitignore** - Git Ignore
16. **README.md** - Frontend Doku

---

## 🚀 Installation & Start

### 1. Node.js & npm installieren
Falls noch nicht installiert: https://nodejs.org/ (LTS Version)

### 2. Dependencies installieren
```powershell
cd ds-finance-bank-frontend
npm install
```

### 3. Backend starten
```powershell
# In anderem Terminal:
cd C:\Programs\wildfly-28.0.1.Final-dev\bin
.\standalone.bat
```

Warte bis WildFly vollständig gestartet ist!

### 4. Frontend starten
```powershell
# Im Frontend-Verzeichnis:
npm start
```

Browser öffnet automatisch: `http://localhost:3000`

---

## 🎯 Features

### ✅ **Login-System**
- Employee & Customer Login
- Role-based Routing
- Session Management (localStorage)
- Auto-Redirect bei 401

### ✅ **Employee Dashboard**
- Bank-Volumen anzeigen
- Bank initialisieren
- Kundenverwaltung:
  - Neuen Kunden anlegen
  - Kunden suchen (nach Name)
  - Alle Kunden auflisten
- Trading Panel:
  - Aktien suchen
  - Für Kunden kaufen/verkaufen
  - Depot eines Kunden anzeigen

### ✅ **Customer Dashboard**
- Aktien suchen
- Eigene Aktien kaufen/verkaufen
- Eigenes Depot anzeigen mit:
  - Alle Positionen
  - Aktueller Wert pro Position
  - Gesamtwert des Depots

### ✅ **Design**
- Material-UI (moderne Google-Design-Sprache)
- Responsive (funktioniert auf Desktop & Mobile)
- Professionelle UI Components
- Klare Struktur & Navigation

---

## 📁 Projekt-Struktur

```
ds-finance-bank-frontend/
├── public/
│   └── index.html              # HTML Template
├── src/
│   ├── components/
│   │   ├── Layout.js           # App Layout mit Navigation
│   │   ├── CustomerManagement.js  # Kundenverwaltung
│   │   └── TradingPanel.js     # Trading Interface
│   ├── pages/
│   │   ├── Login.js            # Login-Seite
│   │   ├── EmployeeDashboard.js   # Mitarbeiter-Ansicht
│   │   └── CustomerDashboard.js   # Kunden-Ansicht
│   ├── services/
│   │   ├── apiClient.js        # HTTP Client (Axios)
│   │   └── api.js              # API Service Functions
│   ├── context/
│   │   └── AuthContext.js      # Auth State Management
│   ├── config/
│   │   └── api.js              # API URLs & Endpoints
│   ├── App.js                  # Main App Component
│   └── index.js                # Entry Point
├── package.json                # Dependencies
└── README.md                   # Doku
```

---

## 🔧 Konfiguration

### API URL anpassen
Falls Backend auf anderem Port läuft:

**`src/config/api.js`:**
```javascript
export const API_BASE_URL = 'http://localhost:8080/ds-finance-bank-web/api';
```

### CORS aktivieren
Im Backend ist CORS bereits aktiviert in:
`ds-finance-bank-ejb/.../rest/CorsFilter.java`

Falls Probleme: Prüfe ob `@Provider` Annotation aktiv ist.

---

## 🎨 Screenshots

### Login-Seite
- Elegantes Design mit Bank-Icon
- Role-Auswahl (Employee/Customer)
- Test-Benutzer angezeigt

### Employee Dashboard
- Übersicht-Karten (Bank-Volumen, Kunden, Trading)
- Tabs: Kundenverwaltung | Trading
- Vollständige Kundenverwaltung
- Trading für beliebige Kunden

### Customer Dashboard
- Persönlicher Gruß
- Eigenes Trading-Panel
- Depot-Übersicht

---

## 📦 Verwendete Technologien

| Technologie | Version | Zweck |
|------------|---------|-------|
| React | 18.2 | UI Framework |
| React Router | 6.15 | Navigation & Routing |
| Material-UI | 5.14 | UI Components |
| Axios | 1.5 | HTTP Client |
| Context API | - | State Management |

---

## 🔐 Security

### Authentication
- Basic Auth über HTTP Headers
- Credentials in localStorage gespeichert
- Automatisches Logout bei 401

### Authorization
- Role-based Routing (Employee/Customer)
- Protected Routes mit PrivateRoute Component
- Customer kann nur eigene Daten sehen

---

## 🛠️ Development

### Scripts
```bash
npm start          # Dev Server (Port 3000)
npm run build      # Production Build
npm test           # Tests
```

### Hot Reload
Änderungen werden automatisch im Browser aktualisiert.

### Production Build
```bash
npm run build

# Build-Output in /build Verzeichnis
# Kann auf jedem Webserver deployed werden
```

---

## 🚨 Troubleshooting

### Problem: npm Fehler beim Installieren
**Lösung**: Node.js aktualisieren, Cache löschen:
```bash
npm cache clean --force
npm install
```

### Problem: "CORS Error" im Browser
**Lösung**: 
1. Prüfe ob Backend läuft
2. Prüfe `CorsFilter.java` - `@Provider` aktiv?
3. Backend neu starten nach CORS-Änderung

### Problem: "401 Unauthorized"
**Lösung**:
1. Prüfe WildFly User (employee1/customer1)
2. Prüfe Login-Daten im Frontend
3. Lösche localStorage und logge neu ein

### Problem: "Connection Refused"
**Lösung**:
1. Ist Backend gestartet?
2. Läuft auf Port 8080?
3. Prüfe API_BASE_URL in `src/config/api.js`

---

## 🎯 Nächste Schritte

### Optional erweitern:
1. **Charts** - Depot-Wert Verlauf (Chart.js)
2. **Real-time Updates** - WebSockets für Live-Kurse
3. **Dashboard Stats** - Mehr Statistiken
4. **Transaction History** - Historie der Trades
5. **Dark Mode** - Theme-Switcher

---

## ✅ Checkliste Frontend-Start

- [ ] Node.js installiert
- [ ] Backend (WildFly) läuft
- [ ] CORS aktiviert im Backend
- [ ] `npm install` ausgeführt
- [ ] `npm start` ausgeführt
- [ ] Browser öffnet `http://localhost:3000`
- [ ] Login funktioniert
- [ ] Employee Dashboard zeigt Daten
- [ ] Customer Dashboard zeigt Daten

---

## 🎊 Fertig!

Du hast jetzt ein **vollständiges, modernes React Frontend** für die DS Finance Bank!

**Features:**
- ✅ Professionelles Design (Material-UI)
- ✅ Role-based Access
- ✅ Kundenverwaltung
- ✅ Trading System
- ✅ Depot-Verwaltung
- ✅ Responsive Design

**Nächster Schritt:** `npm install && npm start` 🚀

