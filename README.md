# Sistema de foros en línea

**Estudiante:** Matias Muñoz  
**Asignatura:** Desarrollo Full Stack III DSY2205  
**Actividad:** Experiencia 1 Semana 2

Aplicación BackEnd desarrollada con Java 21, Spring Boot y Oracle. Incluye un microservicio de usuarios e inicio de sesión y un microservicio de categorías, temas y comentarios.

## Organización

- `sistema-foros-en-linea`: código fuente, configuración, pruebas y colección Postman.
- `scripts`: creación de esquemas y tablas Oracle, carga de datos iniciales y consultas de verificación.

El foro incluye cinco categorías y tres roles con permisos diferentes. Cada tabla contiene al menos tres registros iniciales. La autenticación utiliza tokens JWT y contraseñas almacenadas con BCrypt.

## Ejecución

Con JDK 21, Oracle XE con XEPDB1 abierto y SQL Plus disponible, ejecutar desde `sistema-foros-en-linea`:

```powershell
Copy-Item .env.ejemplo .env
.\mvnw.cmd clean verify
powershell -NoProfile -ExecutionPolicy Bypass -File .\herramientas\preparar-oracle.ps1
```

La preparación de Oracle se realiza una vez en una instalación nueva mediante una cuenta autorizada como SYSDBA. Para iniciar los servicios, ejecutar cada comando en una terminal independiente:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\herramientas\iniciar-usuarios.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\herramientas\iniciar-foro.ps1
```

El servicio de usuarios utiliza el puerto 8081 y el foro utiliza el puerto 8082. Las instrucciones completas se encuentran en `sistema-foros-en-linea/LEEME.txt`.

## Validación

22 pruebas automatizadas aprobadas. Colección Postman con 46 solicitudes y 54 verificaciones aprobadas contra Oracle. Las cuentas y claves de ejemplo corresponden a la demostración académica local.
