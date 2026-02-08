package com.valenciaBank.valenciaBank.repository;

import com.valenciaBank.valenciaBank.model.FundPurchase;
import com.valenciaBank.valenciaBank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundPurchaseRepository extends JpaRepository<FundPurchase, Long> {
    List<FundPurchase> findByUser(User user);
    List<FundPurchase> findByUserAndSymbol(User user, String symbol);
    List<FundPurchase> findByUserIdAndSymbol(Long userId, String symbol);
    List<FundPurchase> findByUserId(Long userId);
}
