package com.valenciaBank.valenciaBank.repository;

import com.valenciaBank.valenciaBank.model.Crypto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CryptoRepository extends JpaRepository<Crypto, Long> {

    @Query("SELECT MAX(c.date) FROM Crypto c WHERE c.name = :name")
    LocalDate findLatestDate(@Param("name") String name);

    @Query("SELECT c FROM Crypto c WHERE c.name = :name")
    List<Crypto> findByCryptoName(@Param("name") String name);


}
