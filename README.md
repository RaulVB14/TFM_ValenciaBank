# 🏦 ValenciaBank — Plataforma Bancaria Digital con IA

**ValenciaBank** es una plataforma bancaria moderna desarrollada como Trabajo Final de Máster (TFM). Integra inteligencia artificial para ayudar a los usuarios a gestionar sus finanzas, operar con criptomonedas y fondos indexados/ETFs, y recibir recomendaciones de inversión personalizadas.

---

## 📑 Índice

- [Características principales](#-características-principales)
- [Arquitectura del proyecto](#-arquitectura-del-proyecto)
- [Requisitos previos](#-requisitos-previos)
- [Instalación y configuración](#-instalación-y-configuración)
- [Ejecución](#-ejecución)
- [Documentación técnica](#-documentación-técnica)
- [Submódulos del proyecto](#-submódulos-del-proyecto)
- [APIs externas utilizadas](#-apis-externas-utilizadas)
- [Estado del proyecto](#-estado-del-proyecto)
- [Autor](#-autor)

---

## ✨ Características principales

| Categoría | Funcionalidad |
|-----------|---------------|
| 🏦 **Banca digital** | Gestión de cuentas, consulta de saldo, depósitos, transferencias e historial de movimientos |
| 📈 **Inversiones Crypto** | Compra y venta de criptomonedas con precios en tiempo real (CoinGecko) |
| 📊 **Fondos indexados y ETFs** | Compra y venta de ETFs/fondos con cotizaciones reales (Yahoo Finance / Finnhub) |
| 💼 **Portfolio** | Portfolio detallado con posiciones, ganancias/pérdidas, gráficos de evolución temporal |
| 🤖 **IA — Análisis de tendencias** | Análisis de mercado con Llama 3.3 70B (Groq) — Recomendaciones comprar/vender/mantener |
| 📰 **Noticias** | Noticias de criptomonedas y economía mediante web scraping (CoinTelegraph, Investing.com) |
| 🔐 **Seguridad** | Autenticación con JWT, hashing BCrypt, Spring Security |

---

## 🏗 Arquitectura del proyecto

```
ValenciaBankWeb/
├── backend/          → API REST (Java 21 + Spring Boot 3.3.2 + MariaDB)
├── frontend/         → SPA (React 18 + Vite 5)
├── docs/             → Documentación técnica (arquitectura, BBDD, API)
└── README.md         → Este archivo
```

El proyecto sigue una arquitectura **cliente-servidor**:

- **Backend**: API REST con Spring Boot que gestiona la lógica de negocio, conexión a la base de datos (MariaDB), integración con APIs externas (CoinGecko, Yahoo Finance, Groq AI) y web scraping de noticias.
- **Frontend**: Aplicación SPA con React + Vite que consume la API REST, renderiza gráficos interactivos con ECharts y proporciona una interfaz moderna y responsiva.

> Para más detalles, consulta la [documentación de arquitectura](docs/ARCHITECTURE.md).

---

## 📋 Requisitos previos

| Componente | Versión mínima |
|------------|---------------|
| **Java** | JDK 21+ |
| **Node.js** | 18+ |
| **npm** | 9+ |
| **MariaDB / MySQL** | 10.6+ / 8.0+ |
| **Maven** | 3.9+ (incluido via `mvnw`) |
| **Git** | 2.30+ |

---

## 🛠 Instalación y configuración

### 1. Clonar el repositorio

```bash
git clone https://github.com/RaulVB14/ValenciaBankWeb.git
cd ValenciaBankWeb
```

### 2. Configurar la base de datos

Crear la base de datos en MariaDB/MySQL:

```sql
CREATE DATABASE valenciabank CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

> Las tablas se crean automáticamente gracias a Hibernate (`ddl-auto=update`).

### 3. Configurar el backend

```bash
cd backend/src/main/resources/
cp application.properties.example application.properties
```

Editar `application.properties` con tus credenciales:

```properties
# Base de datos
spring.datasource.url=jdbc:mariadb://localhost:3306/valenciabank
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña

# API Keys
api.key=TU_ALPHA_VANTAGE_KEY
finnhub.api.key=TU_FINNHUB_KEY
groq.api.key=TU_GROQ_KEY

# JWT
jwt.key=TU_CLAVE_SECRETA_JWT
```

### 4. Instalar dependencias del frontend

```bash
cd frontend/
npm install
```

> Para más detalles de cada módulo, consulta los README individuales.

---

## 🚀 Ejecución

### Backend (puerto 8080)

```bash
cd backend/
./mvnw spring-boot:run
```

> En Windows: `mvnw.cmd spring-boot:run`

### Frontend (puerto 5173)

```bash
cd frontend/
npm run dev
```

Abre http://localhost:5173 en tu navegador.

---

## 📖 Documentación técnica

| Documento | Descripción |
|-----------|-------------|
| 📐 [Arquitectura](docs/ARCHITECTURE.md) | Diagrama de arquitectura, stack tecnológico, flujo de datos y patrones de diseño |
| 🗄️ [Base de datos](docs/DATABASE.md) | Modelo entidad-relación, descripción de tablas y campos |
| 🔌 [API REST](docs/API.md) | Referencia completa de todos los endpoints con métodos, rutas y descripciones |

---

## 📂 Submódulos del proyecto

| Módulo | README | Descripción |
|--------|--------|-------------|
| ⚙️ Backend | [backend/README.md](backend/README.md) | API REST con Spring Boot, servicios, controladores y modelos |
| 🎨 Frontend | [frontend/README.md](frontend/README.md) | Aplicación React con Vite, componentes, páginas y estilos |

---

## 🔗 APIs externas utilizadas

| API | Uso | Tier |
|-----|-----|------|
| [CoinGecko](https://www.coingecko.com/en/api) | Precios e historial de criptomonedas | Gratuito |
| [Yahoo Finance](https://finance.yahoo.com/) | Cotizaciones de ETFs, fondos y acciones | Gratuito (no oficial) |
| [Finnhub](https://finnhub.io/) | Cotizaciones de acciones/ETFs (complementario) | Gratuito (con límites) |
| [Alpha Vantage](https://www.alphavantage.co/) | Datos históricos de mercados | Gratuito (con límites) |
| [Groq](https://console.groq.com/) | IA — Modelo Llama 3.3 70B para análisis de mercado | Gratuito (30 req/min) |

---

## 📌 Estado del proyecto

- [x] Estructura completa backend y frontend
- [x] Gestión de usuarios, cuentas y autenticación JWT
- [x] Depósitos y transferencias entre cuentas
- [x] Visualización de mercado crypto con gráficos interactivos
- [x] Visualización de ETFs y fondos indexados
- [x] Compra y venta de criptomonedas
- [x] Compra y venta de fondos indexados / ETFs
- [x] Portfolio detallado con ganancias/pérdidas y evolución temporal
- [x] Análisis de tendencias con IA (Groq / Llama 3.3)
- [x] Noticias de crypto y economía (web scraping)
- [x] Landing page con noticias en tiempo real

---

## 👤 Autor

**Raúl VB** — Trabajo Final de Máster  
📧 Proyecto académico — Uso educativo

---

> *Desarrollado con Java 21, Spring Boot 3.3.2, React 18, Vite 5, MariaDB, ECharts, y Llama 3.3 70B*
