#!/usr/bin/env bash
set -euo pipefail
sqlplus -s / as sysdba @/opt/foro-sql/00_crear_esquemas.sql "$CLAVE_USUARIOS" "$CLAVE_FORO" FREEPDB1
sqlplus -s "MM_USUARIOS/${CLAVE_USUARIOS}@//localhost:1521/FREEPDB1" @/opt/foro-sql/01_usuarios.sql
sqlplus -s "MM_FORO/${CLAVE_FORO}@//localhost:1521/FREEPDB1" @/opt/foro-sql/02_foro.sql
