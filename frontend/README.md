# 🎨 ValenciaBank — Frontend

Interfaz de usuario desarrollada con **React 18** y **Vite 5**, proporcionando una experiencia bancaria moderna, responsiva y visualmente atractiva.

---

## 📑 Índice

- [Stack tecnológico](#-stack-tecnológico)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Instalación](#-instalación)
- [Ejecución](#-ejecución)
- [Páginas y rutas](#-páginas-y-rutas)
- [Componentes principales](#-componentes-principales)
- [Estilos](#-estilos)

---

## 🛠 Stack tecnológico

| Tecnología | Versión | Uso |
|------------|---------|-----|
| React | 18.3.1 | Librería UI |
| Vite | 5.4.1 | Bundler y dev server |
| React Router DOM | 6.26.1 | Enrutamiento SPA |
| Axios | 1.8.1 | Cliente HTTP |
| ECharts | 5.6.0 | Gráficos interactivos |
| React Icons | 5.5.0 | Iconos (FontAwesome, etc.) |
| ESLint | 9.9.0 | Linting de código |

---

## 📁 Estructura del proyecto

```
frontend/src/
├── Main.jsx                    → Punto de entrada React (ReactDOM.render)
├── App.jsx                     → Router principal + Landing page
├── pages/
│   ├── Login.jsx               → Inicio de sesión (DNI + contraseña)
│   ├── Register.jsx            → Registro de nuevo usuario
│   ├── Home.jsx                → Dashboard principal (gráficos, compra, saldos)
│   ├── Profile.jsx             → Perfil del usuario (edición de datos)
│   ├── Summary.jsx             → Resumen de cuenta y transacciones
│   ├── Deposit.jsx             → Formulario de depósito
│   ├── Transfer.jsx            → Formulario de transferencia
│   └── Portfolio.jsx           → Portfolio de inversiones (crypto + fondos)
├── components/
│   ├── CryptoGraphic.jsx       → Gráfico de precios de criptomonedas (ECharts)
│   ├── IndexedFundsGraphic.jsx → Gráfico de precios de ETFs/fondos (ECharts)
│   ├── BuyCryptoForm.jsx       → Formulario de compra de criptomonedas
│   ├── BuyFundForm.jsx         → Formulario de compra de fondos/ETFs
│   ├── PortfolioChart.jsx      → Gráfico de evolución del portfolio
│   ├── AITrendAnalysis.jsx     → Análisis de tendencia con IA (Groq)
│   ├── NewsSection.jsx         → Noticias crypto/economía (tabs)
│   ├── BalanceComponent.jsx    → Visualización de saldo
│   ├── SummaryComponent.jsx    → Resumen de transacciones
│   ├── DepositMoney.jsx        → Lógica de ingreso de dinero
│   ├── TransferComponent.jsx   → Lógica de transferencia
│   └── MovableDiv.jsx          → Componente arrastrable
├── services/
│   └── ApiService.js           → Centralización de llamadas HTTP al backend
└── assets/
    ├── css/                    → Hojas de estilo por página/componente
    └── images/                 → Recursos gráficos
```

---

## 📦 Instalación

```bash
cd frontend/
npm install
```

---

## 🚀 Ejecución

### Modo desarrollo (con hot reload)

```bash
npm run dev
```

Abre **http://localhost:5173** en tu navegador.

### Build de producción

```bash
npm run build
```

Los archivos se generan en `dist/`.

### Preview del build

```bash
npm run preview
```

---

## 🗺 Páginas y rutas

| Ruta | Página | Descripción |
|------|--------|-------------|
| `/` | Landing | Página de bienvenida con noticias, botones de login y registro |
| `/login` | Login | Inicio de sesión con DNI y contraseña |
| `/register` | Register | Formulario de registro de nuevo usuario |
| `/home` | Home | Dashboard principal: gráficos de mercado crypto y ETFs, botones de compra, saldo, análisis IA |
| `/home/Profile` | Profile | Datos personales del usuario con opción de edición |
| `/home/Summary` | Summary | Saldo de cuenta, historial de transacciones con filtros |
| `/home/Deposit` | Deposit | Formulario para ingresar dinero en la cuenta |
| `/home/Transfer` | Transfer | Formulario para transferir dinero entre cuentas |
| `/home/Portfolio` | Portfolio | Portfolio de inversiones: posiciones crypto y fondos, ganancias/pérdidas, gráfico de evolución |

---

## 🧩 Componentes principales

### Gráficos (ECharts)
- **`CryptoGraphic`** — Gráfico de velas/líneas de criptomonedas con datos de CoinGecko
- **`IndexedFundsGraphic`** — Gráfico de ETFs/fondos con datos de Yahoo Finance
- **`PortfolioChart`** — Evolución temporal del valor del portfolio

### Formularios de compra
- **`BuyCryptoForm`** — Selector de crypto, cantidad, vista previa de coste, compra
- **`BuyFundForm`** — Selector de fondos/ETFs agrupados por categoría, compra

### Inteligencia Artificial
- **`AITrendAnalysis`** — Envía precios al backend → Groq (Llama 3.3) → Muestra análisis con tendencia, soporte/resistencia y recomendación

### Noticias
- **`NewsSection`** — Tabs crypto/economía con grid de tarjetas de noticias

---

## 🎨 Estilos

Cada página tiene su archivo CSS dedicado en `assets/css/`:

| Archivo | Página/Componente |
|---------|-------------------|
| `index.css` | Estilos globales y landing page |
| `App.css` | Layout principal de la aplicación |
| `Home.css` | Dashboard con gráficos y controles |
| `Login.css` | Página de login |
| `Register.css` | Página de registro |
| `Profile.css` | Perfil de usuario |
| `Summary.css` | Resumen de cuenta |
| `Portfolio.css` | Portfolio de inversiones |
| `NewsSection.css` | Sección de noticias |

El diseño usa un tema **oscuro** con acentos dorados (`#ffd54a`) y azules (`#4fc3f7`), gradientes sutiles y animaciones CSS.

---

*Volver al [README principal](../README.md)*
