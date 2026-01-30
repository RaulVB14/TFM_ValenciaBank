package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173/home") //ESTO ES PARA QUITAR EL FALLO CORS
public class API_InversionesCryptoController {

    @Autowired
    private  CryptoService cryptoService;

    // ✅ GET - Devuelve datos de criptomonedas
    @GetMapping("/digitalCurrencyDaily")
    public ResponseEntity<String> getDigitalCurrencyDaily(@RequestParam String symbol, @RequestParam String market) {
        String data = cryptoService.llamarAPI(symbol, market);
        return ResponseEntity.ok(data);
    }

    // ✅ GET - Devuelve datos de ETFs e Índices Bursátiles (NEW)
    @GetMapping("/equityDaily")
    public ResponseEntity<String> getEquityDaily(@RequestParam String symbol) {
        String data = cryptoService.llamarEquityAPI(symbol);
        return ResponseEntity.ok(data);
    }

    // ✅ GET - Devuelve perfil de un ETF con sus holdings (NEW)
    @GetMapping("/etfProfile")
    public ResponseEntity<String> getETFProfile(@RequestParam String symbol) {
        String data = cryptoService.getETFProfile(symbol);
        return ResponseEntity.ok(data);
    }

    // Meter el json manualmente (no usarlo)
    @PostMapping("/datos")
    public ResponseEntity<String> recibirJson(@RequestBody String json) {
        try {
            cryptoService.saveDataFromManualJson(json);
            return ResponseEntity.ok("Datos de Bitcoin guardados correctamente");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al procesar el JSON");
        }
    }


}