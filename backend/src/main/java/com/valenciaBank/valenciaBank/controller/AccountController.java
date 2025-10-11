package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.model.Account;
import com.valenciaBank.valenciaBank.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
@CrossOrigin
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/add")
    public void add(@RequestBody Account account){
        accountService.saveAccount(account);
    }

}
