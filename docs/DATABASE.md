# 🗄️ Base de Datos — ValenciaBank

Este documento describe el modelo de datos del proyecto ValenciaBank, incluyendo el diagrama entidad-relación, descripción de tablas, campos y relaciones.

---

## Índice

- [🗄️ Base de Datos — ValenciaBank](#️-base-de-datos--valenciabank)
  - [Índice](#índice)
  - [Motor de base de datos](#motor-de-base-de-datos)
  - [Diagrama entidad-relación](#diagrama-entidad-relación)
  - [Tablas](#tablas)
    - [`user`](#user)
    - [`account`](#account)
    - [`transaction`](#transaction)
    - [`crypto_purchase`](#crypto_purchase)
    - [`fund_purchase`](#fund_purchase)
    - [`crypto`](#crypto)
    - [`crypto_price`](#crypto_price)
    - [`crypto_history_cache`](#crypto_history_cache)
  - [Relaciones entre entidades](#relaciones-entre-entidades)
  - [Notas de diseño](#notas-de-diseño)
    - [Generación automática de tablas](#generación-automática-de-tablas)
    - [Datos iniciales](#datos-iniciales)
    - [Caché de CoinGecko](#caché-de-coingecko)
    - [Cascadas y borrado](#cascadas-y-borrado)

---

## Motor de base de datos

| Aspecto | Detalle |
|---------|---------|
| Motor | MariaDB 10.6+ / MySQL 8.0+ |
| Charset | utf8mb4 |
| Collation | utf8mb4_general_ci |
| ORM | Hibernate (Spring Data JPA) |
| DDL | Automático (`ddl-auto=update`) |
| Dialecto | `org.hibernate.dialect.MySQLDialect` |

---

## Diagrama entidad-relación

```
┌──────────────────────┐       1:1       ┌──────────────────────┐
│        USER          │────────────────►│       ACCOUNT        │
│──────────────────────│                 │──────────────────────│
│ id (PK)              │                 │ id (PK)              │
│ username (UNIQUE)    │                 │ balance              │
│ password             │                 │ number (UNIQUE)      │
│ dni (UNIQUE)         │                 │ creation_date        │
│ nombre               │                 │ user_id (FK → user)  │
│ apellidos            │                 └──────────────────────┘
│ email (UNIQUE)       │
│ telefono             │       1:N       ┌──────────────────────┐
│ direccion            │────────────────►│    TRANSACTION       │
│                      │                 │──────────────────────│
│                      │                 │ id (PK)              │
│                      │                 │ origin_account       │
│                      │                 │ destination_account  │
│                      │                 │ amount               │
│                      │                 │ date                 │
│                      │                 │ user_id (FK → user)  │
│                      │                 └──────────────────────┘
│                      │
│                      │       1:N       ┌──────────────────────┐
│                      │────────────────►│   CRYPTO_PURCHASE    │
│                      │                 │──────────────────────│
│                      │                 │ id (PK)              │
│                      │                 │ symbol               │
│                      │                 │ quantity             │
│                      │                 │ purchase_price       │
│                      │                 │ purchase_date        │
│                      │                 │ currency             │
│                      │                 │ user_id (FK → user)  │
│                      │                 └──────────────────────┘
│                      │
│                      │       1:N       ┌──────────────────────┐
│                      │────────────────►│    FUND_PURCHASE     │
│                      │                 │──────────────────────│
│                      │                 │ id (PK)              │
│                      │                 │ symbol               │
│                      │                 │ name                 │
│                      │                 │ type (ETF/INDEX)     │
│                      │                 │ quantity             │
│                      │                 │ purchase_price       │
│                      │                 │ purchase_date        │
│                      │                 │ currency             │
│                      │                 │ user_id (FK → user)  │
└──────────────────────┘                 └──────────────────────┘


┌──────────────────────┐    ┌──────────────────────────┐    ┌──────────────────────────┐
│       CRYPTO         │    │      CRYPTO_PRICE        │    │  CRYPTO_HISTORY_CACHE    │
│──────────────────────│    │──────────────────────────│    │──────────────────────────│
│ name (PK compuesta)  │    │ id (PK)                  │    │ id (PK)                  │
│ date (PK compuesta)  │    │ symbol                   │    │ symbol                   │
│ close                │    │ market                   │    │ days                     │
│ high                 │    │ price                    │    │ currency                 │
│ low                  │    │ last_updated             │    │ history_data (LONGTEXT)  │
│ open                 │    │ UNIQUE(symbol, market)   │    │ last_fetched             │
│ volume               │    │                          │    │ expires_at               │
│                      │    │                          │    │ UNIQUE(symbol,days,curr.) │
└──────────────────────┘    └──────────────────────────┘    └──────────────────────────┘
```

---

## Tablas

### `user`

Almacena los datos de los usuarios registrados en la plataforma.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `username` | VARCHAR | NOT NULL, UNIQUE | Nombre de usuario |
| `password` | VARCHAR | — | Contraseña hasheada (BCrypt) |
| `dni` | VARCHAR | NOT NULL, UNIQUE | Documento Nacional de Identidad |
| `nombre` | VARCHAR | — | Nombre del usuario |
| `apellidos` | VARCHAR | — | Apellidos del usuario |
| `email` | VARCHAR | UNIQUE | Correo electrónico |
| `telefono` | VARCHAR | — | Número de teléfono |
| `direccion` | VARCHAR | — | Dirección postal |

---

### `account`

Cuenta bancaria asociada a un usuario. Relación 1:1 con `user`.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `balance` | DOUBLE | — | Saldo actual de la cuenta (en EUR) |
| `number` | VARCHAR | NOT NULL, UNIQUE | Número de cuenta bancaria (generado automáticamente) |
| `creation_date` | DATE | — | Fecha de creación de la cuenta |
| `user_id` | BIGINT | FK → user(id) | Usuario propietario |

---

### `transaction`

Registro de movimientos bancarios (depósitos y transferencias).

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `origin_account` | VARCHAR | — | Número de cuenta origen |
| `destination_account` | VARCHAR | — | Número de cuenta destino |
| `amount` | DOUBLE | — | Importe de la transacción |
| `date` | DATE | — | Fecha de la transacción |
| `user_id` | BIGINT | FK → user(id) | Usuario que realiza la transacción |

> **Nota**: Si `origin_account == destination_account`, se trata de un depósito.

---

### `crypto_purchase`

Registro de compras de criptomonedas realizadas por los usuarios.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `symbol` | VARCHAR | NOT NULL | Símbolo de la criptomoneda (BTC, ETH, SOL...) |
| `quantity` | DOUBLE | NOT NULL | Cantidad comprada |
| `purchase_price` | DOUBLE | NOT NULL | Precio por unidad en el momento de compra |
| `purchase_date` | DATETIME | NOT NULL | Fecha y hora de la compra |
| `currency` | VARCHAR | NOT NULL | Moneda de referencia (EUR, USD...) |
| `user_id` | BIGINT | FK → user(id) | Usuario comprador |

---

### `fund_purchase`

Registro de compras de fondos indexados y ETFs.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `symbol` | VARCHAR | NOT NULL | Símbolo del fondo (SPY, VOO, XLK...) |
| `name` | VARCHAR | NOT NULL | Nombre descriptivo (ej: "S&P 500") |
| `type` | VARCHAR | NOT NULL | Tipo: `ETF` o `INDEX` |
| `quantity` | DOUBLE | NOT NULL | Cantidad de participaciones |
| `purchase_price` | DOUBLE | NOT NULL | Precio por unidad en el momento de compra |
| `purchase_date` | DATETIME | NOT NULL | Fecha y hora de la compra |
| `currency` | VARCHAR | NOT NULL | Moneda de referencia (EUR, USD...) |
| `user_id` | BIGINT | FK → user(id) | Usuario comprador |

---

### `crypto`

Datos históricos de criptomonedas importados desde Alpha Vantage. Clave primaria compuesta por `name` + `date`.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `name` | VARCHAR | PK (compuesta) | Nombre/símbolo de la criptomoneda |
| `date` | DATE | PK (compuesta) | Fecha del dato |
| `close` | DOUBLE | — | Precio de cierre |
| `high` | DOUBLE | — | Precio máximo del día |
| `low` | DOUBLE | — | Precio mínimo del día |
| `open` | DOUBLE | — | Precio de apertura |
| `volume` | DOUBLE | — | Volumen de trading |

---

### `crypto_price`

Precios actuales de criptomonedas almacenados en BD como fallback cuando las APIs externas no están disponibles.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `symbol` | VARCHAR | NOT NULL | Símbolo de la crypto (BTC, ETH...) |
| `market` | VARCHAR | NOT NULL | Mercado/moneda (EUR, USD...) |
| `price` | DOUBLE | NOT NULL | Precio actual |
| `last_updated` | DATETIME | NOT NULL | Última actualización |
| — | — | UNIQUE(symbol, market) | Restricción de unicidad compuesta |

---

### `crypto_history_cache`

Caché de datos históricos de CoinGecko para evitar llamadas excesivas a la API (rate limiting). TTL de 24 horas.

| Campo | Tipo | Restricciones | Descripción |
|-------|------|---------------|-------------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `symbol` | VARCHAR | NOT NULL | Símbolo de la crypto |
| `days` | INT | NOT NULL | Rango temporal (1, 7, 30, 365) |
| `currency` | VARCHAR | NOT NULL | Moneda (EUR, USD) |
| `history_data` | LONGTEXT | NOT NULL | Datos JSON completos del historial |
| `last_fetched` | DATETIME | NOT NULL | Última vez que se obtuvo de la API |
| `expires_at` | DATETIME | NOT NULL | Fecha de expiración de la caché |
| — | — | UNIQUE(symbol, days, currency) | Restricción de unicidad compuesta |

---

## Relaciones entre entidades

| Relación | Tipo | Descripción |
|----------|------|-------------|
| User ↔ Account | 1:1 | Cada usuario tiene exactamente una cuenta bancaria |
| User → Transaction | 1:N | Un usuario puede tener múltiples transacciones |
| User → CryptoPurchase | 1:N | Un usuario puede tener múltiples compras de crypto |
| User → FundPurchase | 1:N | Un usuario puede tener múltiples compras de fondos |

> Las tablas `Crypto`, `CryptoPrice` y `CryptoHistoryCache` son independientes y no tienen FK hacia `User`. Sirven como almacenamiento de datos de mercado globales.

---

## Notas de diseño

### Generación automática de tablas
Hibernate genera y actualiza las tablas automáticamente (`ddl-auto=update`). No es necesario ejecutar scripts SQL de creación.

### Datos iniciales
Al arrancar la aplicación, `DataInitializationService` ejecuta el script `crypto-prices-init.sql` para poblar la tabla `CRYPTO_PRICE` con precios iniciales de referencia.

### Caché de CoinGecko
La tabla `CRYPTO_HISTORY_CACHE` implementa un patrón cache-aside con lógica upsert para evitar errores de claves duplicadas en peticiones concurrentes. El TTL es de 24 horas y soporta fallback a cachés de otros rangos temporales.

### Cascadas y borrado
- `User → CryptoPurchase`: Cascade ALL + orphanRemoval (si se borra el usuario, se borran sus compras)
- `User → Transaction`: Cascade PERSIST (las transacciones persisten aunque se modifique el usuario)
- `User → Account`: Relación bidireccional gestionada por JPA

---

*Volver al [README principal](../README.md)*
