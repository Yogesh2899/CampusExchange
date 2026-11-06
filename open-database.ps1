$ErrorActionPreference='Stop'
& (Join-Path $PSScriptRoot 'start-mysql.ps1')
$local=Join-Path $PSScriptRoot '.local'
$cfg=Get-Content (Join-Path $local 'mysql.json') -Raw | ConvertFrom-Json
$mysql=Join-Path $cfg.base 'bin\mysql.exe'
Write-Output 'Opening the application database: campusexchange'
Write-Output 'Try: SHOW TABLES; or DESCRIBE items;'
Write-Output 'Type exit; to return to PowerShell.'
& $mysql "--defaults-extra-file=$local\client.ini" --database=campusexchange --table
