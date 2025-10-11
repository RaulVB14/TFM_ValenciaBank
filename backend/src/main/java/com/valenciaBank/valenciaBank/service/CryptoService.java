package com.valenciaBank.valenciaBank.service;

import com.valenciaBank.valenciaBank.model.Crypto;
import com.valenciaBank.valenciaBank.repository.CryptoRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    // ✅ Llama a la API externa y guardamos la info en nuestra BBDD
    public String llamarAPIExterna(String crytpoName, String market) {
        String url = "https://www.alphavantage.co/query?function=DIGITAL_CURRENCY_DAILY&symbol="
                    + crytpoName + "&market=" + market + "&apikey=" + apiKey;
        try {
            String data = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // Bloquear hasta obtener la respuesta (para este ejemplo)

            cachedData.put(crytpoName + "-" + market, data);
            //GUARDAMOS LA INFO EN BBDD
            saveDataInOurBBDD(cachedData, crytpoName, market);

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"Error al obtener datos de la API externa\"}";
        }
    }

    // ✅ Llama a la API Local y así solo uso la API externa una vez al día
    public String llamarAPI(String cryptoName, String market) {
        LocalDate lastDate = cryptoRepository.findLatestDate(cryptoName);

        if (lastDate != null && lastDate.equals(LocalDate.now())) {
            System.out.println("Obtengo datos de mi DB");
            System.out.println("Que crypto me estoy obteniendo: " + cryptoName);
            return getCryptoDataFromDB(cryptoName, market);
        } else {
            System.out.println("Obtengo datos de mi API EXTERNA");
            return llamarAPIExterna(cryptoName, market);
        }
    }


    // Guarda los datos en la base de datos
    public void saveDataInOurBBDD(Map<String, String> data, String crytpoName, String market) {
        // Convertir el JSON a un objeto JSONObject
        //String btcEurJson = data.get("BTC-EUR");
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

    // Recupera los datos de la base de datos y construye el JSON
    private String getCryptoDataFromDB(String crytpoName, String market) {

        List<Crypto> cryptoList = cryptoRepository.findByCryptoName(crytpoName); // Recupera todos los datos
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

    //metodo de ayuda para meter el json manual (no usarlo)
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
