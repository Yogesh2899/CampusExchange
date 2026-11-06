$ErrorActionPreference='Stop'
$local=Join-Path $PSScriptRoot '.local'
$cfg=Get-Content (Join-Path $local 'mysql.json') -Raw | ConvertFrom-Json
& (Join-Path $cfg.base 'bin\mysqladmin.exe') "--defaults-extra-file=$local\root.ini" shutdown
if($LASTEXITCODE -ne 0){throw 'MySQL could not be stopped.'}
Write-Output 'Local MySQL server stopped.'
