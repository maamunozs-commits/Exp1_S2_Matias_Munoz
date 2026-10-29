$ErrorActionPreference = 'Stop'
$raizProyecto = Split-Path -Parent $PSScriptRoot
$versionMaven = '3.9.12'
$directorioMaven = Join-Path $env:USERPROFILE ".m2/foro-academico/apache-maven-$versionMaven"
$ejecutableMaven = Join-Path $directorioMaven 'bin/mvn.cmd'
if (-not (Test-Path -LiteralPath $ejecutableMaven)) {
    $directorioDescarga = Split-Path -Parent $directorioMaven
    New-Item -ItemType Directory -Path $directorioDescarga -Force | Out-Null
    $archivoMaven = Join-Path $directorioDescarga 'maven.zip'
    $direccionMaven = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$versionMaven/apache-maven-$versionMaven-bin.zip"
    Invoke-WebRequest -Uri $direccionMaven -OutFile $archivoMaven -UseBasicParsing
    $huellaEsperada = (Invoke-WebRequest -Uri "$direccionMaven.sha512" -UseBasicParsing).Content.Trim().Split(' ')[0]
    $algoritmoHuella = [System.Security.Cryptography.SHA512]::Create()
    $flujoArchivo = [System.IO.File]::OpenRead($archivoMaven)
    try { $huellaCalculada = [BitConverter]::ToString($algoritmoHuella.ComputeHash($flujoArchivo)).Replace('-', '') }
    finally { $flujoArchivo.Dispose(); $algoritmoHuella.Dispose() }
    if ($huellaCalculada -ine $huellaEsperada) { throw 'La verificación de integridad de Maven no fue satisfactoria.' }
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    [System.IO.Compression.ZipFile]::ExtractToDirectory($archivoMaven, $directorioDescarga)
}
Push-Location $raizProyecto
try {
    & $ejecutableMaven @args
    $codigoSalida = $LASTEXITCODE
} finally { Pop-Location }
exit $codigoSalida
