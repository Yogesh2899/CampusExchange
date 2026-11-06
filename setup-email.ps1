$ErrorActionPreference='Stop'
$email=(Read-Host 'Gmail/Google Workspace address that will SEND notifications').Trim()
if($email -notmatch '^[^\s@]+@[^\s@]+\.[^\s@]+$'){throw 'Enter a valid sender email address.'}
$secret=Read-Host 'Google app password (not your normal account password)' -AsSecureString
$pointer=[Runtime.InteropServices.Marshal]::SecureStringToBSTR($secret)
try {
 $plain=[Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer).Replace(' ','')
 if([string]::IsNullOrWhiteSpace($plain)){throw 'An app password is required.'}
 $normalized=ConvertTo-SecureString $plain -AsPlainText -Force
 $local=Join-Path $PSScriptRoot '.local'
 New-Item -ItemType Directory -Force $local | Out-Null
 @{Username=$email;EncryptedPassword=($normalized | ConvertFrom-SecureString)} | ConvertTo-Json | Set-Content (Join-Path $local 'mail-settings.json') -Encoding utf8
 Write-Output 'Sender saved with Windows user encryption. Restart start.ps1, then publish a new listing to test delivery.'
} finally {
 [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
 $plain=$null
}
