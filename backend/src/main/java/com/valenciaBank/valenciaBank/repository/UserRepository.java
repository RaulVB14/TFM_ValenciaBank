package com.valenciaBank.valenciaBank.repository;

import com.valenciaBank.valenciaBank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByDni(String dni);
    User findUserByDni(String dni);
}
