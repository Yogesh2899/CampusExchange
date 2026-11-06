param([string]$Email)
$ErrorActionPreference='Stop'
if(!$Email){$Email=Read-Host 'Email of your registered CampusExchange account'}
$Email=$Email.Trim()
if($Email -notmatch '@thapar\.edu$' -or $Email.Length -gt 160 -or $Email -notmatch '^[a-zA-Z0-9._+%\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$'){throw 'Enter a registered @thapar.edu college email address.'}
& (Join-Path $PSScriptRoot 'start-mysql.ps1')
$local=Join-Path $PSScriptRoot '.local'
$cfg=Get-Content (Join-Path $local 'mysql.json') -Raw | ConvertFrom-Json
$query="UPDATE campusexchange.users SET role='ADMIN' WHERE email='$Email'; SELECT COUNT(*) FROM campusexchange.users WHERE email='$Email' AND role='ADMIN';"
$result=& (Join-Path $cfg.base 'bin\mysql.exe') "--defaults-extra-file=$local\root.ini" --batch --skip-column-names "--execute=$query"
if($LASTEXITCODE -ne 0 -or $result -ne '1'){throw 'No matching account found. Register in the app first.'}
Write-Output 'Admin role assigned. Sign out and sign back in to access the Admin link.'

