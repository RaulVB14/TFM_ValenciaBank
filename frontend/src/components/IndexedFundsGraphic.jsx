import React, { useEffect, useRef } from "react";
import * as echarts from "echarts";

const IndexedFundsGraphic = ({ symbol, dates, prices }) => {
    const chartRef = useRef(null);

    useEffect(() => {
        if (!chartRef.current || !dates || dates.length === 0) return;

        const myChart = echarts.init(chartRef.current);

        const option = {
            title: {
                text: `Fluctuación de ${symbol} (EUR)`,
                left: "auto",
                right: "120px",
                textStyle: { color: '#ffd54a', fontWeight: 700 }
            },
            tooltip: {
                trigger: "axis",
                backgroundColor: 'rgba(0,0,0,0.75)',
                textStyle: { color: '#fff' }
            },
            grid: {
                left: "5%",
                right: "5%",
                bottom: "8%",
                containLabel: true,
            },
            xAxis: {
                type: "category",
                data: dates,
                axisLabel: {
                    color: '#888',
                },
                axisLine: {
                    lineStyle: {
                        color: '#444',
                    },
                },
            },
            yAxis: {
                type: "value",
                axisLabel: {
                    color: '#888',
                },
                splitLine: {
                    lineStyle: {
                        color: '#333',
                    },
                },
            },
            series: [
                {
                    data: prices,
                    type: "line",
                    smooth: true,
                    itemStyle: {
                        color: '#ffd54a',
                    },
                    lineStyle: {
                        color: '#ffd54a',
                        width: 2,
                    },
                    areaStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: 'rgba(255, 213, 74, 0.3)' },
                            { offset: 1, color: 'rgba(255, 213, 74, 0.05)' },
                        ]),
                    },
                },
            ],
        };

        myChart.setOption(option);

        const handleResize = () => {
            myChart.resize();
        };

        window.addEventListener("resize", handleResize);
        return () => window.removeEventListener("resize", handleResize);
    }, [dates, prices, symbol]);

    return <div ref={chartRef} style={{ width: "100%", height: "420px" }}></div>;
};

export default IndexedFundsGraphic;
