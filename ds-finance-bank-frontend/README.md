# DS Finance Bank - React Frontend

## 🚀 Quick Start

### Installation
```bash
npm install
```

### Development Server starten
```bash
npm start
```

Öffnet automatisch: `http://localhost:3000`

### Production Build
```bash
npm run build
```

## ⚙️ Konfiguration

### API URL anpassen
In `src/config/api.js`:
```javascript
export const API_BASE_URL = 'http://localhost:8080/ds-finance-bank-web/api';
```

### Test-Benutzer
- **Employee**: `employee1` / `employeepass`
- **Customer**: `customer1` / `customerpass`

## 📦 Technologie-Stack

- **React** 18
- **React Router** - Navigation
- **Axios** - HTTP Client
- **Material-UI** - UI Components
- **Context API** - State Management

## 📁 Struktur

```
src/
├── components/        # Wiederverwendbare Components
├── pages/            # Seiten (Employee/Customer Dashboard)
├── services/         # API Services
├── context/          # Auth Context
├── config/           # Konfiguration
└── App.js           # Haupt-App
```

## 🎯 Features

- ✅ Login (Employee & Customer)
- ✅ Customer Management (Employee)
- ✅ Stock Search
- ✅ Buy/Sell Stocks
- ✅ View Depot
- ✅ Bank Volume (Employee)
- ✅ Responsive Design
- ✅ Role-based Views

## 🔧 Scripts

```bash
npm start          # Development Server
npm run build      # Production Build
npm test           # Tests ausführen
npm run eject      # React Config anpassen (nicht rückgängig!)
```

