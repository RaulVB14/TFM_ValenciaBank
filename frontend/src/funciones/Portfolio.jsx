import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { FaArrowLeft } from 'react-icons/fa';
import PortfolioChart from '../components/PortfolioChart';
import '../css/Portfolio.css';

function Portfolio() {
    const [portfolio, setPortfolio] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showChart, setShowChart] = useState(false);
    const [chartTimeRange, setChartTimeRange] = useState('1M');
    const [chartData, setChartData] = useState({ dates: [], values: [], invested: [] });
    const [chartLoading, setChartLoading] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        fetchPortfolio();
    }, []);

    const timeRangeMap = { '1D': 1, '1S': 7, '1M': 30, '1Y': 365 };

    const fetchPortfolioHistory = async (range) => {
        try {
            setChartLoading(true);
            const dni = localStorage.getItem('dni');
            const token = localStorage.getItem('token');
            if (!dni || !token) return;

            const userResponse = await axios.get(
                `http://localhost:8080/user/get/${dni}`,
                { headers: { Authorization: `Bearer ${token}` } }
            );
            const userId = userResponse.data.id;
            const days = timeRangeMap[range] || 30;

            const response = await axios.get(
                `http://localhost:8080/portfolio/history/${userId}?days=${days}`,
                { headers: { Authorization: `Bearer ${token}` } }
            );

            if (response.data) {
                // Formatear timestamps a fechas legibles
                const rawDates = response.data.dates || [];
                const formattedDates = rawDates.map(ts => {
                    const d = new Date(Number(ts));
                    if (days <= 1) {
                        return d.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
                    } else if (days <= 7) {
                        return d.toLocaleDateString('es-ES', { weekday: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
                    } else if (days <= 30) {
                        return d.toLocaleDateString('es-ES', { day: 'numeric', month: 'short' });
                    } else {
                        return d.toLocaleDateString('es-ES', { day: 'numeric', month: 'short', year: '2-digit' });
                    }
                });
                setChartData({
                    dates: formattedDates,
                    values: response.data.values || [],
                    invested: response.data.invested || []
                });
            }
        } catch (err) {
            console.error('Error al cargar historial:', err);
        } finally {
            setChartLoading(false);
        }
    };

    const handleToggleChart = () => {
        const next = !showChart;
        setShowChart(next);
        if (next && chartData.dates.length === 0) {
            fetchPortfolioHistory(chartTimeRange);
        }
    };

    const handleTimeRange = (range) => {
        setChartTimeRange(range);
        fetchPortfolioHistory(range);
    };

    const fetchPortfolio = async () => {
        try {
            setLoading(true);
            const dni = localStorage.getItem('dni');
            const token = localStorage.getItem('token');

            if (!dni || !token) {
                setError('Usuario no autenticado');
                setLoading(false);
                return;
            }

            // Primero obtener el userId desde el DNI
            const userResponse = await axios.get(
                `http://localhost:8080/user/get/${dni}`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    }
                }
            );

            if (!userResponse.data || !userResponse.data.id) {
                setError('No se pudo obtener ID del usuario');
                setLoading(false);
                return;
            }

            const userId = userResponse.data.id;

            // Luego obtener el portafolio
            const response = await axios.get(
                `http://localhost:8080/portfolio/detailed/${userId}`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    }
                }
            );

            if (response.data.success) {
                setPortfolio(response.data);
                setError('');
            } else {
                setError(response.data.error || 'Error al cargar portafolio');
            }
        } catch (err) {
            console.error('Error:', err);
            setError(err.response?.data?.error || 'Error al obtener portafolio');
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return <div className="portfolio-container loading">Cargando portafolio...</div>;
    }

    if (error) {
        return (
            <div className="portfolio-container">
                <div className="error-message">❌ {error}</div>
                <button onClick={fetchPortfolio} className="refresh-btn">Reintentar</button>
            </div>
        );
    }

    if (!portfolio || !portfolio.positions || portfolio.positions.length === 0) {
        return (
            <div className="portfolio-container">
                <h2>Mi Portafolio</h2>
                <div className="empty-portfolio">
                    <p>📊 No tienes criptomonedas en tu portafolio</p>
                    <p>Compra tu primera cripto para comenzar</p>
                </div>
            </div>
        );
    }

    const summary = portfolio.summary;
    const gainLossColor = summary.totalGainLoss >= 0 ? '#00C853' : '#FF3333';
    const gainLossIcon = summary.totalGainLoss >= 0 ? '📈' : '📉';

    return (
        <div className="portfolio-container">
            <div className="portfolio-header">
                <div className="portfolio-header-left">
                    <button className="back-btn" onClick={() => navigate('/home')}><FaArrowLeft /> Volver</button>
                    <h2>💼 Mi Portafolio</h2>
                </div>
                <button onClick={fetchPortfolio} className="refresh-btn">🔄 Actualizar</button>
            </div>

            {/* RESUMEN */}
            <div className="portfolio-summary">
                <div className="summary-card">
                    <span className="summary-label">Invertido</span>
                    <span className="summary-value">{summary.totalInvested.toFixed(2)} EUR</span>
                </div>
                <div className="summary-card">
                    <span className="summary-label">Valor Actual</span>
                    <span className="summary-value highlight">{summary.totalCurrentValue.toFixed(2)} EUR</span>
                </div>
                <div className="summary-card" style={{ borderColor: gainLossColor }}>
                    <span className="summary-label">{gainLossIcon} Ganancia/Pérdida</span>
                    <span className="summary-value" style={{ color: gainLossColor }}>
                        {summary.totalGainLoss >= 0 ? '+' : ''}{summary.totalGainLoss.toFixed(2)} EUR
                    </span>
                    <span className="summary-percent" style={{ color: gainLossColor }}>
                        ({summary.totalGainLossPercent >= 0 ? '+' : ''}{summary.totalGainLossPercent.toFixed(2)}%)
                    </span>
                </div>
            </div>

            {/* GRÁFICO DE EVOLUCIÓN */}
            <div className="chart-section">
                <button className="chart-toggle-btn" onClick={handleToggleChart}>
                    {showChart ? '📉 Ocultar Gráfico' : '📊 Ver Evolución'}
                </button>

                {showChart && (
                    <div className="chart-panel">
                        <div className="chart-time-buttons">
                            {Object.keys(timeRangeMap).map((range) => (
                                <button
                                    key={range}
                                    className={`time-btn ${chartTimeRange === range ? 'active' : ''}`}
                                    onClick={() => handleTimeRange(range)}
                                >
                                    {range}
                                </button>
                            ))}
                        </div>
                        {chartLoading ? (
                            <div className="chart-loading">Cargando gráfico...</div>
                        ) : chartData.dates.length > 0 ? (
                            <PortfolioChart
                                dates={chartData.dates}
                                values={chartData.values}
                                invested={chartData.invested}
                            />
                        ) : (
                            <div className="chart-loading">No hay datos para el rango seleccionado</div>
                        )}
                    </div>
                )}
            </div>

            {/* TABLA DE POSICIONES */}
            <div className="positions-table">
                <table>
                    <thead>
                        <tr>
                            <th>Cripto</th>
                            <th>Cantidad</th>
                            <th>Precio Entrada</th>
                            <th>Precio Actual</th>
                            <th>Valor Invertido</th>
                            <th>Valor Actual</th>
                            <th>Ganancia/Pérdida</th>
                            <th>%</th>
                        </tr>
                    </thead>
                    <tbody>
                        {portfolio.positions.map((position, index) => {
                            const isGain = position.gainLoss >= 0;
                            const gainColor = isGain ? '#00C853' : '#FF3333';
                            const gainIcon = isGain ? '📈' : '📉';

                            return (
                                <tr key={index} className={isGain ? 'gain' : 'loss'}>
                                    <td className="symbol">
                                        <strong>{position.symbol}</strong>
                                    </td>
                                    <td className="quantity">
                                        {position.quantity.toFixed(8).replace(/\.?0+$/, '')}
                                    </td>
                                    <td className="price">
                                        {position.averagePrice.toFixed(2)} EUR
                                    </td>
                                    <td className="price highlight">
                                        {position.currentPrice.toFixed(2)} EUR
                                    </td>
                                    <td className="value">
                                        {position.investmentValue.toFixed(2)} EUR
                                    </td>
                                    <td className="value highlight">
                                        {position.currentValue.toFixed(2)} EUR
                                    </td>
                                    <td className="gainloss" style={{ color: gainColor }}>
                                        {gainIcon} {isGain ? '+' : ''}{position.gainLoss.toFixed(2)} EUR
                                    </td>
                                    <td className="percent" style={{ color: gainColor }}>
                                        {isGain ? '+' : ''}{position.gainLossPercent.toFixed(2)}%
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            </div>

            {/* INFO ADICIONAL */}
            <div className="portfolio-info">
                <p>💡 Última actualización: {new Date().toLocaleTimeString('es-ES')}</p>
                <p>📈 Los precios se actualizan en tiempo real desde CoinGecko</p>
            </div>
        </div>
    );
}

export default Portfolio;
