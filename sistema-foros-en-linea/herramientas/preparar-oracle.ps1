param([string]$Contenedor = 'XEPDB1')
$ErrorActionPreference = 'Stop'
. "$PSScriptRoot/cargar-entorno.ps1"
$env:NLS_LANG = 'AMERICAN_AMERICA.AL32UTF8'
$directorioCodigo = Split-Path -Parent $PSScriptRoot
$directorioSql = Join-Path (Split-Path -Parent $directorioCodigo) 'scripts'
& sqlplus -L -s '/ as sysdba' "@$directorioSql/00_crear_esquemas.sql" $env:ORACLE_CLAVE_USUARIOS $env:ORACLE_CLAVE_FORO $Contenedor
if ($LASTEXITCODE -ne 0) { throw 'No se completó la creación de los esquemas.' }
$destinoOracle = $env:ORACLE_URL -replace '^jdbc:oracle:thin:@', ''
& sqlplus -L -s "MM_USUARIOS/$($env:ORACLE_CLAVE_USUARIOS)@$destinoOracle" "@$directorioSql/01_usuarios.sql"
if ($LASTEXITCODE -ne 0) { throw 'No se completó la creación de las tablas de usuarios.' }
& sqlplus -L -s "MM_FORO/$($env:ORACLE_CLAVE_FORO)@$destinoOracle" "@$directorioSql/02_foro.sql"
if ($LASTEXITCODE -ne 0) { throw 'No se completó la creación de las tablas del foro.' }
