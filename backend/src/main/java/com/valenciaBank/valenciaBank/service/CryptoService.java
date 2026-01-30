package com.valenciaBank.valenciaBank.service;

import com.valenciaBank.valenciaBank.model.Crypto;
import com.valenciaBank.valenciaBank.repository.CryptoRepository;
import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CryptoService {

    @Value("${api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final Map<String, String> cachedData = new HashMap<>();
    private final CryptoRepository cryptoRepository;

    public CryptoService(CryptoRepository cryptoRepository) {
        this.cryptoRepository = cryptoRepository;
        this.webClient = WebClient.create();
    }

    // ✅ Llama a la API externa Alpha Vantage
    public String llamarAPIExterna(String crytpoName, String market) {
        String url = "https://www.alphavantage.co/query?function=DIGITAL_CURRENCY_DAILY&symbol="
                    + crytpoName + "&market=" + market + "&apikey=" + apiKey;
        try {
            String data = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            cachedData.put(crytpoName + "-" + market, data);
            saveDataInOurBBDD(cachedData, crytpoName, market);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"Error al obtener datos de la API externa\"}";
        }
    }

    // ✅ Llama a la API local si es del mismo día, sino a la externa
    public String llamarAPI(String cryptoName, String market) {
        LocalDate lastDate = cryptoRepository.findLatestDate(cryptoName);

        if (lastDate != null && lastDate.equals(LocalDate.now())) {
            System.out.println("Obtengo datos de DB: " + cryptoName);
            return getCryptoDataFromDB(cryptoName, market);
        } else {
            System.out.println("Obtengo datos de API EXTERNA: " + cryptoName);
            return llamarAPIExterna(cryptoName, market);
        }
    }

    // ✅ NUEVO: Precio actual usando CoinGecko (sin límite de rate)
    public String getCurrentPrice(String symbol) {
        try {
            String url = "https://api.coingecko.com/api/v3/simple/price?ids=" 
                    + symbol.toLowerCase() 
                    + "&vs_currencies=usd,eur&include_market_cap=true&include_24hr_change=true";
            
            String data = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return data;
        } catch (Exception e) {
            return "{\"error\":\"Error obteniendo precio de " + symbol + "\"}";
        }
    }

    // ✅ NUEVO: Top 10 criptomonedas por market cap
    public String getTop10Cryptocurrencies() {
        try {
            String url = "https://api.coingecko.com/api/v3/coins/markets?"
                    + "vs_currency=eur&order=market_cap_desc&per_page=10&page=1";
            
            String data = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return data;
        } catch (Exception e) {
            return "{\"error\":\"Error obteniendo Top 10 criptos\"}";
        }
    }

    // ✅ NUEVO: Análisis de una criptomoneda (promedio, cambio %, volatilidad)
    public String getAnalysis(String symbol, int days) {
        try {
            List<Crypto> cryptoList = cryptoRepository.findByCryptoName(symbol);
            
            if (cryptoList.isEmpty()) {
                return "{\"error\":\"No data found for " + symbol + "\"}";
            }

            // Limitar por número de días
            List<Crypto> limitedList = cryptoList.stream()
                    .sorted(Comparator.comparing(Crypto::getDate).reversed())
                    .limit(days)
                    .collect(Collectors.toList());

            double avgPrice = limitedList.stream()
                    .mapToDouble(Crypto::getClose)
                    .average()
                    .orElse(0.0);

            double firstClose = limitedList.get(limitedList.size() - 1).getClose();
            double lastClose = limitedList.get(0).getClose();
            double percentChange = ((lastClose - firstClose) / firstClose) * 100;

            double volatility = calculateVolatility(limitedList);
            double highPrice = limitedList.stream().mapToDouble(Crypto::getHigh).max().orElse(0.0);
            double lowPrice = limitedList.stream().mapToDouble(Crypto::getLow).min().orElse(0.0);

            JSONObject result = new JSONObject();
            result.put("symbol", symbol);
            result.put("period_days", days);
            result.put("average_price", Math.round(avgPrice * 100.0) / 100.0);
            result.put("percent_change", Math.round(percentChange * 100.0) / 100.0);
            result.put("volatility", Math.round(volatility * 100.0) / 100.0);
            result.put("highest_price", highPrice);
            result.put("lowest_price", lowPrice);
            result.put("current_price", lastClose);

            return result.toString();
        } catch (Exception e) {
            return "{\"error\":\"Error en análisis de " + symbol + "\"}";
        }
    }

    // ✅ NUEVO: Historial filtrado por rango de fechas
    public String getHistoryByDateRange(String symbol, String startDateStr, String endDateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(startDateStr, formatter);
            LocalDate endDate = LocalDate.parse(endDateStr, formatter);

            List<Crypto> cryptoList = cryptoRepository.findByCryptoName(symbol);
            List<Crypto> filtered = cryptoList.stream()
                    .filter(c -> !c.getDate().isBefore(startDate) && !c.getDate().isAfter(endDate))
                    .sorted(Comparator.comparing(Crypto::getDate))
                    .collect(Collectors.toList());

            JSONObject result = new JSONObject();
            JSONObject timeSeries = new JSONObject();

            for (Crypto crypto : filtered) {
                JSONObject values = new JSONObject();
                values.put("open", crypto.getOpen());
                values.put("high", crypto.getHigh());
                values.put("low", crypto.getLow());
                values.put("close", crypto.getClose());
                values.put("volume", crypto.getVolume());
                timeSeries.put(crypto.getDate().format(formatter), values);
            }

            result.put("symbol", symbol);
            result.put("start_date", startDateStr);
            result.put("end_date", endDateStr);
            result.put("data", timeSeries);
            return result.toString();
        } catch (Exception e) {
            return "{\"error\":\"Error al filtrar historial\"}";
        }
    }

    // ✅ NUEVO: Comparar múltiples criptomonedas
    public String compareCryptos(String symbols) {
        try {
            String[] cryptoArray = symbols.split(",");
            JSONObject result = new JSONObject();
            JSONArray comparisons = new JSONArray();

            for (String crypto : cryptoArray) {
                crypto = crypto.trim();
                List<Crypto> cryptoList = cryptoRepository.findByCryptoName(crypto);
                
                if (!cryptoList.isEmpty()) {
                    Crypto latest = cryptoList.stream()
                            .max(Comparator.comparing(Crypto::getDate))
                            .orElse(cryptoList.get(0));

                    JSONObject obj = new JSONObject();
                    obj.put("symbol", crypto);
                    obj.put("latest_price", latest.getClose());
                    obj.put("date", latest.getDate());
                    comparisons.put(obj);
                }
            }

            result.put("comparison", comparisons);
            return result.toString();
        } catch (Exception e) {
            return "{\"error\":\"Error comparando criptos\"}";
        }
    }

    // Método auxiliar: Calcula la volatilidad (desviación estándar)
    private double calculateVolatility(List<Crypto> cryptoList) {
        double[] prices = cryptoList.stream().mapToDouble(Crypto::getClose).toArray();
        double mean = Arrays.stream(prices).average().orElse(0.0);
        double variance = Arrays.stream(prices).map(p -> Math.pow(p - mean, 2)).average().orElse(0.0);
        return Math.sqrt(variance);
    }

    // Guarda los datos en la base de datos
    public void saveDataInOurBBDD(Map<String, String> data, String crytpoName, String market) {
        String cryptoEurJson = data.get(crytpoName + "-" + market);
        JSONObject dataJSON = new JSONObject(cryptoEurJson);

        JSONObject timeSeries = dataJSON.getJSONObject("Time Series (Digital Currency Daily)");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (String date : timeSeries.keySet()) {
            JSONObject values = timeSeries.getJSONObject(date);

            Crypto crypto = new Crypto();
            crypto.setName(crytpoName);
            crypto.setDate(LocalDate.parse(date, formatter));
            crypto.setOpen(values.getDouble("1. open"));
            crypto.setHigh(values.getDouble("2. high"));
            crypto.setLow(values.getDouble("3. low"));
            crypto.setClose(values.getDouble("4. close"));
            crypto.setVolume(values.getDouble("5. volume"));
            cryptoRepository.save(crypto);
        }
    }

    // Recupera los datos de la base de datos
    private String getCryptoDataFromDB(String crytpoName, String market) {
        List<Crypto> cryptoList = cryptoRepository.findByCryptoName(crytpoName);
        if (cryptoList.isEmpty()) {
            return "{\"error\":\"No data found\"}";
        }

        JSONObject result = new JSONObject();
        JSONObject timeSeries = new JSONObject();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Crypto crytpo : cryptoList) {
            JSONObject values = new JSONObject();
            values.put("1. open", crytpo.getOpen());
            values.put("2. high", crytpo.getHigh());
            values.put("3. low", crytpo.getLow());
            values.put("4. close", crytpo.getClose());
            values.put("5. volume", crytpo.getVolume());
            timeSeries.put(crytpo.getDate().format(formatter), values);
        }

        result.put("Time Series (Digital Currency Daily)", timeSeries);
        return result.toString();
    }

    // Método para guardar JSON manual
    public void saveDataFromManualJson(String manualJson) {
        try {
            JSONObject dataJSON = new JSONObject(manualJson);
            JSONObject timeSeries = dataJSON.getJSONObject("Time Series (Digital Currency Daily)");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (String date : timeSeries.keySet()) {
                JSONObject values = timeSeries.getJSONObject(date);

                Crypto crypto = new Crypto();
                crypto.setDate(LocalDate.parse(date, formatter));
                crypto.setOpen(values.getDouble("1. open"));
                crypto.setHigh(values.getDouble("2. high"));
                crypto.setLow(values.getDouble("3. low"));
                crypto.setClose(values.getDouble("4. close"));
                crypto.setVolume(values.getDouble("5. volume"));
                cryptoRepository.save(crypto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}