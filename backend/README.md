# ⚙️ ValenciaBank — Backend

API REST desarrollada con **Java 21** y **Spring Boot 3.3.2** que gestiona toda la lógica de negocio de la plataforma bancaria.

---

## 📑 Índice

- [Stack tecnológico](#-stack-tecnológico)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Configuración](#-configuración)
- [Ejecución](#-ejecución)
- [Endpoints API](#-endpoints-api)
- [Servicios y APIs externas](#-servicios-y-apis-externas)
- [Modelos de datos](#-modelos-de-datos)
- [Seguridad](#-seguridad)

---

## 🛠 Stack tecnológico

| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.3.2 | Framework backend |
| Spring Data JPA | — | ORM / Acceso a datos |
| Spring Security | — | Autenticación y CORS |
| MariaDB | 10.6+ | Base de datos relacional |
| JWT (Auth0) | 4.4.0 | Tokens de autenticación |
| Jsoup | 1.17.2 | Web scraping de noticias |
| Jackson | — | Serialización JSON |
| Maven | 3.9+ | Gestión de dependencias y build |

---

## 📁 Estructura del proyecto

```
backend/src/main/java/com/valenciaBank/valenciaBank/
├── ValenciaBankApplication.java       → Punto de entrada
├── config/
│   ├── JacksonConfig.java             → Configuración de serialización JSON
│   └── RestTemplateConfig.java        → Configuración de RestTemplate
├── controller/
│   ├── UserController.java            → Gestión de usuarios (/user)
│   ├── AccountController.java         → Gestión de cuentas (/account)
│   ├── TransactionsController.java    → Depósitos y transferencias (/transactions)
│   ├── PortfolioController.java       → Portfolio crypto (/portfolio)
│   ├── BuyCryptoController.java       → Compra/venta crypto (/crypto/purchase)
│   ├── BuyFundController.java         → Compra/venta fondos (/fund/purchase)
│   ├── CoinGeckoController.java       → Precios crypto (/api/coingecko)
│   ├── FinnhubController.java         → Datos ETFs (/api/finnhub)
│   ├── CryptoPriceController.java     → Precios en BD (/api/crypto-prices)
│   ├── AIAnalysisController.java      → Análisis IA (/api/ai)
│   ├── NewsController.java            → Noticias scraping (/api/news)
│   └── API_InversionesCryptoController → Datos Alpha Vantage (/)
├── model/
│   ├── User.java                      → Entidad usuario
│   ├── Account.java                   → Entidad cuenta bancaria
│   ├── Transaction.java               → Entidad transacción
│   ├── CryptoPurchase.java            → Entidad compra de crypto
│   ├── FundPurchase.java              → Entidad compra de fondo/ETF
│   ├── Crypto.java / CryptoId.java    → Datos históricos crypto (Alpha Vantage)
│   ├── CryptoPrice.java               → Precios actuales en BD
│   ├── CryptoHistoryCache.java        → Caché de historial CoinGecko
│   ├── NewsArticle.java               → DTO de noticia (sin persistencia)
│   └── TransactionData.java           → DTO de transacción
├── repository/                        → Interfaces JPA Repository
├── service/
│   ├── AccountService.java            → Gestión de cuentas bancarias
│   ├── AccountServiceImplementation.java → Implementación de AccountService
│   ├── TransactionService.java        → Gestión de transacciones
│   ├── TransactionServiceImplementation.java → Implementación de TransactionService
│   ├── UserServiceImplementation.java → Lógica de usuarios (registro, login, BCrypt)
│   ├── CoinGeckoService.java          → Integración API CoinGecko
│   ├── FinnhubService.java            → Integración API Finnhub
│   ├── YahooFinanceService.java       → Integración Yahoo Finance
│   ├── AIAnalysisService.java         → Integración Groq AI (Llama 3.3)
│   ├── NewsScraperService.java        → Web scraping de noticias
│   ├── CryptoService.java             → Lógica de datos crypto
│   ├── BuyCryptoService.java          → Lógica compra/venta crypto
│   ├── BuyFundService.java            → Lógica compra/venta fondos
│   ├── PortfolioService.java          → Portfolio detallado + historial
│   └── DataInitializationService.java → Datos iniciales al arrancar
└── utils/
    ├── SecurityConfig.java            → Configuración Spring Security + CORS
    ├── Jwt.java                       → Utilidad generación/validación JWT
    ├── JwtFilter.java                 → Filtro JWT para peticiones
    ├── Methods.java                   → Métodos utilitarios (generación de nº cuenta)
    └── TokenResponse.java             → DTO para respuesta de autenticación JWT
```

---

## ⚙️ Configuración

### 1. Copiar el archivo de propiedades

```bash
cd backend/src/main/resources/
cp application.properties.example application.properties
```

### 2. Configurar las variables necesarias

```properties
# Base de datos MariaDB
spring.datasource.url=jdbc:mariadb://localhost:3306/valenciabank
spring.datasource.username=root
spring.datasource.password=TU_PASSWORD

# API Keys (obtener de cada proveedor)
api.key=TU_ALPHA_VANTAGE_KEY          # https://www.alphavantage.co/support/#api-key
finnhub.api.key=TU_FINNHUB_KEY        # https://finnhub.io/register
groq.api.key=TU_GROQ_KEY              # https://console.groq.com/keys

# JWT
jwt.key=TU_CLAVE_SECRETA_JWT

# Servidor
server.port=8080
```

> **Nota**: Los tres API keys (`api.key`, `finnhub.api.key`, `groq.api.key`) son necesarios para el funcionamiento completo. Sin `finnhub.api.key` no funcionarán las cotizaciones de ETFs, y sin `groq.api.key` no funcionará el análisis IA.

### 3. Crear la base de datos

```sql
CREATE DATABASE valenciabank CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

> Hibernate crea las tablas automáticamente (`ddl-auto=update`).

---

## 🚀 Ejecución

```bash
cd backend/

# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

El servidor arranca en **http://localhost:8080**.

---

## 🔌 Endpoints API

> Referencia completa en [docs/API.md](../docs/API.md)

### Resumen de endpoints principales

| Módulo | Base Path | Endpoints |
|--------|-----------|-----------|
| Usuarios | `/user` | `POST /add`, `POST /login`, `GET /get/{dni}`, `PUT /update/{dni}` |
| Cuentas | `/account` | `POST /add` |
| Transacciones | `/transactions` | `POST /add`, `GET /getFilter` |
| Portfolio Crypto | `/portfolio` | `GET /detailed/{userId}`, `GET /history/{userId}`, `POST /add` |
| Compra Crypto | `/crypto/purchase` | `POST /buy`, `POST /sell` |
| Compra Fondos | `/fund/purchase` | `POST /buy`, `POST /sell`, `GET /portfolio/detailed/{userId}` |
| Precios Crypto | `/api/coingecko` | `GET /price/{symbol}/{currency}`, `GET /history/{symbol}/{days}/{currency}` |
| Datos ETFs | `/api/finnhub` | `GET /etf/{symbol}`, `GET /candles/{symbol}/{res}/{days}` |
| IA | `/api/ai` | `POST /analyze` |
| Noticias | `/api/news` | `GET /crypto`, `GET /economy`, `GET /all` |

---

## 🔗 Servicios y APIs externas

| Servicio | API | Descripción |
|----------|-----|-------------|
| `CoinGeckoService` | CoinGecko API v3 | Precios e historial de criptomonedas. Caché en BD (24h) para evitar rate limiting |
| `YahooFinanceService` | Yahoo Finance | Cotizaciones y velas de ETFs/acciones/índices. API pública sin key |
| `FinnhubService` | Finnhub API | Cotizaciones complementarias (plan free) |
| `AIAnalysisService` | Groq API (Llama 3.3 70B) | Análisis inteligente de tendencias de mercado |
| `NewsScraperService` | Web scraping (Jsoup) | Noticias crypto (CoinTelegraph, CoinDesk) y economía (Investing.com, CNBC) |
| `CryptoService` | Alpha Vantage | Datos diarios de criptomonedas y ETFs (complementario) |

---

## 📊 Modelos de datos

> Diagrama completo en [docs/DATABASE.md](../docs/DATABASE.md)

### Entidades principales

| Entidad | Descripción |
|---------|-------------|
| `User` | Usuario del sistema (username, DNI, email, etc.) |
| `Account` | Cuenta bancaria (saldo, número de cuenta). Relación 1:1 con User |
| `Transaction` | Movimientos bancarios (depósitos, transferencias) |
| `CryptoPurchase` | Registro de compra de criptomonedas (símbolo, cantidad, precio) |
| `FundPurchase` | Registro de compra de fondos/ETFs (símbolo, nombre, tipo, cantidad) |
| `CryptoPrice` | Precios actuales en BD (fallback para cuando la API falla) |
| `CryptoHistoryCache` | Caché de datos históricos de CoinGecko (TTL 24h) |

---

## 🔐 Seguridad

- **Autenticación**: JWT con algoritmo HMAC256 (expiración 24h)
- **Hashing de contraseñas**: BCrypt
- **CORS**: Configurado para `http://localhost:5173` (frontend Vite)
- **CSRF**: Desactivado (API REST stateless)
- **Filtro JWT**: `JwtFilter.java` disponible para validación de tokens

---

## 🧪 Testing

El backend incluye una suite completa de **170 tests unitarios**:

```bash
# Ejecutar todos los tests
./mvnw test

# En Windows
mvnw.cmd test
```

### Configuración de tests

- **Base de datos**: H2 en memoria (perfil `test`)
- **Framework**: JUnit 5 + Mockito
- **Controladores**: MockMvc con `standaloneSetup()` (evita carga del contexto completo)

### Estructura de tests

| Categoría | Clases de test | Tests |
|-----------|---------------|-------|
| Modelos | 10 | Getters, setters, constructores, relaciones JPA |
| Utilidades | 4 | JWT, generación de cuentas, SecurityConfig, TokenResponse |
| Servicios | 8 | Lógica de negocio con mocks |
| Controladores | 11 | Endpoints HTTP con MockMvc |
| **Total** | **33** | **170 tests — 0 fallos** |

---

*Volver al [README principal](../README.md)*
