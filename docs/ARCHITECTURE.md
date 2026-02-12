# 📐 Arquitectura — ValenciaBank

Este documento describe la arquitectura técnica del proyecto ValenciaBank, incluyendo el stack tecnológico, patrones de diseño, flujo de datos y decisiones de diseño.

---

## Índice

- [Visión general](#visión-general)
- [Diagrama de arquitectura](#diagrama-de-arquitectura)
- [Stack tecnológico](#stack-tecnológico)
- [Flujo de datos](#flujo-de-datos)
- [Patrones de diseño](#patrones-de-diseño)
- [Integración con APIs externas](#integración-con-apis-externas)
- [Estrategias de caché](#estrategias-de-caché)
- [Seguridad](#seguridad)
- [Despliegue](#despliegue)

---

## Visión general

ValenciaBank sigue una arquitectura **cliente-servidor** con separación completa entre frontend y backend:

```
┌──────────────────────────────────────────────────────────────────────┐
│                           CLIENTE (Browser)                          │
│                                                                      │
│   ┌──────────────────────────────────────────────────────────────┐   │
│   │              Frontend (React 18 + Vite 5)                    │   │
│   │         http://localhost:5173                                 │   │
│   │                                                              │   │
│   │  ┌─────────┐  ┌──────────┐  ┌──────────┐  ┌─────────────┐  │   │
│   │  │  Pages   │  │Components│  │  ECharts │  │  ApiService │  │   │
│   │  └─────────┘  └──────────┘  └──────────┘  └──────┬──────┘  │   │
│   └──────────────────────────────────────────────────│──────────┘   │
│                                                       │              │
└───────────────────────────────────────────────────────│──────────────┘
                                                        │ HTTP/REST
                                                        │ (axios)
┌───────────────────────────────────────────────────────│──────────────┐
│                        SERVIDOR                       │              │
│                                                       ▼              │
│   ┌──────────────────────────────────────────────────────────────┐   │
│   │            Backend (Spring Boot 3.3.2 — Java 21)             │   │
│   │         http://localhost:8080                                 │   │
│   │                                                              │   │
│   │  ┌────────────┐  ┌───────────┐  ┌────────────────────────┐  │   │
│   │  │ Controllers │──│  Services │──│   JPA Repositories     │  │   │
│   │  └────────────┘  └─────┬─────┘  └───────────┬────────────┘  │   │
│   │                        │                     │               │   │
│   │              ┌─────────┴─────────┐           │               │   │
│   │              │ APIs Externas     │           │               │   │
│   │              │ • CoinGecko       │           │               │   │
│   │              │ • Yahoo Finance   │     ┌─────▼─────┐        │   │
│   │              │ • Finnhub         │     │  MariaDB   │        │   │
│   │              │ • Groq AI         │     │  (JPA/     │        │   │
│   │              │ • Web Scraping    │     │  Hibernate)│        │   │
│   │              └───────────────────┘     └───────────┘        │   │
│   └──────────────────────────────────────────────────────────────┘   │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Stack tecnológico

### Backend

| Capa | Tecnología | Versión |
|------|-----------|---------|
| Lenguaje | Java | 21 (LTS) |
| Framework | Spring Boot | 3.3.2 |
| ORM | Spring Data JPA / Hibernate | — |
| Base de datos | MariaDB | 10.6+ |
| Seguridad | Spring Security + JWT (Auth0) | 4.4.0 |
| HTTP Clients | RestTemplate + WebClient | — |
| Web Scraping | Jsoup | 1.17.2 |
| Build | Maven | 3.9+ |

### Frontend

| Capa | Tecnología | Versión |
|------|-----------|---------|
| Librería UI | React | 18.3.1 |
| Bundler | Vite | 5.4.1 |
| Router | React Router DOM | 6.26.1 |
| HTTP | Axios | 1.8.1 |
| Gráficos | ECharts | 5.6.0 |
| Iconos | React Icons | 5.5.0 |
| Linting | ESLint | 9.9.0 |

---

## Flujo de datos

### 1. Autenticación

```
Usuario → Login (DNI + password)
       → POST /user/login
       → Backend valida con BCrypt
       → Genera JWT (HMAC256, 24h)
       → Frontend guarda token en localStorage
       → Todas las peticiones incluyen header: Authorization: Bearer <token>
```

### 2. Operaciones bancarias

```
Depósito/Transferencia
       → POST /transactions/add
       → Valida cuentas origen/destino
       → Actualiza saldos en tabla Account
       → Registra transacción en tabla Transaction
```

### 3. Compra de criptomonedas

```
Usuario selecciona crypto + cantidad
       → POST /crypto/purchase/buy
       → BuyCryptoService obtiene precio actual (CoinGecko)
       → Verifica saldo suficiente
       → Resta dinero de Account
       → Crea registro en CryptoPurchase
       → Retorna confirmación con detalles
```

### 4. Visualización de mercado

```
Frontend monta gráfico
       → GET /api/coingecko/history/{symbol}/{days}/{currency}
       → CoinGeckoService verifica caché en BD (CryptoHistoryCache)
       → Si caché válido (< 24h): retorna datos cacheados
       → Si expirado: llama a CoinGecko API, guarda en caché, retorna
       → Frontend procesa datos y renderiza con ECharts
```

### 5. Análisis con IA

```
Frontend envía datos de precios
       → POST /api/ai/analyze { symbol, type, prices[], language }
       → AIAnalysisService construye prompt de análisis
       → Llama a Groq API (Llama 3.3 70B)
       → Genera: tendencia, soporte/resistencia, recomendación
       → Frontend muestra análisis al usuario
```

---

## Patrones de diseño

### Arquitectura en capas (N-tier)

```
Controller → Service → Repository → Database
```

- **Controllers**: Reciben peticiones HTTP, validan entrada, delegan al servicio
- **Services**: Contienen la lógica de negocio, orquestan operaciones
- **Repositories**: Acceso a datos mediante interfaces JPA
- **Models**: Entidades JPA mapeadas a tablas de la BD

### Otros patrones aplicados

| Patrón | Uso |
|--------|-----|
| **DTO** | `TransactionData`, `NewsArticle` — Objetos de transferencia sin persistencia |
| **Repository** | Spring Data JPA — Interfaces con queries derivadas automáticamente |
| **Singleton** | Servicios Spring (`@Service`) — Instancias únicas gestionadas por el contenedor |
| **Cache-aside** | `CoinGeckoService` — Verifica caché en BD antes de llamar a la API externa |
| **Fallback** | `NewsScraperService` — Si CoinTelegraph falla, usa CoinDesk como respaldo |
| **Proxy** | `FinnhubController` actúa como proxy hacia `YahooFinanceService` |

---

## Integración con APIs externas

```
                    ┌───────────────────┐
                    │    CoinGecko      │ ◄── Precios e historial crypto
                    │  (api.coingecko.  │     Rate limit: 1.5s entre llamadas
                    │   com/api/v3)     │     Caché: BD 24h
                    └───────────────────┘

                    ┌───────────────────┐
                    │  Yahoo Finance    │ ◄── Cotizaciones ETFs/fondos/índices
                    │  (query1.finance. │     Sin API key
Backend ────────►   │  yahoo.com)       │     Mapeo de símbolos integrado
                    └───────────────────┘

                    ┌───────────────────┐
                    │    Finnhub        │ ◄── Cotizaciones complementarias
                    │  (finnhub.io)     │     Free tier: símbolos limitados
                    └───────────────────┘

                    ┌───────────────────┐
                    │    Groq AI        │ ◄── Análisis de mercado con IA
                    │  (Llama 3.3 70B)  │     Free: 30 req/min, 14.4K req/día
                    └───────────────────┘

                    ┌───────────────────┐
                    │  Web Scraping     │ ◄── Noticias crypto + economía
                    │  (Jsoup)          │     Caché en memoria: 15 min
                    │  • CoinTelegraph  │
                    │  • CoinDesk RSS   │
                    │  • Investing.com  │
                    │  • CNBC RSS       │
                    └───────────────────┘
```

---

## Estrategias de caché

| Recurso | Tipo de caché | TTL | Almacenamiento |
|---------|---------------|-----|----------------|
| Historial crypto (CoinGecko) | Cache-aside | 24 horas | Tabla `CRYPTO_HISTORY_CACHE` (MariaDB) |
| Precios crypto actuales | Write-through | Siempre actualizado | Tabla `CRYPTO_PRICE` (MariaDB) |
| Noticias (scraping) | In-memory | 15 minutos | `ConcurrentHashMap` en `NewsScraperService` |
| Cotizaciones ETFs | Sin caché | — | Siempre consulta en tiempo real |

---

## Seguridad

### Arquitectura de seguridad actual

```
Request → Spring Security Filter Chain
       → CORS Filter (localhost:5173)
       → CSRF desactivado (API REST stateless)
       → Todas las rutas permitidas (permitAll)
       → Controller procesa la petición
```

### Componentes de seguridad

- **`SecurityConfig.java`**: Configuración de Spring Security con CORS
- **`Jwt.java`**: Generación y validación de tokens JWT (HMAC256)
- **`JwtFilter.java`**: Filtro para validación de tokens en peticiones
- **`BCryptPasswordEncoder`**: Hashing de contraseñas

### Tokens JWT

- **Algoritmo**: HMAC256
- **Expiración**: 24 horas
- **Emisión**: `POST /user/login` → `TokenResponse { token, username }`
- **Uso**: Header `Authorization: Bearer <token>` en todas las peticiones autenticadas

---

## Despliegue

### Desarrollo local

| Componente | URL | Comando |
|------------|-----|---------|
| Backend | http://localhost:8080 | `./mvnw spring-boot:run` |
| Frontend | http://localhost:5173 | `npm run dev` |
| Base de datos | localhost:3306 | Servicio MariaDB |

### Producción

El backend soporta un perfil de producción (`application-prod.properties`) para configurar:
- URL de base de datos de producción
- CORS con dominio real
- Claves JWT seguras

---

*Volver al [README principal](../README.md)*
