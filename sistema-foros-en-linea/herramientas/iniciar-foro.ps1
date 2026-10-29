$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/cargar-entorno.ps1"
$archivoAplicacion = Join-Path (Split-Path -Parent $PSScriptRoot) 'servicio-foro/target/servicio-foro-1.0.0.jar'
& "$env:JAVA_HOME/bin/java.exe" -jar $archivoAplicacion
exit $LASTEXITCODE
