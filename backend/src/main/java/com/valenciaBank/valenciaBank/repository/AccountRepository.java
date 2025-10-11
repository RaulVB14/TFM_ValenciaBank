package com.valenciaBank.valenciaBank.repository;

import com.valenciaBank.valenciaBank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByNumber(String number);  // Asegúrate de que el método está actualizado
}
