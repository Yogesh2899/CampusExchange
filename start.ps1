$ErrorActionPreference = 'Stop'
if(Test-Path (Join-Path $PSScriptRoot '.local\mysql.json')) { & (Join-Path $PSScriptRoot 'start-mysql.ps1') }
& (Join-Path $PSScriptRoot 'load-email-settings.ps1')
$javaPath = if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) { "$env:JAVA_HOME\bin\java.exe" } else {
    $installedJdk = Get-ChildItem 'C:\Program Files\Java\jdk*' -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
    if ($installedJdk) { Join-Path $installedJdk.FullName 'bin\java.exe' } else { 'java' }
}
$jarPath = Join-Path $PSScriptRoot 'CampusExchange-email.jar'
if (-not (Test-Path $jarPath)) { throw 'Build the project with Maven and copy target\campusexchange-0.1.0.jar to CampusExchange-email.jar first.' }
& $javaPath -jar $jarPath @args










