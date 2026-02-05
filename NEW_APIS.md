# Integración de CoinGecko y Finnhub APIs

## Descripción

Se han integrado dos APIs externas para obtener datos de criptomonedas y ETFs en tiempo real:
- **CoinGecko**: Para criptomonedas (sin límite de llamadas en plan gratuito)
- **Finnhub**: Para ETFs y fondos indexados (60 llamadas/minuto)

## CoinGecko API (Criptomonedas)

### Características
- ✅ Sin límite de llamadas en plan gratuito
- ✅ Cobertura de 10,000+ criptomonedas
- ✅ Datos históricos de precios
- ✅ Información de mercado capitalizado
- ✅ Volumen de 24h y cambios

### Endpoints Disponibles

#### 1. Obtener precio actual
```
GET /api/coingecko/price/{symbol}/{currency}
```
**Ejemplo**: `GET /api/coingecko/price/BTC/EUR`

**Respuesta**:
```json
{
  "bitcoin": {
    "eur": 95000,
    "market_cap": {
      "eur": 1900000000000
    },
    "market_cap_rank": 1,
    "total_volume": {
      "eur": 45000000000
    },
    "high_24h": {
      "eur": 96000
    },
    "low_24h": {
      "eur": 94000
    },
    "price_change_24h": 500,
    "price_change_percentage_24h": 0.53
  }
}
```

#### 2. Obtener datos históricos
```
GET /api/coingecko/history/{symbol}/{days}/{currency}
```
**Ejemplo**: `GET /api/coingecko/history/BTC/30/EUR`

**Parámetros**:
- `symbol`: BTC, ETH, ADA, SOL, etc.
- `days`: 1, 7, 30, 90, 365
- `currency`: eur, usd, gbp, etc.

### Símbolos Soportados
BTC, ETH, ADA, SOL, XRP, DOT, DOGE, LINK, MATIC, UNI, AVAX, LTC, BCH, ETC, XLM, ATOM, NEAR, FLOW, THETA, VET, USDT, USDC, BNB, SHIB, PEPE, FLOKI, XMR, ZEC, DASH, ARB, OP, LINEA, AAVE, CRV, SUSHI, FIL, ICP, RUNE, GRT, AXS, SAND, MANA, ALGO

---

## Finnhub API (ETFs y Fondos)

### Configuración Requerida

1. **Obtén tu API key gratuita**:
   - Ir a https://finnhub.io/
   - Registrarse (es gratis)
   - Copiar tu API key

2. **Configura en `application.properties`**:
   ```properties
   finnhub.api.key=tu_api_key_aqui
   ```

### Características
- ✅ Datos de ETFs, fondos e índices
- ✅ Datos históricos de precios (velas)
- ✅ Búsqueda de símbolos
- ✅ 60 llamadas/minuto en plan gratuito
- ✅ Actualizaciones en tiempo real

### Endpoints Disponibles

#### 1. Obtener datos actuales de ETF
```
GET /api/finnhub/etf/{symbol}
```
**Ejemplo**: `GET /api/finnhub/etf/VWRL`

**Respuesta**:
```json
{
  "c": 92.50,
  "h": 93.10,
  "l": 92.00,
  "o": 92.80,
  "pc": 92.30,
  "t": 1707033600,
  "v": 1500000
}
```

#### 2. Obtener datos históricos (velas)
```
GET /api/finnhub/candles/{symbol}/{resolution}/{days}
```
**Ejemplo**: `GET /api/finnhub/candles/VWRL/D/30`

**Parámetros**:
- `symbol`: VWRL, SPY, VOO, etc.
- `resolution`: 1, 5, 15, 30, 60 (minutos), D (día), W (semana), M (mes)
- `days`: Número de días históricos a obtener

#### 3. Buscar ETF
```
GET /api/finnhub/search/{query}
```
**Ejemplo**: `GET /api/finnhub/search/VANGUARD`

### Símbolos ETFs Soportados
VWRL (Global), SPY (S&P 500), VOO (Vanguard S&P 500), IVV (iShares S&P 500), EUNL (MSCI World), SWRD (MSCI World), VEUR (Europe), ECOS (Spain), XESC (Spain), XLK (Tech), XLF (Finance), XLV (Healthcare), XLE (Energy), VYME (Dividends), IUSA (US Value)

---

## Comparativa con Alpha Vantage

| Característica | Alpha Vantage | CoinGecko | Finnhub |
|---|---|---|---|
| **Criptos** | ✅ | ✅⭐ | ❌ |
| **ETFs/Fondos** | ✅ | ❌ | ✅⭐ |
| **Límite Gratuito** | 25 calls/día | ∞ | 60/minuto |
| **Tiempo Real** | ~15 min delay | ✅ | ✅ |
| **API Key Requerida** | ✅ | ❌ | ✅ |

---

## Migración desde Alpha Vantage

### Para Criptomonedas
**Anterior**:
```
GET /digitalCurrencyDaily?symbol=BTC&market=EUR
```

**Nuevo**:
```
GET /api/coingecko/history/BTC/30/EUR
GET /api/coingecko/price/BTC/EUR
```

### Para ETFs/Fondos
**Anterior**:
```
GET /equityDaily?symbol=VWRL
```

**Nuevo**:
```
GET /api/finnhub/candles/VWRL/D/30
GET /api/finnhub/etf/VWRL
```

---

## Errores Comunes

### "API key de Finnhub no configurada"
**Solución**: Agrega tu API key en `application.properties`:
```properties
finnhub.api.key=tu_api_key
```

### "Error al obtener datos de Finnhub"
**Causas posibles**:
1. API key inválida o expirada
2. Excedido límite de 60 llamadas/minuto
3. Símbolo no existe (intenta buscarlo primero)

**Solución**: 
- Verifica tu API key en https://finnhub.io/dashboard
- Espera un minuto si excediste el límite
- Usa `/api/finnhub/search/{query}` para encontrar símbolos válidos

### "Símbolo no encontrado en CoinGecko"
**Solución**: CoinGecko soporta 10,000+ criptos. Si el símbolo no funciona:
1. Asegúrate de usar mayúsculas (BTC no btc)
2. Usa `/api/coingecko/price/bitcoin/EUR` (nombre en lugar de símbolo)
3. Verifica en https://coingecko.com que el símbolo existe

---

## Plan de Migración en Frontend

Actualiza tus llamadas API en el frontend:

```javascript
// Antes (Alpha Vantage)
GET http://localhost:8080/digitalCurrencyDaily?symbol=BTC&market=EUR

// Después (CoinGecko)
GET http://localhost:8080/api/coingecko/history/BTC/30/EUR
GET http://localhost:8080/api/coingecko/price/BTC/EUR
```

---

## Notas Importantes

✅ **CoinGecko es GRATUITO** - No necesita API key
✅ **Finnhub requiere registro GRATUITO** - Solo crea una cuenta
✅ Ambas APIs tienen documentación excelente en sus sitios
✅ Las pruebas se pueden hacer en Postman o desde el navegador
