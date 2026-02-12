# 🔌 API REST — ValenciaBank

Referencia completa de todos los endpoints de la API REST del backend de ValenciaBank.

**Base URL**: `http://localhost:8080`

---

## Índice

- [Autenticación](#autenticación)
- [Usuarios](#usuarios---user)
- [Cuentas](#cuentas---account)
- [Transacciones](#transacciones---transactions)
- [Portfolio Crypto](#portfolio-crypto---portfolio)
- [Compra/Venta Crypto](#compraventa-crypto---cryptopurchase)
- [Compra/Venta Fondos/ETFs](#compraventa-fondosetfs---fundpurchase)
- [Precios Crypto (CoinGecko)](#precios-crypto---apicoingecko)
- [Precios Crypto (BD)](#precios-crypto-bd---apicrypto-prices)
- [Datos ETFs/Fondos (Finnhub/Yahoo)](#datos-etfsfondos---apifinnhub)
- [Datos Alpha Vantage](#datos-alpha-vantage)
- [Análisis IA](#análisis-ia---apiai)
- [Noticias](#noticias---apinews)

---

## Autenticación

La API usa **JWT (JSON Web Tokens)** para autenticación.

1. El usuario inicia sesión con `POST /user/login`
2. Recibe un token JWT en la respuesta
3. Incluye el token en las peticiones posteriores:

```
Authorization: Bearer <token>
```

---

## Usuarios — `/user`

### `POST /user/add` — Registrar usuario

Crea un nuevo usuario y genera automáticamente una cuenta bancaria.

**Body** (JSON):
```json
{
  "username": "raul_vb",
  "password": "contraseña123",
  "dni": "12345678A",
  "nombre": "Raúl",
  "apellidos": "García López",
  "email": "raul@email.com",
  "telefono": "612345678",
  "direccion": "Calle Valencia 1"
}
```

**Respuesta** `200 OK`: Objeto User con Account asociada.

---

### `POST /user/login` — Iniciar sesión

**Body** (JSON):
```json
{
  "dni": "12345678A",
  "password": "contraseña123"
}
```

**Respuesta** `200 OK`:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "username": "raul_vb"
}
```

---

### `GET /user/get/{dni}` — Obtener usuario por DNI

**Headers**: `Authorization: Bearer <token>`

**Respuesta** `200 OK`: Objeto User completo con Account.

---

### `GET /user/getAll` — Listar todos los usuarios

**Respuesta** `200 OK`: Array de usuarios.

---

### `GET /user/exists/{dni}` — Verificar si existe un usuario

**Respuesta** `200 OK`: `true` | `false`

---

### `PUT /user/update/{dni}` — Actualizar usuario

**Headers**: `Authorization: Bearer <token>`

**Body** (JSON): Campos a actualizar (username, nombre, apellidos, email, telefono, direccion, password).

**Respuesta** `200 OK`: Usuario actualizado.

---

## Cuentas — `/account`

### `POST /account/add` — Crear cuenta

> Normalmente se crea automáticamente al registrar un usuario.

---

## Transacciones — `/transactions`

### `POST /transactions/add` — Crear transacción

Soporta depósitos (misma cuenta origen y destino) y transferencias entre cuentas.

**Body** (JSON):
```json
{
  "transaction": {
    "originAccount": "ES1234567890",
    "destinationAccount": "ES0987654321",
    "amount": 100.00
  },
  "user": "12345678A"
}
```

**Respuesta** `200 OK`: Transacción creada.

---

### `GET /transactions/getFilter` — Filtrar transacciones

**Query params**: `startDate`, `endDate`, `minAmount`, `maxAmount`

**Respuesta** `200 OK`: Lista de transacciones filtradas.

---

## Portfolio Crypto — `/portfolio`

### `GET /portfolio/detailed/{userId}` — Portfolio detallado

Devuelve posiciones agrupadas por símbolo con precios actuales y ganancias/pérdidas.

**Respuesta** `200 OK`:
```json
{
  "success": true,
  "userId": 1,
  "positions": [
    {
      "symbol": "BTC",
      "quantity": 0.5,
      "averagePrice": 45000.00,
      "currentPrice": 52000.00,
      "investmentValue": 22500.00,
      "currentValue": 26000.00,
      "gainLoss": 3500.00,
      "gainLossPercent": 15.56,
      "firstPurchaseDate": "2025-01-15T10:30:00"
    }
  ],
  "summary": {
    "totalInvested": 22500.00,
    "totalCurrentValue": 26000.00,
    "totalGainLoss": 3500.00,
    "totalGainLossPercent": 15.56
  }
}
```

---

### `GET /portfolio/history/{userId}?days=30` — Historial del portfolio

Devuelve el valor del portfolio en cada punto temporal para gráficos de evolución.

**Query params**: `days` (1, 7, 30, 365)

**Respuesta** `200 OK`:
```json
{
  "success": true,
  "dates": ["1707000000000", "1707086400000"],
  "values": [25000.00, 26000.00],
  "invested": [22500.00, 22500.00]
}
```

---

### `GET /portfolio/user/{userId}` — Portfolio completo (raw)

Devuelve todas las compras de crypto sin procesar.

---

### `GET /portfolio/user/{userId}/symbol/{symbol}` — Holdings de una crypto

---

### `GET /portfolio/user/{userId}/quantity/{symbol}` — Cantidad total

**Respuesta** `200 OK`:
```json
{
  "symbol": "BTC",
  "totalQuantity": 0.5,
  "averageCost": 45000.00
}
```

---

### `POST /portfolio/add` — Agregar compra

---

### `DELETE /portfolio/{purchaseId}` — Eliminar compra (venta completa)

---

### `PUT /portfolio/{purchaseId}/quantity` — Actualizar cantidad (venta parcial)

---

## Compra/Venta Crypto — `/crypto/purchase`

### `POST /crypto/purchase/buy` — Comprar criptomoneda

**Body** (JSON):
```json
{
  "userId": 1,
  "symbol": "BTC",
  "quantity": 0.01,
  "currency": "EUR"
}
```

**Respuesta** `200 OK`:
```json
{
  "success": true,
  "message": "Compra realizada exitosamente",
  "symbol": "BTC",
  "quantity": 0.01,
  "pricePerUnit": 52000.00,
  "totalCost": 520.00,
  "newBalance": 9480.00
}
```

---

### `POST /crypto/purchase/sell` — Vender criptomoneda

**Body** (JSON):
```json
{
  "userId": 1,
  "purchaseId": 5,
  "quantityToSell": 0.005
}
```

---

## Compra/Venta Fondos/ETFs — `/fund/purchase`

### `POST /fund/purchase/buy` — Comprar fondo/ETF

**Body** (JSON):
```json
{
  "userId": 1,
  "symbol": "SPY",
  "name": "S&P 500",
  "type": "ETF",
  "quantity": 2.5,
  "currency": "USD"
}
```

**Respuesta** `200 OK`:
```json
{
  "success": true,
  "message": "Compra realizada exitosamente",
  "symbol": "SPY",
  "name": "S&P 500",
  "type": "ETF",
  "quantity": 2.5,
  "pricePerUnit": 480.50,
  "totalCost": 1201.25,
  "newBalance": 8798.75
}
```

---

### `POST /fund/purchase/sell` — Vender fondo/ETF

**Body** (JSON):
```json
{
  "userId": 1,
  "purchaseId": 3,
  "quantityToSell": 1.0
}
```

---

### `GET /fund/purchase/portfolio/{userId}` — Portfolio de fondos (raw)

---

### `GET /fund/purchase/portfolio/detailed/{userId}` — Portfolio detallado de fondos

Devuelve posiciones agrupadas con precios actuales de Finnhub/Yahoo Finance.

**Respuesta** `200 OK`:
```json
{
  "success": true,
  "userId": 1,
  "positions": [
    {
      "symbol": "SPY",
      "name": "S&P 500",
      "type": "ETF",
      "quantity": 2.5,
      "averagePrice": 480.50,
      "currentPrice": 495.20,
      "investmentValue": 1201.25,
      "currentValue": 1238.00,
      "gainLoss": 36.75,
      "gainLossPercent": 3.06,
      "currency": "USD"
    }
  ],
  "summary": {
    "totalInvested": 1201.25,
    "totalCurrentValue": 1238.00,
    "totalGainLoss": 36.75,
    "totalGainLossPercent": 3.06
  }
}
```

---

## Precios Crypto — `/api/coingecko`

### `GET /api/coingecko/price/{symbol}/{currency}` — Precio actual

Obtiene el precio actual de una criptomoneda desde CoinGecko.

**Ejemplo**: `GET /api/coingecko/price/BTC/EUR`

---

### `GET /api/coingecko/history/{symbol}/{days}/{currency}` — Historial de precios

Obtiene datos históricos con caché inteligente (24h en BD).

**Ejemplo**: `GET /api/coingecko/history/ETH/30/EUR`

**Respuesta**: JSON de CoinGecko con `prices: [[timestamp, price], ...]`

---

## Precios Crypto (BD) — `/api/crypto-prices`

### `GET /api/crypto-prices` — Todos los precios almacenados

### `GET /api/crypto-prices/{symbol}/{market}` — Precio específico

### `POST /api/crypto-prices` — Crear/actualizar precio

### `POST /api/crypto-prices/update-from-api` — Actualizar desde Alpha Vantage

### `DELETE /api/crypto-prices/{id}` — Eliminar registro

---

## Datos ETFs/Fondos — `/api/finnhub`

### `GET /api/finnhub/etf/{symbol}` — Cotización actual

**Ejemplo**: `GET /api/finnhub/etf/SPY`

**Respuesta** `200 OK`:
```json
{
  "c": 495.20,
  "h": 498.00,
  "l": 492.50,
  "o": 494.00,
  "pc": 493.80
}
```

> Internamente usa Yahoo Finance para mayor disponibilidad.

---

### `GET /api/finnhub/candles/{symbol}/{resolution}/{days}` — Datos históricos

**Ejemplo**: `GET /api/finnhub/candles/SPY/D/30`

**Respuesta**: JSON con arrays `c` (closes), `h` (highs), `l` (lows), `o` (opens), `t` (timestamps), `v` (volumes).

---

### `GET /api/finnhub/search/{query}` — Buscar ETF/acción

---

## Datos Alpha Vantage

### `GET /digitalCurrencyDaily?symbol={}&market={}` — Crypto diario

### `GET /equityDaily?symbol={}` — Acción/ETF diario

### `GET /etfProfile?symbol={}` — Perfil de ETF

---

## Análisis IA — `/api/ai`

### `POST /api/ai/analyze` — Análisis de tendencia

Envía datos de precios a Groq AI (Llama 3.3 70B) para obtener un análisis de mercado.

**Body** (JSON):
```json
{
  "symbol": "BTC",
  "type": "crypto",
  "prices": [52000, 52500, 51800, 53000, 52200],
  "language": "es"
}
```

**Respuesta** `200 OK`: Texto con análisis de tendencia, niveles de soporte/resistencia y recomendación (comprar/vender/mantener).

---

## Noticias — `/api/news`

### `GET /api/news/crypto` — Noticias de criptomonedas

Fuentes: CoinTelegraph (scraping) con fallback a CoinDesk RSS.

**Respuesta** `200 OK`:
```json
[
  {
    "title": "Bitcoin alcanza nuevos máximos...",
    "description": "El precio de Bitcoin...",
    "url": "https://cointelegraph.com/...",
    "imageUrl": "https://...",
    "source": "CoinTelegraph",
    "category": "crypto",
    "publishedAt": "2026-02-12"
  }
]
```

### `GET /api/news/economy` — Noticias de economía

Fuentes: Investing.com RSS con fallback a CNBC RSS.

### `GET /api/news/all` — Todas las noticias

Combina noticias crypto + economía.

> Las noticias se cachean en memoria durante 15 minutos.

---

*Volver al [README principal](../README.md)*
