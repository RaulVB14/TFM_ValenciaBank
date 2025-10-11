package com.valenciaBank.valenciaBank.service;

import com.valenciaBank.valenciaBank.model.Account;
import com.valenciaBank.valenciaBank.model.Transaction;
import com.valenciaBank.valenciaBank.repository.AccountRepository;
import com.valenciaBank.valenciaBank.repository.TransactionReporsitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    @Autowired
    private TransactionReporsitory transactionReporsitory;

    @Autowired
    private AccountRepository accountRepository;

    public void saveTransaction(Transaction transaction) {
        // Obtener la cuenta de origen
        Account originAccount = accountRepository.findByNumber(transaction.getOriginAccount());

        // Obtener la cuenta de destino
        Account destinationAccount = accountRepository.findByNumber(transaction.getDestinationAccount());

        if (originAccount != null && destinationAccount != null) {
            // Verificar si la cuenta de origen tiene suficiente saldo
            if (originAccount.getBalance() >= transaction.getAmount()) {
                // Restar el monto de la cuenta de origen
                double newOriginBalance = originAccount.getBalance() - transaction.getAmount();
                originAccount.setBalance(newOriginBalance);

                // Sumar el monto a la cuenta de destino
                double newDestinationBalance = destinationAccount.getBalance() + transaction.getAmount();
                destinationAccount.setBalance(newDestinationBalance);

                // Guardar la transacción
                transactionReporsitory.save(transaction);

                // Actualizar el balance de ambas cuentas
                accountRepository.save(originAccount);
                accountRepository.save(destinationAccount);
            } else {
                throw new RuntimeException("Saldo insuficiente en la cuenta de origen");
            }
        } else {
            throw new RuntimeException("Cuenta de origen o destino no encontrada");
        }
    }
}
