import React, { useState } from 'react';
import axios from 'axios';

function BuyCryptoForm({ userData, onPurchaseSuccess }) {
    const [symbol, setSymbol] = useState('BTC');
    const [quantity, setQuantity] = useState('');
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState('');
    const [messageType, setMessageType] = useState(''); // 'success', 'error', 'info'
    const [estimatedCost, setEstimatedCost] = useState(0);

    const cryptoList = {
        'BTC': 'Bitcoin',
        'ETH': 'Ethereum',
        'SOL': 'Solana',
        'ADA': 'Cardano',
        'USDT': 'Tether',
        'USDC': 'USD Coin',
        'BNB': 'Binance Coin',
        'XRP': 'Ripple',
        'DOGE': 'Dogecoin',
        'SHIB': 'Shiba Inu'
    };

    const handleQuantityChange = (e) => {
        const value = e.target.value;
        setQuantity(value);
        // Aquí podrías calcular el costo estimado si tienes el precio actual
    };

    const handleBuyCrypto = async (e) => {
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
                'http://localhost:8080/crypto/purchase/buy',
                {
                    userId: userData.id,
                    symbol: symbol,
                    quantity: parseFloat(quantity),
                    market: 'EUR'
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
                const precio = response.data.pricePerUnit || response.data.totalCost / response.data.quantity;
                console.log("✅ Compra exitosa - Precio usado:", precio);
                setMessage(`✅ ¡Compra exitosa! Compraste ${quantity} ${symbol} por ${response.data.totalCost.toFixed(2)} EUR (${precio.toFixed(2)} EUR/unidad)`);
                setQuantity('');
                
                // Actualizar datos del usuario después de la compra
                if (onPurchaseSuccess) {
                    console.log("Llamando a onPurchaseSuccess...");
                    // Dar un pequeño delay para asegurar que el backend procesó la transacción
                    setTimeout(() => {
                        onPurchaseSuccess(response.data);
                    }, 500);
                }
            } else {
                setMessageType('error');
                setMessage(`❌ Error: ${response.data.error}`);
            }
        } catch (error) {
            console.error('Error completo al comprar:', error);
            console.error('Status:', error.response?.status);
            console.error('URL:', error.config?.url);
            console.error('Headers:', error.config?.headers);
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
            <h2 style={{ marginTop: 0, color: '#fff', fontSize: '20px' }}>💰 Comprar Criptomonedas</h2>

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

            <form onSubmit={handleBuyCrypto}>
                <div style={{ marginBottom: '16px' }}>
                    <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600' }}>
                        Criptomoneda
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
                        {Object.entries(cryptoList).map(([key, value]) => (
                            <option key={key} value={key}>
                                {key} - {value}
                            </option>
                        ))}
                    </select>
                </div>

                <div style={{ marginBottom: '16px' }}>
                    <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600' }}>
                        Cantidad a comprar
                    </label>
                    <input
                        type="number"
                        step="0.0001"
                        min="0"
                        value={quantity}
                        onChange={handleQuantityChange}
                        placeholder="Ej: 0.5"
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

export default BuyCryptoForm;
