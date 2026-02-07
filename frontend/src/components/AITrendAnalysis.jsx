import React, { useState } from 'react';
import axios from 'axios';
import { FaRobot, FaTimes } from 'react-icons/fa';

/**
 * AITrendAnalysis - Component that provides AI-powered market trend analysis.
 * Uses Groq AI (Llama 3.3 70B) via the backend to generate insights about crypto/ETF trends.
 * 
 * Props:
 *   symbol   - The asset symbol (e.g., "BTC", "SPY")
 *   type     - "crypto" or "etf"
 *   prices   - Array of recent price data points
 */
function AITrendAnalysis({ symbol, type, prices }) {
    const [analysis, setAnalysis] = useState('');
    const [loading, setLoading] = useState(false);
    const [visible, setVisible] = useState(false);
    const [error, setError] = useState('');

    const handleAnalyze = async () => {
        if (!prices || prices.length === 0) {
            setError('No hay datos de precios disponibles para analizar.');
            setVisible(true);
            return;
        }

        setLoading(true);
        setError('');
        setAnalysis('');
        setVisible(true);

        try {
            const response = await axios.post('http://localhost:8080/api/ai/analyze', {
                symbol,
                type,
                prices: prices.slice(-50), // Send last 50 data points to keep payload small
                language: 'es'
            });

            if (response.data.success) {
                setAnalysis(response.data.analysis);
            } else {
                setError(response.data.error || 'Error al generar análisis.');
            }
        } catch (err) {
            console.error('Error AI analysis:', err);
            setError(err.response?.data?.error || 'Error al conectar con el servicio de IA.');
        } finally {
            setLoading(false);
        }
    };

    const handleClose = () => {
        setVisible(false);
        setAnalysis('');
        setError('');
    };

    return (
        <div style={{ marginTop: '12px' }}>
            <button
                onClick={handleAnalyze}
                disabled={loading}
                style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px',
                    padding: '10px 18px',
                    background: loading
                        ? 'rgba(138, 43, 226, 0.3)'
                        : 'linear-gradient(135deg, rgba(138, 43, 226, 0.8), rgba(75, 0, 130, 0.9))',
                    color: '#fff',
                    border: '1px solid rgba(138, 43, 226, 0.4)',
                    borderRadius: '12px',
                    cursor: loading ? 'wait' : 'pointer',
                    fontWeight: '600',
                    fontSize: '14px',
                    transition: 'all 200ms ease',
                    boxShadow: '0 4px 15px rgba(138, 43, 226, 0.2)',
                    width: '100%',
                    justifyContent: 'center'
                }}
            >
                <FaRobot />
                {loading ? 'Analizando tendencia...' : `Análisis IA de ${symbol}`}
            </button>

            {visible && (
                <div style={{
                    marginTop: '12px',
                    background: 'rgba(138, 43, 226, 0.08)',
                    border: '1px solid rgba(138, 43, 226, 0.25)',
                    borderRadius: '14px',
                    padding: '16px',
                    position: 'relative',
                    animation: 'fadeIn 0.3s ease'
                }}>
                    <button
                        onClick={handleClose}
                        style={{
                            position: 'absolute',
                            top: '8px',
                            right: '8px',
                            background: 'none',
                            border: 'none',
                            color: 'rgba(255,255,255,0.5)',
                            cursor: 'pointer',
                            fontSize: '14px',
                            padding: '4px'
                        }}
                    >
                        <FaTimes />
                    </button>

                    <div style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '8px',
                        marginBottom: '10px',
                        color: '#b388ff',
                        fontWeight: '700',
                        fontSize: '14px'
                    }}>
                        <FaRobot /> Análisis IA - {symbol}
                    </div>

                    {loading && (
                        <div style={{
                            color: '#b388ff',
                            fontSize: '14px',
                            textAlign: 'center',
                            padding: '20px 0'
                        }}>
                            🧠 Generando análisis con inteligencia artificial...
                        </div>
                    )}

                    {error && (
                        <div style={{
                            color: '#ff6b6b',
                            fontSize: '14px',
                            lineHeight: '1.5'
                        }}>
                            {error}
                        </div>
                    )}

                    {analysis && (
                        <div style={{
                            color: '#e6e9ee',
                            fontSize: '14px',
                            lineHeight: '1.7',
                            whiteSpace: 'pre-wrap'
                        }}>
                            {analysis}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}

export default AITrendAnalysis;
