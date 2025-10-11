package com.valenciaBank.valenciaBank.service;

import com.valenciaBank.valenciaBank.model.Account;
import com.valenciaBank.valenciaBank.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public Account saveAccount(Account account) {
        //metodo en el que guardamos la cuenta
        return accountRepository.save(account);
    }

    public Account findAccountByNumber(String number) {
        return accountRepository.findByNumber(number);  // Usa findByNumber
    }
}
