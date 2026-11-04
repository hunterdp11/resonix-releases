$ErrorActionPreference = "Stop"

function Find-Java {
    $paths = @(
        "D:\A_Studio\jbr",
        "C:\Program Files\Android\Android Studio\jbr",
        "C:\Program Files\Android\Android Studio\jre",
        "C:\Program Files\Google\Android Studio\jbr",
        "C:\Program Files\Java\jdk*",
        "C:\Program Files\Eclipse Adoptium\jdk*"
    )

    foreach ($path in $paths) {
        $resolved = Resolve-Path $path -ErrorAction SilentlyContinue
        if ($resolved) {
            foreach ($p in $resolved.Path) {
                if (Test-Path "$p\bin\java.exe") {
                    return $p
                }
            }
        }
    }
    return $null
}

if (-not $env:JAVA_HOME) {
    Write-Host "Locating JDK..." -ForegroundColor Yellow
    $jdkPath = Find-Java
    if ($jdkPath) {
        Write-Host "Found JDK at: $jdkPath" -ForegroundColor Green
        $env:JAVA_HOME = $jdkPath
        $env:Path = "$jdkPath\bin;$env:Path"
    } else {
        Write-Host "Could not locate a valid JDK. Please set JAVA_HOME." -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "Using JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Cyan
}

Write-Host "Building Call Screener AI (Debug APK)..." -ForegroundColor Cyan
& .\gradlew.bat assembleDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host "Build Successful!" -ForegroundColor Green
    Write-Host "APK Location: app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Green

    # Attempt install if ADB device is connected
    $adbPath = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk\platform-tools\adb.exe"
    if (Test-Path $adbPath) {
        $devices = & $adbPath devices
        if ($devices -match "device\s*$") {
            Write-Host "Connected Android device detected. Installing..." -ForegroundColor Cyan
            & $adbPath install -r app\build\outputs\apk\debug\app-debug.apk
            Write-Host "Launching Call Screener..." -ForegroundColor Green
            & $adbPath shell am start -n com.callscreener.app/.ui.MainActivity
        }
    }
} else {
    Write-Host "Build failed with exit code $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}

