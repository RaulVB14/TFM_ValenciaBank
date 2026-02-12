# 📋 Propósitos y Roadmap del TFM

Este documento recoge los objetivos planificados para el Trabajo Final de Máster y su estado de desarrollo.

---

## Objetivos completados

- [x] Migrar la base de datos a remoto (URL pública)
- [x] Implementar chatbot IA para recomendaciones de inversión (comprar / vender / mantener)
- [x] Permitir compra y venta de criptomonedas
- [x] Permitir compra y venta de ETFs y fondos indexados
- [x] Crear portfolios para que los usuarios visualicen sus activos

---

## Notas de despliegue (Railway)

### Pasos para migrar a remoto

1. Ir a [railway.app](https://railway.app) → "Start Project" → "Deploy from GitHub"
2. Seleccionar el repositorio `ValenciaBank`
3. Railway detecta el proyecto Maven y compila automáticamente
4. Añadir servicio de base de datos: "+ Add Service" → "MySQL" (compatible con MariaDB)
5. Configurar variables de entorno en el panel de Railway:
   - Railway sugiere automáticamente las variables de la BD
   - El archivo `application-prod.properties` las consume con `${DB_HOST}`, `${DB_USER}`, etc.
6. Railway despliega automáticamente el backend

### Archivos de configuración para producción

| Archivo | Descripción |
|---------|-------------|
| `application-prod.properties` | Usa MariaDB con variables de entorno |
| `pom.xml` | Driver MariaDB (sin PostgreSQL) |

---

*Volver al [README principal](README.md)*
