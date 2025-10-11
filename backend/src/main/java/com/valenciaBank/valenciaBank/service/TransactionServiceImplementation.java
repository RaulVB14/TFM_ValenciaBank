package com.valenciaBank.valenciaBank.service;

import com.valenciaBank.valenciaBank.model.Transaction;
import com.valenciaBank.valenciaBank.repository.TransactionReporsitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class TransactionServiceImplementation extends TransactionService {

    @Autowired
    private TransactionReporsitory transactionRepository;

    @Override
    public void saveTransaction(Transaction transaction){
        transactionRepository.save(transaction);
    }
}
