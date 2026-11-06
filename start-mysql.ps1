$ErrorActionPreference='Stop'
$local=Join-Path $PSScriptRoot '.local'
$cfg=Get-Content (Join-Path $local 'mysql.json') -Raw | ConvertFrom-Json
$mysql=Join-Path $cfg.base 'bin\mysql.exe'
if(!(Test-Path $mysql)){throw 'The local MySQL runtime is missing. Keep the project workspace and work/mysql folder together.'}
$client=Join-Path $local 'client.ini'
@"
[client]
user=$($cfg.user)
password=$($cfg.password)
host=127.0.0.1
port=$($cfg.port)
protocol=tcp
connect-timeout=2
"@ | Set-Content $client -Encoding ascii
function Test-MySqlReady {
 # Capture native stderr directly: PowerShell 5.1 otherwise turns a normal
 # connection-refused response into a terminating NativeCommandError.
 $info=New-Object System.Diagnostics.ProcessStartInfo
 $info.FileName=$mysql
 $info.Arguments="--defaults-extra-file=`"$client`" --execute=`"SELECT 1`" --silent"
 $info.UseShellExecute=$false
 $info.CreateNoWindow=$true
 $info.RedirectStandardOutput=$true
 $info.RedirectStandardError=$true
 $probe=New-Object System.Diagnostics.Process
 $probe.StartInfo=$info
 try {
  $null=$probe.Start()
  if(!$probe.WaitForExit(5000)) { $probe.Kill(); return $false }
  $null=$probe.StandardOutput.ReadToEnd()
  $null=$probe.StandardError.ReadToEnd()
  return $probe.ExitCode -eq 0
 } finally { $probe.Dispose() }
}
if(!(Test-MySqlReady)){
 Write-Output 'Starting the local MySQL server...'
 Start-Process (Join-Path $cfg.base 'bin\mysqld.exe') -ArgumentList "--defaults-file=`"$local\my.ini`"" -WindowStyle Hidden | Out-Null
 $ready=$false
 for($attempt=0;$attempt -lt 30;$attempt++){
  if(Test-MySqlReady){$ready=$true;break}
  Start-Sleep -Milliseconds 500
 }
 if(!$ready){throw 'MySQL failed to start. Check work/mysql/mysql.log.'}
}
$env:DB_URL="jdbc:mysql://127.0.0.1:$($cfg.port)/campusexchange"
$env:DB_USER=$cfg.user
$env:DB_PASSWORD=$cfg.password
$env:SPRING_PROFILES_ACTIVE='mysql'
Write-Output 'Using persistent MySQL storage on local port 3307.'
