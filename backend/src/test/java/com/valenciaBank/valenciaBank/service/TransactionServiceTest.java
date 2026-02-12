package com.valenciaBank.valenciaBank.service;

import com.valenciaBank.valenciaBank.model.Account;
import com.valenciaBank.valenciaBank.model.Transaction;
import com.valenciaBank.valenciaBank.repository.AccountRepository;
import com.valenciaBank.valenciaBank.repository.TransactionReporsitory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService - Tests unitarios")
class TransactionServiceTest {

    @Mock
    private TransactionReporsitory transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("saveTransaction transfiere saldo entre cuentas correctamente")
    void saveTransactionExitoso() {
        Account origin = new Account();
        origin.setNumber("1111111111111111");
        origin.setBalance(1000.0);

        Account destination = new Account();
        destination.setNumber("2222222222222222");
        destination.setBalance(500.0);

        Transaction transaction = new Transaction();
        transaction.setOriginAccount("1111111111111111");
        transaction.setDestinationAccount("2222222222222222");
        transaction.setAmount(300.0);

        when(accountRepository.findByNumber("1111111111111111")).thenReturn(origin);
        when(accountRepository.findByNumber("2222222222222222")).thenReturn(destination);

        transactionService.saveTransaction(transaction);

        assertEquals(700.0, origin.getBalance(), 0.01);
        assertEquals(800.0, destination.getBalance(), 0.01);
        verify(transactionRepository).save(transaction);
        verify(accountRepository).save(origin);
        verify(accountRepository).save(destination);
    }

    @Test
    @DisplayName("saveTransaction lanza excepción con saldo insuficiente")
    void saveTransactionSaldoInsuficiente() {
        Account origin = new Account();
        origin.setNumber("1111111111111111");
        origin.setBalance(100.0);

        Account destination = new Account();
        destination.setNumber("2222222222222222");
        destination.setBalance(500.0);

        Transaction transaction = new Transaction();
        transaction.setOriginAccount("1111111111111111");
        transaction.setDestinationAccount("2222222222222222");
        transaction.setAmount(500.0); // Más que el saldo

        when(accountRepository.findByNumber("1111111111111111")).thenReturn(origin);
        when(accountRepository.findByNumber("2222222222222222")).thenReturn(destination);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionService.saveTransaction(transaction));

        assertEquals("Saldo insuficiente en la cuenta de origen", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveTransaction lanza excepción si cuenta origen no existe")
    void saveTransactionCuentaOrigenNoExiste() {
        Account destination = new Account();
        destination.setNumber("2222222222222222");

        Transaction transaction = new Transaction();
        transaction.setOriginAccount("0000000000000000");
        transaction.setDestinationAccount("2222222222222222");
        transaction.setAmount(100.0);

        when(accountRepository.findByNumber("0000000000000000")).thenReturn(null);
        when(accountRepository.findByNumber("2222222222222222")).thenReturn(destination);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionService.saveTransaction(transaction));

        assertEquals("Cuenta de origen o destino no encontrada", exception.getMessage());
    }

    @Test
    @DisplayName("saveTransaction lanza excepción si cuenta destino no existe")
    void saveTransactionCuentaDestinoNoExiste() {
        Account origin = new Account();
        origin.setNumber("1111111111111111");

        Transaction transaction = new Transaction();
        transaction.setOriginAccount("1111111111111111");
        transaction.setDestinationAccount("0000000000000000");
        transaction.setAmount(100.0);

        when(accountRepository.findByNumber("1111111111111111")).thenReturn(origin);
        when(accountRepository.findByNumber("0000000000000000")).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionService.saveTransaction(transaction));

        assertEquals("Cuenta de origen o destino no encontrada", exception.getMessage());
    }
}
