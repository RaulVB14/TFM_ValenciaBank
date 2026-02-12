package com.valenciaBank.valenciaBank.service;

import com.valenciaBank.valenciaBank.model.Account;
import com.valenciaBank.valenciaBank.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService - Tests unitarios")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("saveAccount guarda y retorna la cuenta")
    void saveAccount() {
        Account account = new Account();
        account.setNumber("1234567890123456");
        account.setBalance(1000.0);

        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });

        Account saved = accountService.saveAccount(account);

        assertNotNull(saved);
        assertEquals(1L, saved.getId());
        assertEquals(1000.0, saved.getBalance());
        verify(accountRepository).save(account);
    }

    @Test
    @DisplayName("findAccountByNumber retorna cuenta existente")
    void findAccountByNumber() {
        Account account = new Account();
        account.setNumber("1234567890123456");
        account.setBalance(5000.0);

        when(accountRepository.findByNumber("1234567890123456")).thenReturn(account);

        Account result = accountService.findAccountByNumber("1234567890123456");

        assertNotNull(result);
        assertEquals("1234567890123456", result.getNumber());
        assertEquals(5000.0, result.getBalance());
    }

    @Test
    @DisplayName("findAccountByNumber retorna null si no existe")
    void findAccountByNumberNoExiste() {
        when(accountRepository.findByNumber("0000000000000000")).thenReturn(null);

        Account result = accountService.findAccountByNumber("0000000000000000");

        assertNull(result);
    }
}
