package com.valenciaBank.valenciaBank.service;

import com.valenciaBank.valenciaBank.model.Account;
import com.valenciaBank.valenciaBank.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImplementation extends AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }
}
