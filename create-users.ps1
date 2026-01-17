# DS Finance Bank - User Setup Script
# Erstellt automatisch Test-User für die Anwendung

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "DS Finance Bank - User Setup" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""

$WILDFLY_HOME = "D:\VERTEILTE SYSTEME\wildfly-28.0.1.Final"

if (-not (Test-Path $WILDFLY_HOME)) {
    Write-Host "ERROR: WildFly nicht gefunden!" -ForegroundColor Red
    Write-Host "Pfad: $WILDFLY_HOME" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Bitte passe den Pfad an:" -ForegroundColor Yellow
    $WILDFLY_HOME = Read-Host "WildFly Pfad"

    if (-not (Test-Path $WILDFLY_HOME)) {
        Write-Host "ERROR: Pfad existiert nicht!" -ForegroundColor Red
        Read-Host "Enter zum Beenden"
        exit 1
    }
}

$ADD_USER_BAT = "$WILDFLY_HOME\bin\add-user.bat"

if (-not (Test-Path $ADD_USER_BAT)) {
    Write-Host "ERROR: add-user.bat nicht gefunden!" -ForegroundColor Red
    Read-Host "Enter zum Beenden"
    exit 1
}

Write-Host "OK WildFly gefunden: $WILDFLY_HOME" -ForegroundColor Green
Write-Host ""

# User Definitionen
$users = @(
    @{
        Username = "employee1"
        Password = "employeepass"
        Role = "employee"
        Description = "Mitarbeiter mit vollem Zugriff"
    },
    @{
        Username = "test"
        Password = "test"
        Role = "employee"
        Description = "Test-Employee"
    },
    @{
        Username = "customer1"
        Password = "customerpass"
        Role = "customer"
        Description = "Kunde mit eingeschränktem Zugriff"
    },
    @{
        Username = "admin"
        Password = "admin"
        Role = "employee"
        Description = "Admin-Zugriff"
    }
)

Write-Host "Folgende User werden angelegt:" -ForegroundColor Cyan
Write-Host ""

foreach ($user in $users) {
    Write-Host "  • $($user.Username)" -NoNewline -ForegroundColor White
    Write-Host " / $($user.Password)" -NoNewline -ForegroundColor Gray
    Write-Host " [$($user.Role)]" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Fortfahren? (j/n)" -ForegroundColor Yellow
$answer = Read-Host

if ($answer -ne "j" -and $answer -ne "J") {
    Write-Host "Abgebrochen." -ForegroundColor Yellow
    Read-Host "Enter zum Beenden"
    exit 0
}

Write-Host ""
Write-Host "Lege User an..." -ForegroundColor Cyan
Write-Host ""

foreach ($user in $users) {
    Write-Host "Creating user: $($user.Username)..." -ForegroundColor White

    # add-user.bat im batch mode aufrufen
    $env:JAVA_OPTS = "-Djboss.aesh.disableCompletion=true"

    # Erstelle Eingabe-String
    $input = "a`n$($user.Username)`n$($user.Password)`n$($user.Password)`n$($user.Role)`nyes`n"

    try {
        # Führe add-user.bat aus mit Pipe-Input
        $process = Start-Process -FilePath $ADD_USER_BAT `
            -WorkingDirectory "$WILDFLY_HOME\bin" `
            -NoNewWindow `
            -Wait `
            -PassThru `
            -RedirectStandardInput ([System.IO.Path]::GetTempFileName()) `
            -RedirectStandardOutput ([System.IO.Path]::GetTempFileName()) `
            -RedirectStandardError ([System.IO.Path]::GetTempFileName())

        # Alternative: Direkte Manipulation der Properties-Dateien
        $usersFile = "$WILDFLY_HOME\standalone\configuration\application-users.properties"
        $rolesFile = "$WILDFLY_HOME\standalone\configuration\application-roles.properties"

        # Prüfe ob User bereits existiert
        if (Test-Path $usersFile) {
            $existingUsers = Get-Content $usersFile
            if ($existingUsers -match "^$($user.Username)=") {
                Write-Host "  -> User existiert bereits, ueberspringe..." -ForegroundColor Yellow
                continue
            }
        }

        # Hash das Password (MD5 Hash von username:ApplicationRealm:password)
        $realm = "ApplicationRealm"
        # Use format operator to avoid parsing issues with ':' inside double-quoted interpolation
        $toHash = "{0}:{1}:{2}" -f $user.Username, $realm, $user.Password
        $md5 = [System.Security.Cryptography.MD5]::Create()
        $hash = $md5.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($toHash))
        $hashString = [System.BitConverter]::ToString($hash).Replace("-", "").ToLower()

        # Füge zu Properties-Dateien hinzu
        Add-Content -Path $usersFile -Value "$($user.Username)=$hashString"
        Add-Content -Path $rolesFile -Value "$($user.Username)=$($user.Role)"

        Write-Host "  OK $($user.Username) angelegt!" -ForegroundColor Green

    } catch {
        Write-Host "  ✗ Fehler bei $($user.Username): $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "===============================================" -ForegroundColor Green
Write-Host "✓ User Setup abgeschlossen!" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Green
Write-Host ""

Write-Host "Angelegte User:" -ForegroundColor Cyan
foreach ($user in $users) {
    Write-Host "  • Username: " -NoNewline -ForegroundColor White
    Write-Host "$($user.Username)" -NoNewline -ForegroundColor Yellow
    Write-Host " / Password: " -NoNewline -ForegroundColor White
    Write-Host "$($user.Password)" -NoNewline -ForegroundColor Yellow
    Write-Host " / Role: " -NoNewline -ForegroundColor White
    Write-Host "$($user.Role)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "WICHTIG:" -ForegroundColor Red
Write-Host "Starte WildFly NEU, damit die User aktiv werden!" -ForegroundColor Yellow
Write-Host ""

Read-Host "Enter zum Beenden"
