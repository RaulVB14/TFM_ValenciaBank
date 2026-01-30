--Pasos para migrar a remoto
2. Ve a https://railway.app
Haz clic en "Start Project"
Elige "Deploy from GitHub"
Selecciona tu repo valenciabank
3. Railway detectará el proyecto Maven
Automáticamente compilará y levantará la app
Importante: Railway automáticamente crea una BD MariaDB cuando lo solicites
4. Añade la base de datos MariaDB
En el dashboard de Railway:

Haz clic en "+ Add Service"
Busca "MySQL" (compatible con MariaDB)
Selecciona la versión 8.0
Railway generará automáticamente:

5. Configura el backend para usar la BD
En el panel de tu aplicación en Railway:

Ve a "Variables"
Añade las variables del DB (Railway las sugiere automáticamente)
El application-prod.properties las usará con ${DB_HOST}, ${DB_USER}, etc.
6. Deploy automático
Railway automáticamente:

Compila tu Maven
Levanta la BD MariaDB
Lanza tu backend en la URL:
✅ Resumen de cambios:
✅ application-prod.properties → Usa MariaDB con variables de entorno
✅ pom.xml → MariaDB driver (sin PostgreSQL)
✅ Procfile → Rail ya sabe cómo levantar tu app
