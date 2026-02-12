import React, { useState, useEffect } from 'react';
import axios from 'axios';
import '../assets/css/NewsSection.css';

const NewsSection = () => {
    const [cryptoNews, setCryptoNews] = useState([]);
    const [economyNews, setEconomyNews] = useState([]);
    const [activeTab, setActiveTab] = useState('crypto');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchNews();
    }, []);

    const fetchNews = async () => {
        setLoading(true);
        setError(null);
        try {
            const [cryptoRes, economyRes] = await Promise.all([
                axios.get('http://localhost:8080/api/news/crypto'),
                axios.get('http://localhost:8080/api/news/economy')
            ]);
            setCryptoNews(cryptoRes.data);
            setEconomyNews(economyRes.data);
        } catch (err) {
            console.error('Error fetching news:', err);
            setError('No se pudieron cargar las noticias');
        } finally {
            setLoading(false);
        }
    };

    const currentNews = activeTab === 'crypto' ? cryptoNews : economyNews;

    const placeholderImg = 'data:image/svg+xml,' + encodeURIComponent(
        '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="220" fill="%230f1620"><rect width="400" height="220"/><text x="50%" y="50%" fill="%239aa4b2" font-size="16" text-anchor="middle" dy=".3em" font-family="sans-serif">Sin imagen</text></svg>'
    );

    return (
        <section className="news-section">
            <h2 className="news-title">
                <span className="news-title-icon">📰</span>
                Últimas Noticias
            </h2>

            <div className="news-tabs">
                <button
                    className={`news-tab ${activeTab === 'crypto' ? 'active' : ''}`}
                    onClick={() => setActiveTab('crypto')}
                >
                    🪙 Crypto
                </button>
                <button
                    className={`news-tab ${activeTab === 'economy' ? 'active' : ''}`}
                    onClick={() => setActiveTab('economy')}
                >
                    📊 Economía
                </button>
            </div>

            {loading && (
                <div className="news-loading">
                    <div className="news-spinner"></div>
                    <p>Cargando noticias...</p>
                </div>
            )}

            {error && (
                <div className="news-error">
                    <p>{error}</p>
                    <button className="news-retry-btn" onClick={fetchNews}>Reintentar</button>
                </div>
            )}

            {!loading && !error && currentNews.length === 0 && (
                <div className="news-empty">
                    <p>No hay noticias disponibles en este momento</p>
                </div>
            )}

            {!loading && !error && currentNews.length > 0 && (
                <div className="news-grid">
                    {currentNews.map((article, index) => (
                        <a
                            key={index}
                            href={article.url}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="news-card"
                        >
                            <div className="news-card-img-wrapper">
                                <img
                                    src={article.imageUrl || placeholderImg}
                                    alt={article.title}
                                    className="news-card-img"
                                    onError={(e) => { e.target.src = placeholderImg; }}
                                />
                                <span className="news-card-source">{article.source}</span>
                            </div>
                            <div className="news-card-body">
                                <h3 className="news-card-title">{article.title}</h3>
                                {article.description && (
                                    <p className="news-card-desc">{article.description}</p>
                                )}
                                {article.publishedAt && (
                                    <span className="news-card-date">{article.publishedAt}</span>
                                )}
                            </div>
                        </a>
                    ))}
                </div>
            )}
        </section>
    );
};

export default NewsSection;
