package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.model.Account;
import com.valenciaBank.valenciaBank.model.Transaction;
import com.valenciaBank.valenciaBank.model.User;
import com.valenciaBank.valenciaBank.service.AccountService;
import com.valenciaBank.valenciaBank.service.TransactionService;
import com.valenciaBank.valenciaBank.service.UserServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionsController - Tests unitarios")
class TransactionsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionsService;

    @Mock
    private UserServiceImplementation userService;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TransactionsController transactionsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionsController).build();
    }

    @Test
    @DisplayName("POST /transactions/add depósito exitoso (misma cuenta)")
    void addDepositExitoso() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setDni("12345678A");

        Account account = new Account();
        account.setNumber("ES1234567890123456");
        account.setBalance(5000.0);

        when(userService.getUser("12345678A")).thenReturn(user);
        when(accountService.findAccountByNumber("ES1234567890123456")).thenReturn(account);

        String request = "{" +
                "\"transaction\":{\"originAccount\":\"ES1234567890123456\",\"destinationAccount\":\"ES1234567890123456\",\"amount\":500.0}," +
                "\"user\":\"12345678A\"" +
                "}";

        mockMvc.perform(post("/transactions/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());

        verify(transactionsService).saveTransaction(any(Transaction.class));
    }

    @Test
    @DisplayName("POST /transactions/add transferencia entre cuentas diferentes")
    void addTransferenciaEntreCuentas() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setDni("12345678A");

        Account originAccount = new Account();
        originAccount.setNumber("ES1111111111111111");
        originAccount.setBalance(10000.0);

        Account destAccount = new Account();
        destAccount.setNumber("ES2222222222222222");
        destAccount.setBalance(2000.0);

        when(userService.getUser("12345678A")).thenReturn(user);
        when(accountService.findAccountByNumber("ES1111111111111111")).thenReturn(originAccount);
        when(accountService.findAccountByNumber("ES2222222222222222")).thenReturn(destAccount);

        String request = "{" +
                "\"transaction\":{\"originAccount\":\"ES1111111111111111\",\"destinationAccount\":\"ES2222222222222222\",\"amount\":1000.0}," +
                "\"user\":\"12345678A\"" +
                "}";

        mockMvc.perform(post("/transactions/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());

        verify(transactionsService).saveTransaction(any(Transaction.class));
    }
 }
