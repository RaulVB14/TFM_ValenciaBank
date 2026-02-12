# ValenciaBankWeb

## Descripción
ValenciaBankWeb es una plataforma bancaria moderna que integra inteligencia artificial para ayudar a los clientes a gestionar y optimizar sus inversiones en criptomonedas y fondos indexados. El objetivo principal es ofrecer una experiencia bancaria digital avanzada, donde los usuarios puedan consultar el estado de sus inversiones y recibir recomendaciones personalizadas a través de un chatbot inteligente.

## Características principales
- **Gestión de cuentas bancarias**: Consulta de saldo, transferencias y movimientos.
- **Visualización de inversiones**: Gráficas y resúmenes de carteras de criptomonedas y fondos indexados.
- **Compra y venta de criptomonedas**: Interfaz sencilla para operar con activos digitales.
- **Recomendaciones inteligentes**: Un chatbot con IA analiza el perfil de riesgo del usuario y recomienda si debe comprar, vender o mantener sus inversiones.
- **Seguridad**: Autenticación y gestión segura de usuarios.

## Arquitectura
- **Backend**: Java + Spring Boot
  - Gestión de usuarios, cuentas, inversiones y lógica de negocio.
  - API REST para comunicación con el frontend.
- **Frontend**: React + Vite
  - Interfaz moderna y responsiva.
  - Visualización de datos y comunicación con el backend.

## Inteligencia Artificial
El sistema incorpora un chatbot que analiza:
- Perfil de riesgo del usuario (conservador, moderado, agresivo)
- Estado actual del mercado
- Historial de operaciones

El chatbot sugiere acciones (comprar, vender, mantener) para cada activo, adaptándose a la situación y preferencias del cliente.

## Instalación y despliegue
1. **Clona el repositorio**
2. **Configura las variables de entorno** (ver archivos `.example`)
3. **Backend**
   - Requiere Java 17+ y una base de datos MariaDB/MySQL
   - Ejecuta: `./mvnw spring-boot:run` en la carpeta `backend`
4. **Frontend**
   - Requiere Node.js 18+
   - Ejecuta: `npm install && npm run dev` en la carpeta `frontend`

## Estado del proyecto
- [x] Estructura básica backend y frontend
- [x] Gestión de usuarios y cuentas
- [x] Visualización de inversiones
- [x] Integración completa del chatbot IA
- [x] Recomendaciones automáticas según perfil de riesgo

## Licencia
Proyecto académico para TFM. Uso educativo.

---
**Autor:** RaulVB14
