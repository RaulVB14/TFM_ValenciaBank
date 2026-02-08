import React, { useState } from 'react';
import axios from 'axios';

function BuyFundForm({ userData, onPurchaseSuccess }) {
    const [symbol, setSymbol] = useState('SPY');
    const [quantity, setQuantity] = useState('');
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState('');
    const [messageType, setMessageType] = useState('');

    const fundList = {
        'ETFs Globales': [
            { symbol: 'VWRL', name: 'Vanguard FTSE All-World' },
        ],
        'USA Índices': [
            { symbol: 'SPY', name: 'S&P 500 (SPDR)' },
            { symbol: 'VOO', name: 'Vanguard S&P 500' },
            { symbol: 'IVV', name: 'iShares Core S&P 500' },
            { symbol: 'QQQ', name: 'Invesco QQQ (NASDAQ)' },
        ],
        'Sectores': [
            { symbol: 'XLK', name: 'Tecnología' },
            { symbol: 'XLF', name: 'Financiero' },
            { symbol: 'XLV', name: 'Salud' },
            { symbol: 'XLE', name: 'Energía' },
        ],
    };

    const getSelectedFundName = () => {
        for (const group of Object.values(fundList)) {
            const fund = group.find(f => f.symbol === symbol);
            if (fund) return fund.name;
        }
        return symbol;
    };

    const handleBuyFund = async (e) => {
        e.preventDefault();

        if (!quantity || parseFloat(quantity) <= 0) {
            setMessageType('error');
            setMessage('❌ Por favor ingresa una cantidad válida');
            return;
        }

        if (!userData || !userData.id) {
            setMessageType('error');
            setMessage('❌ Usuario no identificado');
            return;
        }

        setLoading(true);
        setMessage('');

        try {
            const token = localStorage.getItem('token');
            const response = await axios.post(
                'http://localhost:8080/fund/purchase/buy',
                {
                    userId: userData.id,
                    symbol: symbol,
                    name: getSelectedFundName(),
                    type: 'ETF',
                    quantity: parseFloat(quantity),
                    currency: 'USD'
                },
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            if (response.data.success) {
                setMessageType('success');
                const precio = response.data.pricePerUnit;
                setMessage(`✅ ¡Compra exitosa! Compraste ${quantity} participaciones de ${symbol} por ${response.data.totalCost.toFixed(2)} USD (${precio.toFixed(2)} USD/unidad)`);
                setQuantity('');

                if (onPurchaseSuccess) {
                    setTimeout(() => {
                        onPurchaseSuccess(response.data);
                    }, 500);
                }
            } else {
                setMessageType('error');
                setMessage(`❌ Error: ${response.data.error}`);
            }
        } catch (error) {
            console.error('Error al comprar fondo:', error);
            setMessageType('error');
            if (error.response?.data?.error) {
                setMessage(`❌ Error: ${error.response.data.error}`);
            } else {
                setMessage('❌ Error al procesar la compra. Intenta nuevamente.');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{
            background: 'linear-gradient(180deg, rgba(255,255,255,0.02), rgba(255,255,255,0.01))',
            border: '1px solid rgba(255,213,74,0.06)',
            borderRadius: '18px',
            padding: '24px',
            marginTop: '20px',
            color: '#e6e9ee'
        }}>
            <h2 style={{ marginTop: 0, color: '#fff', fontSize: '20px' }}>📈 Comprar Fondos & ETFs</h2>

            {userData && (
                <div style={{
                    background: 'rgba(255,213,74,0.08)',
                    padding: '12px',
                    borderRadius: '10px',
                    marginBottom: '16px',
                    fontSize: '14px'
                }}>
                    <strong>Saldo disponible:</strong> {userData.account?.balance ? userData.account.balance.toFixed(2) : '0.00'} EUR
                </div>
            )}

            <form onSubmit={handleBuyFund}>
                <div style={{ marginBottom: '16px' }}>
                    <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600' }}>
                        Fondo / ETF
                    </label>
                    <select
                        value={symbol}
                        onChange={(e) => setSymbol(e.target.value)}
                        disabled={loading}
                        style={{
                            width: '100%',
                            padding: '10px 14px',
                            background: 'rgba(255,213,74,0.06)',
                            color: '#ffd54a',
                            border: '1px solid rgba(255,213,74,0.12)',
                            borderRadius: '12px',
                            fontWeight: '600',
                            cursor: 'pointer',
                            boxSizing: 'border-box'
                        }}
                    >
                        {Object.entries(fundList).map(([group, funds]) => (
                            <optgroup key={group} label={group} style={{ background: '#0f1620', color: '#e6e9ee' }}>
                                {funds.map(fund => (
                                    <option key={fund.symbol} value={fund.symbol} style={{ background: '#0f1620', color: '#e6e9ee' }}>
                                        {fund.symbol} - {fund.name}
                                    </option>
                                ))}
                            </optgroup>
                        ))}
                    </select>
                </div>

                <div style={{ marginBottom: '16px' }}>
                    <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600' }}>
                        Cantidad de participaciones
                    </label>
                    <input
                        type="number"
                        step="0.01"
                        min="0"
                        value={quantity}
                        onChange={(e) => setQuantity(e.target.value)}
                        placeholder="Ej: 2.5"
                        disabled={loading}
                        style={{
                            width: '100%',
                            padding: '10px 14px',
                            background: 'rgba(255,255,255,0.03)',
                            color: '#e6e9ee',
                            border: '1px solid rgba(255,255,255,0.06)',
                            borderRadius: '12px',
                            boxSizing: 'border-box',
                            fontSize: '14px'
                        }}
                    />
                </div>

                {message && (
                    <div style={{
                        background: messageType === 'success'
                            ? 'rgba(34, 197, 94, 0.12)'
                            : 'rgba(255, 59, 48, 0.12)',
                        border: messageType === 'success'
                            ? '1px solid rgba(34, 197, 94, 0.3)'
                            : '1px solid rgba(255, 59, 48, 0.3)',
                        color: messageType === 'success' ? '#22c55e' : '#ff3b30',
                        padding: '12px 16px',
                        borderRadius: '10px',
                        marginBottom: '16px',
                        fontSize: '14px'
                    }}>
                        {message}
                    </div>
                )}

                <button
                    type="submit"
                    disabled={loading || !quantity}
                    style={{
                        width: '100%',
                        padding: '12px 18px',
                        background: loading || !quantity
                            ? 'rgba(255,213,74,0.3)'
                            : 'linear-gradient(90deg, var(--accent-strong), #ffb300)',
                        color: '#111',
                        border: 'none',
                        borderRadius: '12px',
                        fontWeight: '700',
                        fontSize: '16px',
                        cursor: loading || !quantity ? 'not-allowed' : 'pointer',
                        boxShadow: '0 10px 30px rgba(255,171,0,0.08)',
                        transition: 'all 160ms ease'
                    }}
                >
                    {loading ? '⏳ Procesando compra...' : '🚀 Comprar ahora'}
                </button>
            </form>
        </div>
    );
}

export default BuyFundForm;
