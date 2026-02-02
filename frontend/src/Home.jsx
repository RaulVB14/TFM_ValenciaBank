import "./css/Home.css";
import { useNavigate } from "react-router-dom";
import React, { useState, useEffect } from "react";
import axios from "axios";
import CryptoChart from "./components/CryptoGraphic.jsx";
import IndexedFundsGraphic from "./components/IndexedFundsGraphic.jsx";
import BuyCryptoForm from "./components/BuyCryptoForm.jsx";
import { FaSignOutAlt, FaUser, FaMoneyBillAlt, FaExchangeAlt, FaHistory } from "react-icons/fa";

function Home() {
    const navigate = useNavigate();
    const [userData, setUserData] = useState(null);
    const [chartData, setChartData] = useState({ dates: [], prices: [] });
    const [timeRange, setTimeRange] = useState("7");
    const [selectedCrypto, setSelectedCrypto] = useState("BTC");
    const [errorMessage, setErrorMessage] = useState("");
    
    // Estados para Fondos Indexados y ETFs
    const [selectedFund, setSelectedFund] = useState("VWRL");
    const [fundChartData, setFundChartData] = useState({ dates: [], prices: [] });
    const [fundTimeRange, setFundTimeRange] = useState("7");
    const [fundErrorMessage, setFundErrorMessage] = useState("");

    const Exit = () => navigate("/");
    const handleProfile = () => navigate("/home/Profile");
    const handleDeposit = () => navigate("/home/Deposit");
    const handleTransfer = () => navigate("/home/Transfer");
    const handleSummary = () => navigate("/home/Summary");

    // Cargar datos del usuario solo al montar
    useEffect(() => {
        fetchUserData();
    }, []);

    // Cargar datos de crypto solo cuando cambian timeRange o selectedCrypto
    useEffect(() => {
        fetchCryptoData();
    }, [timeRange, selectedCrypto]);

    // Cargar datos de fondos solo cuando cambian fundTimeRange o selectedFund
    useEffect(() => {
        fetchFundData();
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
            const market = "EUR";
            const token = localStorage.getItem("token");
            if (!token) {
                console.error("Token no encontrado en localStorage");
                return;
            }
            const response = await axios.get(
                `http://localhost:8080/digitalCurrencyDaily?symbol=${selectedCrypto}&market=${market}`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );
            
            // Verificar si hay un error en la respuesta
            if (response.data.error) {
                console.error("Error del servidor:", response.data.error);
                setErrorMessage(response.data.error);
                setChartData({ dates: [], prices: [] });
                return;
            }
            
            if (response.data && response.data["Time Series (Digital Currency Daily)"]) {
                processCryptoData(response.data);
            } else {
                console.error("Formato de datos inválido:", response.data);
                setErrorMessage("No hay datos disponibles para esta criptomoneda. Intenta con otra.");
                setChartData({ dates: [], prices: [] });
            }
        } catch (error) {
            console.error("Error al obtener datos de crypto:", error);
            setErrorMessage("Error al obtener datos. Intenta nuevamente.");
            setChartData({ dates: [], prices: [] });
        }
    };

    const processCryptoData = (data) => {
        const timeSeries = data["Time Series (Digital Currency Daily)"];
        if (!timeSeries) {
            console.error("No time series data available");
            return;
        }

        const dates = Object.keys(timeSeries);
        dates.sort((a, b) => new Date(a) - new Date(b));

        const numDays = parseInt(timeRange);
        const recentDates = dates.slice(Math.max(dates.length - numDays, 0));
        const prices = recentDates.map((date) => {
            const closePrice = timeSeries[date]["4. close"];
            return closePrice ? parseFloat(closePrice) : null;
        }).filter(price => price !== null);

        setChartData({ dates: recentDates, prices: prices });
    };

    const handleCryptoChange = (event) => {
        setSelectedCrypto(event.target.value);
    };

    // ✅ Obtener datos de Fondos Indexados y ETFs
    const fetchFundData = async () => {
        console.log("Obteniendo datos de fondo:", selectedFund);
        setFundErrorMessage("");
        try {
            const token = localStorage.getItem("token");
            if (!token) {
                console.error("Token no encontrado");
                return;
            }
            const response = await axios.get(
                `http://localhost:8080/equityDaily?symbol=${selectedFund}`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (response.data.error) {
                console.error("Error:", response.data.error);
                setFundErrorMessage(response.data.error);
                setFundChartData({ dates: [], prices: [] });
                return;
            }

            if (response.data && response.data["Time Series (Daily)"]) {
                processFundData(response.data);
            } else {
                console.error("Formato inválido:", response.data);
                setFundErrorMessage("No hay datos disponibles para este fondo. Intenta con otro.");
                setFundChartData({ dates: [], prices: [] });
            }
        } catch (error) {
            console.error("Error al obtener datos:", error);
            setFundErrorMessage("Error al obtener datos. Intenta nuevamente.");
            setFundChartData({ dates: [], prices: [] });
        }
    };

    const processFundData = (data) => {
        const timeSeries = data["Time Series (Daily)"];
        if (!timeSeries) {
            console.error("No time series data available");
            return;
        }

        const dates = Object.keys(timeSeries);
        dates.sort((a, b) => new Date(a) - new Date(b));

        const numDays = parseInt(fundTimeRange);
        const recentDates = dates.slice(Math.max(dates.length - numDays, 0));
        const prices = recentDates.map((date) => {
            const closePrice = timeSeries[date]["4. close"];
            return closePrice ? parseFloat(closePrice) : null;
        }).filter(price => price !== null);

        setFundChartData({ dates: recentDates, prices: prices });
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
            </div>

            <div className="balance-container">
                {userData && userData.account ? (
                    <div className="balance-info">
                        <p>Numero de cuenta: <strong>{userData.account.number}</strong></p>
                        <p className="balance-amount">Saldo: {userData.account.balance} €</p>
                    </div>
                ) : (
                    <p>Cargando datos...</p>
                )}
            </div>

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
                    <button onClick={() => setTimeRange("7")} className={timeRange === "7" ? "active" : ""}>1S</button>
                    <button onClick={() => setTimeRange("30")} className={timeRange === "30" ? "active" : ""}>1M</button>
                    <button onClick={() => setTimeRange("90")} className={timeRange === "90" ? "active" : ""}>3M</button>
                    <button onClick={() => setTimeRange("365")} className={timeRange === "365" ? "active" : ""}>1Y</button>
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
                        <option value="ECOS">iShares MSCI Spain (ECOS)</option>
                        <option value="XESC">iShares MSCI Spain (XESC)</option>
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
                        <option value="VYME">Vanguard Emerging Markets High Dividend (VYME)</option>
                        <option value="IUSA">iShares Core S&P U.S. Value (IUSA)</option>
                    </optgroup>
                </select>

                <div className="days-buttons">
                    <button onClick={() => setFundTimeRange("7")} className={fundTimeRange === "7" ? "active" : ""}>1S</button>
                    <button onClick={() => setFundTimeRange("30")} className={fundTimeRange === "30" ? "active" : ""}>1M</button>
                    <button onClick={() => setFundTimeRange("90")} className={fundTimeRange === "90" ? "active" : ""}>3M</button>
                    <button onClick={() => setFundTimeRange("365")} className={fundTimeRange === "365" ? "active" : ""}>1Y</button>
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

                <IndexedFundsGraphic symbol={selectedFund} dates={fundChartData.dates} prices={fundChartData.prices} />
                </div>
            </div>

            {/* 🛒 FORMULARIO DE COMPRA DE CRIPTOS */}
            <BuyCryptoForm userData={userData} onPurchaseSuccess={() => fetchUserData()} />
        </div>
    );
}

export default Home;