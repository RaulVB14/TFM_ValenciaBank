import "../assets/css/Home.css";
import { useNavigate } from "react-router-dom";
import React, { useState, useEffect } from "react";
import axios from "axios";
import CryptoChart from "../components/CryptoGraphic.jsx";
import IndexedFundsGraphic from "../components/IndexedFundsGraphic.jsx";
import BuyCryptoForm from "../components/BuyCryptoForm.jsx";
import BuyFundForm from "../components/BuyFundForm.jsx";
import AITrendAnalysis from "../components/AITrendAnalysis.jsx";
import { FaSignOutAlt, FaUser, FaMoneyBillAlt, FaExchangeAlt, FaHistory, FaChartPie, FaChevronRight, FaShoppingCart } from "react-icons/fa";

function Home() {
    const navigate = useNavigate();
    const [userData, setUserData] = useState(null);
    const [chartData, setChartData] = useState({ dates: [], prices: [] });
    const [timeRange, setTimeRange] = useState("1"); // 1 día: máxima precisión (datos cada minuto)
    const [selectedCrypto, setSelectedCrypto] = useState("BTC");
    const [lastCryptoFetchTime, setLastCryptoFetchTime] = useState(0); // Para controlar rate limit
    const [errorMessage, setErrorMessage] = useState("");
    
    // Estados para Fondos Indexados y ETFs
    const [selectedFund, setSelectedFund] = useState("SPY"); // Cambiar a SPY (S&P 500) - compatible con Finnhub free tier
    const [fundChartData, setFundChartData] = useState({ dates: [], prices: [] });
    const [fundTimeRange, setFundTimeRange] = useState("1"); // 1 día: máxima precisión
    const [lastFundFetchTime, setLastFundFetchTime] = useState(0); // Para controlar rate limit
    const [fundErrorMessage, setFundErrorMessage] = useState("");
    const [finnhubDisabled, setFinnhubDisabled] = useState(false); // Flag de error persistente de API
    const [fundCurrency, setFundCurrency] = useState("USD");

    // Estado para el panel de saldos desplegable
    const [showBalanceDetail, setShowBalanceDetail] = useState(false);
    const [investedAmount, setInvestedAmount] = useState(0);
    const [showBuyModal, setShowBuyModal] = useState(false);
    const [showBuyFundModal, setShowBuyFundModal] = useState(false);

    const Exit = () => navigate("/");
    const handleProfile = () => navigate("/home/Profile");
    const handleDeposit = () => navigate("/home/Deposit");
    const handleTransfer = () => navigate("/home/Transfer");
    const handleSummary = () => navigate("/home/Summary");
    const handlePortfolio = () => navigate("/home/Portfolio");

    // Cargar datos del usuario y portafolio al montar
    useEffect(() => {
        fetchUserData();
        fetchInvestedAmount();
    }, []);

    // Cargar datos de crypto solo cuando cambian timeRange o selectedCrypto
    useEffect(() => {
        fetchCryptoData();
        setLastCryptoFetchTime(Date.now());

        // Auto-refresh INTELIGENTE cada minuto
        // Solo actualiza visualmente, no llama a API cada minuto
        if (timeRange === "1") {
            const interval = setInterval(() => {
                const timeSinceLastFetch = Date.now() - lastCryptoFetchTime;
                const tenMinutes = 10 * 60 * 1000; // Aumentado a 10 minutos para evitar rate limiting

                if (timeSinceLastFetch >= tenMinutes) {
                    // Después de 10 minutos, hacer un refresh real desde la API
                    console.log("🔄 Refresh real de CoinGecko (10 minutos)...");
                    fetchCryptoData();
                    setLastCryptoFetchTime(Date.now());
                } else {
                    // Antes de 10 minutos, solo actualizar visualmente (sin llamar API)
                    console.log("📊 Actualización visual del gráfico (sin llamada API)");
                    // El gráfico se actualiza automáticamente porque React re-renderiza
                }
            }, 60000); // 60 segundos

            return () => clearInterval(interval);
        }
    }, [timeRange, selectedCrypto]);

    // Cargar datos de ETFs/Fondos cuando cambian fundTimeRange o selectedFund
    useEffect(() => {
        fetchFundData();
        setLastFundFetchTime(Date.now());

        // Auto-refresh cada 10 minutos para intradía
        if (fundTimeRange === "1") {
            const interval = setInterval(() => {
                const timeSinceLastFetch = Date.now() - lastFundFetchTime;
                const tenMinutes = 10 * 60 * 1000;
                if (timeSinceLastFetch >= tenMinutes) {
                    console.log("🔄 Refresh real de ETFs (10 minutos)...");
                    fetchFundData();
                    setLastFundFetchTime(Date.now());
                }
            }, 60000);
            return () => clearInterval(interval);
        }
    }, [fundTimeRange, selectedFund]);

    const fetchUserData = async () => {
        try {
            const dni = localStorage.getItem("dni");
            const token = localStorage.getItem("token");
            if (!dni || !token) {
                console.error("DNI o token no encontrado en localStorage");
                return;
            }
            const response = await axios.get(`http://localhost:8080/user/get/${dni}`, {
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            });
            setUserData(response.data);
            console.log("✅ Datos del usuario actualizados:", response.data);
        } catch (error) {
            console.error("Error al obtener los datos del usuario", error);
        }
    };

    const fetchCryptoData = async () => {
        console.log("Obteniendo datos de:", selectedCrypto);
        setErrorMessage(""); // Limpiar errores previos
        try {
            // Estrategia inteligente para evitar rate limiting:
            // - "1D" envía "7D" al backend (datos cada hora, más estable)
            // - Luego filtramos solo las últimas 24 horas en el frontend
            let daysToFetch = parseInt(timeRange);
            if (daysToFetch === 1) {
                daysToFetch = 7; // Pedir 7 días (datos cada hora) en lugar de 1 día (datos cada minuto)
            }
            
            const response = await axios.get(
                `http://localhost:8080/api/coingecko/history/${selectedCrypto}/${daysToFetch}/eur`
            );
            
            // Verificar si hay un error en la respuesta
            if (response.data.error) {
                console.error("Error del servidor:", response.data.error);
                setErrorMessage(response.data.error);
                setChartData({ dates: [], prices: [] });
                return;
            }
            
            if (response.data && response.data.prices) {
                processCryptoData(response.data);
            } else {
                console.error("Formato de datos inválido:", response.data);
                setErrorMessage("No hay datos disponibles para esta criptomoneda. Intenta con otra.");
                setChartData({ dates: [], prices: [] });
            }
        } catch (error) {
            console.error("Error al obtener datos de crypto:", error);
            setErrorMessage("Error al obtener datos de CoinGecko. Intenta nuevamente.");
            setChartData({ dates: [], prices: [] });
        }
    };

    const processCryptoData = (data) => {
        if (!data.prices || data.prices.length === 0) {
            console.error("No prices data available");
            return;
        }

        // CoinGecko devuelve [timestamp, price] para cada dato
        const days = parseInt(timeRange);
        
        // Si se solicitó 1 día, filtrar solo las últimas 24 horas
        let filteredPrices = data.prices;
        if (days === 1) {
            const now = Date.now();
            const oneDayMs = 24 * 60 * 60 * 1000;
            filteredPrices = data.prices.filter(item => {
                const timestamp = item[0];
                return (now - timestamp) <= oneDayMs;
            });
            console.log(`📊 Filtrado: ${data.prices.length} datos → ${filteredPrices.length} últimas 24h`);
        }
        
        // Determinar el formato de fecha según el rango (hora de Madrid)
        const madridTZ = 'Europe/Madrid';
        let dateFormat;
        if (days === 1) {
            // Para 1 día: mostrar hora:minuto (HH:MM) - datos cada hora
            dateFormat = (timestamp) => {
                const date = new Date(timestamp);
                return date.toLocaleTimeString('es-ES', { 
                    hour: '2-digit', 
                    minute: '2-digit',
                    timeZone: madridTZ
                });
            };
        } else if (days === 7) {
            // Para 7 días: mostrar fecha y hora (DD/MM HH:MM)
            dateFormat = (timestamp) => {
                const date = new Date(timestamp);
                return date.toLocaleDateString('es-ES', { 
                    day: '2-digit', 
                    month: '2-digit',
                    timeZone: madridTZ
                }) + ' ' + date.toLocaleTimeString('es-ES', { 
                    hour: '2-digit', 
                    minute: '2-digit',
                    timeZone: madridTZ
                });
            };
        } else {
            // Para 30+ días: mostrar solo fecha (DD/MM)
            dateFormat = (timestamp) => {
                const date = new Date(timestamp);
                return date.toLocaleDateString('es-ES', { 
                    day: '2-digit', 
                    month: '2-digit',
                    timeZone: madridTZ
                });
            };
        }

        const dates = filteredPrices.map((item) => dateFormat(item[0]));
        const prices = filteredPrices.map((item) => parseFloat(item[1]));

        setChartData({ dates, prices });
    };

    const fetchInvestedAmount = async () => {
        try {
            const dni = localStorage.getItem("dni");
            const token = localStorage.getItem("token");
            if (!dni || !token) return;

            const userResponse = await axios.get(`http://localhost:8080/user/get/${dni}`, {
                headers: { Authorization: `Bearer ${token}` },
            });

            if (!userResponse.data?.id) return;

            const response = await axios.get(
                `http://localhost:8080/portfolio/detailed/${userResponse.data.id}`,
                { headers: { Authorization: `Bearer ${token}` } }
            );

            if (response.data.success && response.data.summary) {
                setInvestedAmount(response.data.summary.totalCurrentValue);
            }
        } catch (err) {
            console.error("Error al obtener datos de inversión:", err);
        }
    };

    const handleCryptoChange = (event) => {
        setSelectedCrypto(event.target.value);
    };

    // Obtener datos de Fondos Indexados y ETFs (Yahoo Finance)
    const fetchFundData = async () => {
        console.log("Obteniendo datos de fondo:", selectedFund);
        setFundErrorMessage("");
        try {
            const days = parseInt(fundTimeRange);
            const response = await axios.get(
                `http://localhost:8080/api/finnhub/candles/${selectedFund}/D/${days}`
            );

            if (response.data.error) {
                console.error("Error en respuesta:", response.data.error);
                setFundErrorMessage(response.data.error);
                setFundChartData({ dates: [], prices: [] });
                return;
            }

            if (response.data && response.data.c && response.data.c.length > 0) {
                processFundData(response.data);
                if (response.data.currency) {
                    setFundCurrency(response.data.currency);
                }
            } else {
                console.error("Formato inválido:", response.data);
                setFundErrorMessage("No hay datos disponibles para este fondo. Intenta con otro.");
                setFundChartData({ dates: [], prices: [] });
            }
        } catch (error) {
            console.error("Error al obtener datos de ETF:", error.message);
            let errorMsg = "Error al obtener datos del fondo.";
            if (error.response?.status === 429) {
                errorMsg = "Rate limit alcanzado. Intenta de nuevo en unos segundos.";
            }
            setFundErrorMessage(errorMsg);
            setFundChartData({ dates: [], prices: [] });
        }
    };

    const processFundData = (data) => {
        // Finnhub devuelve datos en formato: { c: [closes], h: [highs], l: [lows], o: [opens], t: [timestamps], v: [volumes] }
        if (!data.c || !data.t) {
            console.error("No candle data available");
            return;
        }

        // Determinar el formato de fecha según el rango (hora de Madrid)
        const days = parseInt(fundTimeRange);
        const madridTZ = 'Europe/Madrid';
        let dateFormat;
        if (days === 1) {
            // Para 1 día: datos cada 5 min, mostrar hora:minuto (HH:MM)
            dateFormat = (timestamp) => {
                const date = new Date(timestamp * 1000);
                return date.toLocaleTimeString('es-ES', { 
                    hour: '2-digit', 
                    minute: '2-digit',
                    timeZone: madridTZ
                });
            };
        } else if (days === 7) {
            // Para 7 días: datos cada 30 min, mostrar fecha y hora (DD/MM HH:MM)
            dateFormat = (timestamp) => {
                const date = new Date(timestamp * 1000);
                return date.toLocaleDateString('es-ES', { 
                    day: '2-digit', 
                    month: '2-digit',
                    timeZone: madridTZ
                }) + ' ' + date.toLocaleTimeString('es-ES', { 
                    hour: '2-digit', 
                    minute: '2-digit',
                    timeZone: madridTZ
                });
            };
        } else {
            // Para 30+ días: datos diarios, mostrar solo fecha (DD/MM)
            dateFormat = (timestamp) => {
                const date = new Date(timestamp * 1000);
                return date.toLocaleDateString('es-ES', { 
                    day: '2-digit', 
                    month: '2-digit',
                    timeZone: madridTZ
                });
            };
        }

        // Filtrar timestamps futuros (Yahoo Finance envía datos de toda la sesión de trading)
        const nowSeconds = Math.floor(Date.now() / 1000);
        const validIndices = [];
        for (let i = 0; i < data.t.length; i++) {
            if (data.t[i] <= nowSeconds) {
                validIndices.push(i);
            }
        }

        const dates = validIndices.map((i) => dateFormat(data.t[i]));
        const prices = validIndices.map((i) => parseFloat(data.c[i]));

        setFundChartData({ dates, prices });
    };

    const handleFundChange = (event) => {
        setSelectedFund(event.target.value);
    };

    return (
        <div className="home-container">
            <div className="header-icons">
                <button className="icon-btn" onClick={Exit} title="Salir">
                    <FaSignOutAlt />
                </button>
                <button className="icon-btn" onClick={handleProfile} title="Perfil">
                    <FaUser />
                </button>
                <button className="icon-btn" onClick={handleDeposit} title="Ingresar">
                    <FaMoneyBillAlt />
                </button>
                <button className="icon-btn" onClick={handleTransfer} title="Transferir">
                    <FaExchangeAlt />
                </button>
                <button className="icon-btn" onClick={handleSummary} title="Histórico">
                    <FaHistory />
                </button>
                <button className="icon-btn" onClick={handlePortfolio} title="Portafolio">
                    <FaChartPie />
                </button>
            </div>

            <div className="balance-wrapper">
                <div className="balance-container">
                    {userData && userData.account ? (
                        <div className="balance-info">
                            <p>Numero de cuenta: <strong>{userData.account.number}</strong></p>
                            <button
                                className={`balance-toggle-btn ${showBalanceDetail ? 'open' : ''}`}
                                onClick={() => setShowBalanceDetail(!showBalanceDetail)}
                                title="Ver detalle de saldos"
                            >
                                <FaChevronRight />
                            </button>
                        </div>
                    ) : (
                        <p>Cargando datos...</p>
                    )}
                </div>

                <div className={`balance-detail-panel ${showBalanceDetail ? 'visible' : ''}`}>
                    {userData && userData.account && (
                        <>
                            <div className="balance-detail-item">
                                <span className="balance-detail-label">Dinero líquido</span>
                                <span className="balance-detail-value liquid">{userData.account.balance.toFixed(2)} €</span>
                            </div>
                            <div className="balance-detail-divider"></div>
                            <div className="balance-detail-item">
                                <span className="balance-detail-label">Invertido</span>
                                <span className="balance-detail-value invested">{investedAmount.toFixed(2)} €</span>
                            </div>
                            <div className="balance-detail-divider"></div>
                            <div className="balance-detail-item total">
                                <span className="balance-detail-label">Total</span>
                                <span className="balance-detail-value">{(userData.account.balance + investedAmount).toFixed(2)} €</span>
                            </div>
                        </>
                    )}
                </div>

                <button className="buy-product-btn" onClick={() => setShowBuyModal(true)}>
                    <FaShoppingCart />
                    <span>Comprar Crypto</span>
                </button>
                <button className="buy-product-btn buy-fund-btn" onClick={() => setShowBuyFundModal(true)}>
                    <FaChartPie />
                    <span>Comprar Fondos & ETFs</span>
                </button>
            </div>

            {/* Modal de compra crypto */}
            {showBuyModal && (
                <div className="buy-modal-overlay" onClick={() => setShowBuyModal(false)}>
                    <div className="buy-modal-content" onClick={(e) => e.stopPropagation()}>
                        <button className="buy-modal-close" onClick={() => setShowBuyModal(false)}>×</button>
                        <BuyCryptoForm userData={userData} onPurchaseSuccess={(data) => {
                            fetchUserData();
                            fetchInvestedAmount();
                        }} />
                    </div>
                </div>
            )}

            {/* Modal de compra fondos/ETFs */}
            {showBuyFundModal && (
                <div className="buy-modal-overlay" onClick={() => setShowBuyFundModal(false)}>
                    <div className="buy-modal-content" onClick={(e) => e.stopPropagation()}>
                        <button className="buy-modal-close" onClick={() => setShowBuyFundModal(false)}>×</button>
                        <BuyFundForm userData={userData} onPurchaseSuccess={(data) => {
                            fetchUserData();
                            fetchInvestedAmount();
                        }} />
                    </div>
                </div>
            )}

            <div className="graphics-wrapper">
                <div className="crypto-graphic-container">
                    <h1>Mercado Crypto</h1>

                    <select value={selectedCrypto} onChange={handleCryptoChange}>
                    <optgroup label="🏆 Top Tier">
                        <option value="BTC">Bitcoin (BTC)</option>
                        <option value="ETH">Ethereum (ETH)</option>
                        <option value="USDT">Tether (USDT)</option>
                        <option value="USDC">USD Coin (USDC)</option>
                        <option value="BNB">Binance Coin (BNB)</option>
                    </optgroup>
                    <optgroup label="⚡ Layer 1 Blockchains">
                        <option value="SOL">Solana (SOL)</option>
                        <option value="ADA">Cardano (ADA)</option>
                        <option value="AVAX">Avalanche (AVAX)</option>
                        <option value="DOT">Polkadot (DOT)</option>
                        <option value="NEAR">NEAR Protocol (NEAR)</option>
                        <option value="COSMOS">Cosmos (ATOM)</option>
                        <option value="ALGO">Algorand (ALGO)</option>
                    </optgroup>
                    <optgroup label="🔗 Layer 2 & Scaling">
                        <option value="MATIC">Polygon (MATIC)</option>
                        <option value="ARB">Arbitrum (ARB)</option>
                        <option value="OP">Optimism (OP)</option>
                        <option value="LINSEA">Linea (LINEA)</option>
                    </optgroup>
                    <optgroup label="🔶 DeFi & Exchange">
                        <option value="UNI">Uniswap (UNI)</option>
                        <option value="AAVE">Aave (AAVE)</option>
                        <option value="CRV">Curve (CRV)</option>
                        <option value="LINK">Chainlink (LINK)</option>
                        <option value="SUSHI">SushiSwap (SUSHI)</option>
                    </optgroup>
                    <optgroup label="🌐 Web3 & Infrastructure">
                        <option value="FIL">Filecoin (FIL)</option>
                        <option value="ICP">Internet Computer (ICP)</option>
                        <option value="THO">Thorchain (RUNE)</option>
                        <option value="GRT">The Graph (GRT)</option>
                    </optgroup>
                    <optgroup label="🎮 Metaverse & Gaming">
                        <option value="AXS">Axie Infinity (AXS)</option>
                        <option value="SAND">The Sandbox (SAND)</option>
                        <option value="MANA">Decentraland (MANA)</option>
                    </optgroup>
                    <optgroup label="💰 Meme & Alternatives">
                        <option value="DOGE">Dogecoin (DOGE)</option>
                        <option value="SHIB">Shiba Inu (SHIB)</option>
                        <option value="PEPE">Pepe (PEPE)</option>
                        <option value="FLOKI">Floki Inu (FLOKI)</option>
                    </optgroup>
                    <optgroup label="📊 Privacy & Others">
                        <option value="XMR">Monero (XMR)</option>
                        <option value="ZEC">Zcash (ZEC)</option>
                        <option value="DASH">Dash (DASH)</option>
                        <option value="XRP">Ripple (XRP)</option>
                        <option value="XLM">Stellar (XLM)</option>
                        <option value="LTC">Litecoin (LTC)</option>
                        <option value="BCH">Bitcoin Cash (BCH)</option>
                    </optgroup>
                </select>

                <div className="days-buttons">
                    <button onClick={() => setTimeRange("1")} className={timeRange === "1" ? "active" : ""} title="Datos cada hora • Actualización visual cada minuto, API cada 10 min">1D</button>
                    <button onClick={() => setTimeRange("7")} className={timeRange === "7" ? "active" : ""} title="Datos cada hora">1S</button>
                    <button onClick={() => setTimeRange("30")} className={timeRange === "30" ? "active" : ""} title="Datos cada hora">1M</button>
                    <button onClick={() => setTimeRange("365")} className={timeRange === "365" ? "active" : ""} title="Datos diarios">1Y</button>
                </div>

                {errorMessage && (
                    <div style={{
                        background: "rgba(255, 59, 48, 0.12)",
                        border: "1px solid rgba(255, 59, 48, 0.3)",
                        color: "#ff3b30",
                        padding: "12px 16px",
                        borderRadius: "10px",
                        marginBottom: "12px",
                        fontSize: "14px"
                    }}>
                        ⚠️ {errorMessage}
                    </div>
                )}

                {chartData.dates.length === 0 && !errorMessage && (
                    <div style={{
                        background: "rgba(255, 213, 74, 0.08)",
                        border: "1px solid rgba(255, 213, 74, 0.2)",
                        color: "#ffd54a",
                        padding: "12px 16px",
                        borderRadius: "10px",
                        marginBottom: "12px",
                        fontSize: "14px"
                    }}>
                        ⏳ Cargando datos del gráfico...
                    </div>
                )}

                <CryptoChart dates={chartData.dates} prices={chartData.prices} selectedCrypto={selectedCrypto} />
                <AITrendAnalysis symbol={selectedCrypto} type="crypto" prices={chartData.prices} />
                </div>

                {/* ✅ NUEVA SECCIÓN: FONDOS INDEXADOS Y ETFS */}
                <div className="crypto-graphic-container">
                    <h1>Fondos Indexados & ETFs</h1>

                    <select value={selectedFund} onChange={handleFundChange}>
                    <optgroup label="🌍 ETFs Globales">
                        <option value="VWRL">Vanguard FTSE All-World (VWRL)</option>
                        <option value="EUNL">iShares Core MSCI World (EUNL)</option>
                        <option value="SWRD">iShares Core MSCI World (SWRD)</option>
                    </optgroup>
                    <optgroup label="🇺🇸 USA Índices">
                        <option value="SPY">S&P 500 (SPY)</option>
                        <option value="VOO">Vanguard S&P 500 (VOO)</option>
                        <option value="IVV">iShares Core S&P 500 (IVV)</option>
                    </optgroup>
                    <optgroup label="🇪🇺 Europa">
                        <option value="VEUR">Vanguard FTSE Developed Europe (VEUR)</option>
                    </optgroup>
                    <optgroup label="📊 Índices Principales">
                        <option value="^IBEX">IBEX 35 (España)</option>
                        <option value="^GSPC">S&P 500 (USA)</option>
                        <option value="^IXIC">NASDAQ (USA)</option>
                        <option value="^DJI">Dow Jones (USA)</option>
                    </optgroup>
                    <optgroup label="💼 Sectores Específicos">
                        <option value="XLK">Tecnología (XLK)</option>
                        <option value="XLF">Financiero (XLF)</option>
                        <option value="XLV">Salud (XLV)</option>
                        <option value="XLE">Energía (XLE)</option>
                    </optgroup>
                    <optgroup label="🎯 Dividendos">
                        <option value="IUSA">iShares Core S&P U.S. Value (IUSA)</option>
                    </optgroup>
                </select>

                <div className="days-buttons">
                    <button onClick={() => setFundTimeRange("1")} className={fundTimeRange === "1" ? "active" : ""} title="Datos cada hora • Actualización visual cada minuto, API cada 10 min">1D</button>
                    <button onClick={() => setFundTimeRange("7")} className={fundTimeRange === "7" ? "active" : ""} title="Datos cada hora">1S</button>
                    <button onClick={() => setFundTimeRange("30")} className={fundTimeRange === "30" ? "active" : ""} title="Datos cada hora">1M</button>
                    <button onClick={() => setFundTimeRange("365")} className={fundTimeRange === "365" ? "active" : ""} title="Datos diarios">1Y</button>
                </div>

                {fundErrorMessage && (
                    <div style={{
                        background: "rgba(255, 59, 48, 0.12)",
                        border: "1px solid rgba(255, 59, 48, 0.3)",
                        color: "#ff3b30",
                        padding: "12px 16px",
                        borderRadius: "10px",
                        marginBottom: "12px",
                        fontSize: "14px"
                    }}>
                        ⚠️ {fundErrorMessage}
                    </div>
                )}

                {fundChartData.dates.length === 0 && !fundErrorMessage && (
                    <div style={{
                        background: "rgba(255, 213, 74, 0.08)",
                        border: "1px solid rgba(255, 213, 74, 0.2)",
                        color: "#ffd54a",
                        padding: "12px 16px",
                        borderRadius: "10px",
                        marginBottom: "12px",
                        fontSize: "14px"
                    }}>
                        ⏳ Cargando datos del gráfico...
                    </div>
                )}

                <IndexedFundsGraphic symbol={selectedFund} dates={fundChartData.dates} prices={fundChartData.prices} currency={fundCurrency} />
                <AITrendAnalysis symbol={selectedFund} type="etf" prices={fundChartData.prices} />
                </div>
            </div>

        </div>
    );
}

export default Home;