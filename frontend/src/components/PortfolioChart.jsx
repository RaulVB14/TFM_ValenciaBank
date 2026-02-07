import React, { useEffect, useRef } from "react";
import * as echarts from "echarts";

const PortfolioChart = ({ dates, values, invested }) => {
    const chartRef = useRef(null);

    useEffect(() => {
        if (!chartRef.current || dates.length === 0) return;

        const myChart = echarts.init(chartRef.current);

        // Calcular ganancia/pérdida para cada punto
        const gainLoss = values.map((val, i) => {
            const inv = invested[i] || 0;
            return inv > 0 ? Math.round((val - inv) * 100) / 100 : 0;
        });

        // Color dinámico: verde si ganancias, rojo si pérdidas (último punto)
        const lastGain = gainLoss[gainLoss.length - 1] || 0;
        const mainColor = lastGain >= 0 ? '#00C853' : '#FF3333';
        const investedColor = '#4fc3f7';

        const option = {
            title: {
                text: 'Evolución del Portfolio',
                left: 'center',
                textStyle: { color: '#ffd54a', fontWeight: 700, fontSize: 16 }
            },
            tooltip: {
                trigger: 'axis',
                backgroundColor: 'rgba(0,0,0,0.85)',
                borderColor: 'rgba(255,213,74,0.2)',
                textStyle: { color: '#fff', fontSize: 13 },
                formatter: function (params) {
                    const date = params[0].axisValue;
                    let html = `<div style="font-weight:600;margin-bottom:6px">${date}</div>`;
                    params.forEach(p => {
                        const color = p.color;
                        const name = p.seriesName;
                        const val = p.value.toFixed(2);
                        html += `<div style="display:flex;align-items:center;gap:6px;margin:3px 0">
                            <span style="width:10px;height:10px;border-radius:50%;background:${color};display:inline-block"></span>
                            ${name}: <strong>${val} EUR</strong>
                        </div>`;
                    });
                    // Mostrar ganancia/pérdida
                    if (params.length >= 2) {
                        const valor = params[0].value;
                        const inv = params[1].value;
                        const diff = valor - inv;
                        const pct = inv > 0 ? ((diff / inv) * 100).toFixed(2) : '0.00';
                        const diffColor = diff >= 0 ? '#00C853' : '#FF3333';
                        const sign = diff >= 0 ? '+' : '';
                        html += `<div style="margin-top:6px;padding-top:6px;border-top:1px solid rgba(255,255,255,0.1);color:${diffColor};font-weight:700">
                            ${sign}${diff.toFixed(2)} EUR (${sign}${pct}%)
                        </div>`;
                    }
                    return html;
                }
            },
            legend: {
                top: 35,
                textStyle: { color: '#9aa4b2', fontSize: 12 },
                data: ['Valor Actual', 'Invertido']
            },
            grid: {
                left: '5%',
                right: '5%',
                top: 80,
                bottom: '10%',
                containLabel: true
            },
            xAxis: {
                type: 'category',
                data: dates,
                axisLabel: {
                    fontSize: 11,
                    fontWeight: '600',
                    color: '#9aa4b2',
                    rotate: 30
                },
                axisLine: { lineStyle: { color: 'rgba(230,233,238,0.12)' } }
            },
            yAxis: {
                type: 'value',
                axisLabel: {
                    fontSize: 11,
                    fontWeight: '600',
                    color: '#9aa4b2',
                    formatter: '{value} €'
                },
                axisLine: { lineStyle: { color: 'rgba(230,233,238,0.12)' } },
                splitLine: { lineStyle: { color: 'rgba(255,255,255,0.03)' } }
            },
            series: [
                {
                    name: 'Valor Actual',
                    data: values,
                    type: 'line',
                    smooth: true,
                    areaStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: mainColor === '#00C853' ? 'rgba(0,200,83,0.2)' : 'rgba(255,51,51,0.2)' },
                            { offset: 1, color: 'rgba(0,0,0,0)' }
                        ])
                    },
                    lineStyle: { width: 3, color: mainColor },
                    itemStyle: { color: mainColor },
                    symbol: 'none'
                },
                {
                    name: 'Invertido',
                    data: invested,
                    type: 'line',
                    smooth: true,
                    lineStyle: { width: 2, color: investedColor, type: 'dashed' },
                    itemStyle: { color: investedColor },
                    symbol: 'none'
                }
            ],
            backgroundColor: 'transparent'
        };

        myChart.setOption(option);

        const handleResize = () => myChart.resize();
        window.addEventListener('resize', handleResize);

        return () => {
            window.removeEventListener('resize', handleResize);
            myChart.dispose();
        };
    }, [dates, values, invested]);

    return <div ref={chartRef} style={{ width: '100%', height: '420px' }} />;
};

export default PortfolioChart;
