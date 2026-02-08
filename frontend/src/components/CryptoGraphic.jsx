import React, { useEffect, useRef } from "react";
import * as echarts from "echarts";

const CryptoChart = ({ dates, prices, selectedCrypto }) => {
  const chartRef = useRef(null);

  useEffect(() => {
    if (!chartRef.current || dates.length === 0) return;

    const myChart = echarts.init(chartRef.current);

    const option = {
      title: {
        text: `Fluctuación de ${selectedCrypto} (EUR)`, // 📌 Hacemos el título dinámico
        left: 'center',
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
          fontSize: 12,
          fontWeight: "600",
          color: '#e6e9ee',
          rotate: 30,
        },
        axisLine: { lineStyle: { color: 'rgba(230,233,238,0.12)' } },
      },
      yAxis: {
        type: "value",
        axisLabel: {
          fontSize: 12,
          fontWeight: "600",
          color: '#e6e9ee',
        },
        axisLine: { lineStyle: { color: 'rgba(230,233,238,0.12)' } },
        splitLine: { lineStyle: { color: 'rgba(255,255,255,0.03)' } }
      },
      series: [
        {
          data: prices,
          type: "line",
          smooth: true,
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(255,213,74,0.18)' },
              { offset: 1, color: 'rgba(255,213,74,0.02)' }
            ])
          },
          lineStyle: { width: 3, color: '#ffd54a' },
          itemStyle: { color: '#ffd54a' }
        },
      ],
      backgroundColor: 'transparent'
    };

    myChart.setOption(option);

    window.addEventListener("resize", myChart.resize);

    return () => {
      window.removeEventListener("resize", myChart.resize);
      myChart.dispose();
    };
  }, [dates, prices, selectedCrypto]); // Agregamos selectedCrypto para actualizar el gráfico

  return <div ref={chartRef} style={{ width: "100%", height: "420px" }} />;
};


export default CryptoChart;
