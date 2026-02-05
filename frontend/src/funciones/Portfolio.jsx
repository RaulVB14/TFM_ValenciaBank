import React, { useState, useEffect } from 'react';
import axios from 'axios';
import '../css/Portfolio.css';

function Portfolio() {
    const [portfolio, setPortfolio] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        fetchPortfolio();
    }, []);

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
                <h2>💼 Mi Portafolio</h2>
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
