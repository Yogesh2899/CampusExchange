$ErrorActionPreference='Stop'
$file=Join-Path $PSScriptRoot '.local\mail-settings.json'
if(Test-Path $file){
 $settings=Get-Content $file -Raw | ConvertFrom-Json
 $secret=ConvertTo-SecureString $settings.EncryptedPassword
 $pointer=[Runtime.InteropServices.Marshal]::SecureStringToBSTR($secret)
 try {
  $env:SMTP_USERNAME=$settings.Username
  $env:SMTP_PASSWORD=[Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
  $env:SMTP_HOST='smtp.gmail.com'
  $env:SMTP_PORT='587'
  $env:EMAIL_NOTIFICATIONS_ENABLED='true'
 } finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer) }
 Write-Output 'Listing confirmation emails enabled. Sender connection will be checked on delivery.'
}
