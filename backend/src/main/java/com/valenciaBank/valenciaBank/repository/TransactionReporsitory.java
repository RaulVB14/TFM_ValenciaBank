package com.valenciaBank.valenciaBank.repository;


import com.valenciaBank.valenciaBank.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionReporsitory extends JpaRepository<Transaction,Long> {

}
