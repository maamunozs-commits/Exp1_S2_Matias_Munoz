$ErrorActionPreference = 'Stop'
$archivoEntorno = Join-Path (Split-Path -Parent $PSScriptRoot) '.env'
if (-not (Test-Path -LiteralPath $archivoEntorno)) { throw 'Copie .env.ejemplo como .env antes de iniciar los servicios.' }
foreach ($linea in Get-Content -LiteralPath $archivoEntorno -Encoding UTF8) {
    if ($linea.Trim() -and -not $linea.Trim().StartsWith('#')) {
        $partes = $linea.Split('=', 2)
        [Environment]::SetEnvironmentVariable($partes[0].Trim(), $partes[1].Trim(), 'Process')
    }
}
