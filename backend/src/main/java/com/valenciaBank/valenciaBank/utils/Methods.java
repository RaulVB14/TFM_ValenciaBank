package com.valenciaBank.valenciaBank.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.valenciaBank.valenciaBank.model.Transaction;
import com.valenciaBank.valenciaBank.model.TransactionData;

import java.io.File;
import java.io.IOException;

public class Methods {


    public static String generateAccountNumber() {
        StringBuilder accountNumber = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            accountNumber.append((int) (Math.random() * 10));
        }
        return accountNumber.toString();
    }

    //NO SE USA PERO CREO QUE ESTO NOS SERVIRA EN EL FUTURO
    public static void readJSON(File jsonFile){
        try {
            // Crear un objeto ObjectMapper para deserializar el JSON
            ObjectMapper objectMapper = new ObjectMapper();

            // Leer el archivo JSON (o JSON en formato de String)
            TransactionData transactionData = objectMapper.readValue(jsonFile, TransactionData.class);

            // Extraer la información de la transacción
            Transaction transaction = transactionData.getTransaction();
            String user = transactionData.getUser();

            // Mostrar la información extraída
            System.out.println("Transaction Information:");
            System.out.println("Origin Account: " + transaction.getOriginAccount());
            System.out.println("Destination Account: " + transaction.getDestinationAccount());
            System.out.println("Amount: " + transaction.getAmount());
            System.out.println("Date: " + transaction.getDate());
            System.out.println("User: " + user);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
