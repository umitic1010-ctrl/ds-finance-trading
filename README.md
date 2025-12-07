# DS Finance Bank – Architekturplan

## 1. Ziel Architektur
Der Bankserver gibt mehrschichtige Jakarta-EE-Anwendung in WildFly. Gibt Kundendepots, die verbinfdung zu TradingService-Webservice und Remote-Schnittstellen für customer/employee-clients. 

## 2. Gesamtarchitektur
- **Präsentationsschicht:** Zwei getrennte Java-Clients (Mitarbeiter, Kunde) auf Basis `ds-finance-bank-client`. Beide nutzen identische Fachlogik über gemeinsame Services, unterscheiden sich aber in Authentisierung und Funktionsumfang.
- **Anwendungsschicht:** `ds-finance-bank-ejb` bündelt Geschäftslogik als Stateful/Stateless EJBs. Pro Use-Case-Gruppe werden Remote-Fassaden angeboten: `EmployeeBankingRemote` und `CustomerBankingRemote`.
- **Integrationsschicht:** Ein Adapter kapselt den JAX-WS Zugriff auf das TradingService-WSDL. Authentifizierungsdaten liegen verschlüsselt in der Serverkonfiguration.
- **Persistenzschicht:** JPA/Hibernate mit PostgreSQL zur Speicherung von Kunden, Depots, Trades sowie der bankweiten Volumensumme.
- **Verteilung:** Alle serverseitigen Module werden über `ds-finance-bank-ear` als EAR auf WildFly deployt. Clients greifen via `jboss-ejb-client` auf die Remote-Interfaces zu.

## 3. Technologiewahl
- **Application Server:** WildFly 29, Jakarta EE 9.1 (CDI, EJB, JPA, Bean Validation, JAX-WS).
- **Persistenz:** PostgreSQL 15 + JPA (Hibernate). Schema-Änderungen via Maven-Skripte/DDL.
- **SOAP-Client:** Apache CXF Codegen; gesicherter Credential Store in WildFly.
- **Sicherheit:** Container-Managed Security mit zwei Rollentypen (`EMPLOYEE`, `CUSTOMER`).
- **Build/Tooling:** Maven Multi-Module, Surefire Tests, `wildfly-maven-plugin` zum Deploy.

## 4. Modulverantwortung
| Modul | Verantwortung |
| --- | --- |
| `ds-finance-bank-common` | Gemeinsame DTOs, Exceptions, Utility-Klassen (Geldbeträge, Trade-Typen). |
| `ds-finance-bank-ejb` | Entitäten, Repositories, Fachservices, Remote-Schnittstellen, TradingService-Adapter. |
| `ds-finance-bank-web` | Optionale REST-Fassade für spätere Integrationen (intern genutzt für Tests/Monitoring). |
| `ds-finance-bank-client` | Swing/CLI-Clients für Mitarbeiter und Kunden mit Authentifizierung und Workflows. |
| `ds-finance-bank-ear` | Packaging und Konfiguration (Datasources, Security-Domains, EJB-Deployment). |

## 5. Datenmodell
### Tabellenentwurf
- `customer` (id, customer_number, first_name, last_name, address, created_at, status).
- `account_user` (id, login_name, hashed_password, role, customer_id nullable).
- `stock` (symbol PK, name, currency).
- `depot_holding` (id, customer_id, stock_symbol, quantity DECIMAL(18,4), average_price DECIMAL(18,4)).
- `trade_history` (id, customer_id, stock_symbol, side, quantity DECIMAL(18,4), price DECIMAL(18,4), executed_at, trading_reference, status).
- `bank_volume_ledger` (id, delta_amount DECIMAL(18,2), balance_after DECIMAL(18,2), reason, created_at).

### Regeln
- Kundennummer wird über separate Sequenz generiert und ist eindeutig.
- `depot_holding` nutzt optimistisches Locking, um parallele Trades abzusichern.
- Jeder erfolgreiche Kauf/Verkauf erzeugt zwei Transaktionen: Depot-Anpassung und Volumenbuchung.

## 6. Serviceschicht
- **CustomerService:** CRUD für Kunden, Suche per Nummer oder Name (Mitarbeiter-Use-Case).
- **PortfolioService:** Liest Depotbestand, reichert ihn mit aktuellen Kursen an, berechnet Gesamtwert.
- **TradingServiceFacade:** Einziger Kontaktpunkt zur Börse. Validiert Limits, führt SOAP-Call aus, verarbeitet Antwort.
- **VolumeService:** Verwaltet die investierbare Summe der Bank und stellt Historie bereit.
- **StockCatalogService:** Synchronisiert lokale Aktienliste mit dem TradingService-Angebot.

## 7. Remote-Fassaden
```text
EmployeeBankingRemote
	+ createCustomer(CustomerDTO)
	+ searchCustomers(CustomerSearchCriteria)
	+ listStocks()
	+ placeOrder(customerId, TradeRequest)
	+ getPortfolio(customerId)
	+ getBankVolume()

CustomerBankingRemote
	+ listStocks()
	+ placeOwnOrder(TradeRequest)
	+ getOwnPortfolio()
	+ getOrderHistory()
```
Beide Fassaden delegieren auf dieselben Services, wenden jedoch zusätzliche Sicherheitsprüfungen an (Kunde darf nur auf eigene Daten zugreifen).

## 8. Sicherheitskonzept
- Authentisierung erfolgt containerseitig (Database Realm). Clients übermitteln Benutzername/Passwort beim Aufbau des EJB-Contexts.
- Autorisierung über `@RolesAllowed`. Methoden im Kunden-Facade prüfen zusätzlich die Kundennummer aus dem Principal gegen die Ziel-ID.
- Mitarbeiterrollen dürfen Kunden kontextuell impersonieren, bleiben aber auditierbar (jede Aktion wird mit Mitarbeiter-ID protokolliert).
- Netzwerkabsicherung mittels TLS (WildFly HTTPS + EJB over HTTPS/IIOP-TLS).

## 9. SOAP-Integration mit TradingService
- Codegenerierung erfolgt im Maven-Build (`cxf-codegen-plugin`).
- Adapter `TradingGateway` kapselt Authentifizierung (HTTP Basic). Konfiguration liegt in `META-INF/trading.properties` und wird per `@Resource` geladen.
- Zeitkritische Operationen (Quote Abruf) werden gecached, damit Depotabfragen performant bleiben. Cache-Invalidierung bei Orderabschluss.
- Fehlerfälle (z. B. unzureichendes Volumen) werden in fachliche Exceptions übersetzt und bis zum Client durchgereicht.

## 10. Client-Implementierung
- **Mitarbeiter-Client:** Desktop-GUI (Swing). Szenarien: Kunde anlegen, Kundensuche, Depotansicht, Ordermaske, Volumenübersicht.
- **Kunden-Client:** Reduzierte Oberfläche (eigene Depotansicht, Orderplatzierung, Orderhistorie). Login direkt mit Kundenzugang.
- Gemeinsame Basiskomponenten (`common-client` Package) kapseln Verbindungsaufbau, DTO-Mapping und Fehlerbehandlung.

## 11. Ablauf Buy/Sell
1. Client ruft Remote-Methode mit `TradeRequest`.
2. `TradingServiceFacade` validiert Parameter, prüft Depot und Volumen.
3. SOAP-Call an TradingService; bei Erfolg werden Depot und Volumen in einer Transaktion aktualisiert.
4. Ergebnis (Durchschnittspreis, Status) wird in `trade_history` protokolliert und dem Client zurückgegeben.
5. Bei Fehlern erfolgt Rollback und es wird eine fachliche Fehlermeldung geliefert.

## 12. Umsetzungsschritte
1. **Basis-Setup:** Datenbank einrichten, Entities + Repositories erstellen, grundlegende EJB-Konfiguration.
2. **Kundendienste:** CustomerService implementieren, Remote-Fassade für Mitarbeiter aufbauen.
3. **Depot & Trading:** Holdings, Volumenverwaltung, SOAP-Adapter und Order-Workflows umsetzen.
4. **Kunden-Fassade:** Authentisierung/Konto-Verknüpfung für Kunden, Schutz vor Fremdzugriffen.
5. **Clients:** Mitarbeiter-Client zuerst, anschließend Kunden-Client, jeweils mit Integrationstests gegen Test-Server.
6. **Abschluss:** Lasttests (Volumenkorrektheit), Fehlerbehandlung, Dokumentation aktualisieren.

## 13. Offene Fragen
- Welche Daten liefert das TradingService exakt (Preis, verfügbare Menge, Währung)?
- Müssen historische Kurse gespeichert werden oder reicht On-Demand-Abruf?
- Benötigt der Kunden-Client Offline-Fähigkeiten (z. B. Order-Entwürfe)?
