package com.valenciaBank.valenciaBank.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valenciaBank.valenciaBank.model.Account;
import com.valenciaBank.valenciaBank.model.Transaction;
import com.valenciaBank.valenciaBank.model.TransactionData;
import com.valenciaBank.valenciaBank.model.User;
import com.valenciaBank.valenciaBank.service.AccountService;
import com.valenciaBank.valenciaBank.service.TransactionService;
import com.valenciaBank.valenciaBank.service.UserServiceImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;




@RestController
@RequestMapping("/transactions")
@CrossOrigin
public class TransactionsController {



    @Autowired
    private TransactionService transactionsService;

    @Autowired
    private UserServiceImplementation userService;

    @Autowired
    private AccountService accountService;


    //En este metodo me esta entrando el json de la web al servidor refactorizar codigo pero funciona como dios
    @PostMapping("/add")
    public Transaction addDeposit(@RequestBody String inputWeb) {
        TransactionData transactionData;
        System.out.println("lo que me pasan es" + inputWeb);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            transactionData = objectMapper.readValue(inputWeb, TransactionData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Transaction transaction = transactionData.getTransaction();
        User user = userService.getUser(transactionData.getUser());

        System.out.println("Lo que nos envia la web:");
        System.out.println("Origin Account: " + transaction.getOriginAccount());
        System.out.println("Destination Account: " + transaction.getDestinationAccount());
        System.out.println("Amount: " + transaction.getAmount());
        System.out.println("Date: " + transaction.getDate());
        System.out.println("dniUser: " + user.getDni());

        Account destinationAccount = accountService.findAccountByNumber(transaction.getDestinationAccount());
        Account originAccount = accountService.findAccountByNumber(transaction.getOriginAccount());
        System.out.println("user que obtenemos: " + user);


        try{
            if (user.getId() != null){
                transaction.setUser(user);
                if (transaction.getOriginAccount().equalsIgnoreCase(transaction.getDestinationAccount())){ // si es un ingreso solamente se suma el dinero de su misma cuenta
                    destinationAccount.setBalance(destinationAccount.getBalance() + transaction.getAmount());
                    user.setTransactions(user.getTransactions());
                    transactionsService.saveTransaction(transaction);
                }else { // si es una transferencia de una cuenta a otra hay que quitar el dinero de la cuenta origen y sumarle lo que tiene mas lo que le llega a la destination account
                    originAccount.setBalance(originAccount.getBalance() - transaction.getAmount());
                    destinationAccount.setBalance(destinationAccount.getBalance() + transaction.getAmount());
                    user.setTransactions(user.getTransactions());
                    transactionsService.saveTransaction(transaction);
                }

            }else{
                System.out.println("el usuario que se esta pasando con el dni null por lo tanto no se esta cogiendo bien la info");
            }
        }catch (Exception e){
            System.out.println("hubo un fallo en el servidor");
            System.out.println(e.getMessage());
        }
        return transaction;
    }


 //revisar metodo
    @GetMapping("/getFilter")
    public List<Transaction> getFilterTransactions(
            @RequestBody List<Transaction> inputWeb, // Recibe la lista de transacciones
            @RequestParam(required = false) String startDate, // Fecha de inicio
            @RequestParam(required = false) String endDate, // Fecha de fin
            @RequestParam(required = false) Double minAmount, // Monto mínimo
            @RequestParam(required = false) Double maxAmount // Monto máximo
    ) {
        // Formato de fecha esperado en las transacciones
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Inicializa el filtro de transacciones
        List<Transaction> filteredTransactions = inputWeb;

        // Verifica si se proporcionó el filtro de fecha de inicio
        if (startDate != null && !startDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate, formatter);
            filteredTransactions = filteredTransactions.stream()
                    .filter(transaction -> {
                        LocalDate transactionDate = LocalDate.parse((CharSequence) transaction.getDate(), formatter);
                        return !transactionDate.isBefore(start);
                    })
                    .collect(Collectors.toList());
        }

        // Verifica si se proporcionó el filtro de fecha de fin
        if (endDate != null && !endDate.isEmpty()) {
            LocalDate end = LocalDate.parse(endDate, formatter);
            filteredTransactions = filteredTransactions.stream()
                    .filter(transaction -> {
                        LocalDate transactionDate = LocalDate.parse((CharSequence) transaction.getDate(), formatter);
                        return !transactionDate.isAfter(end);
                    })
                    .collect(Collectors.toList());
        }

        // Verifica si se proporcionó el filtro de monto mínimo
        if (minAmount != null) {
            filteredTransactions = filteredTransactions.stream()
                    .filter(transaction -> transaction.getAmount() >= minAmount)
                    .collect(Collectors.toList());
        }

        // Verifica si se proporcionó el filtro de monto máximo
        if (maxAmount != null) {
            filteredTransactions = filteredTransactions.stream()
                    .filter(transaction -> transaction.getAmount() <= maxAmount)
                    .collect(Collectors.toList());
        }

        // Devuelve las transacciones filtradas
        return filteredTransactions;
    }



}
