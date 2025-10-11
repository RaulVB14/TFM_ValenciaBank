package com.valenciaBank.valenciaBank.service;

import com.valenciaBank.valenciaBank.model.User;
import com.valenciaBank.valenciaBank.repository.UserRepository;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImplementation {

    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImplementation(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User saveUser(User user) {
        // Encripta la contraseña solo si es nueva o se ha modificado
        if (user.getId() == null || user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByDniAndPassword(String dni, String password) {
        System.out.println("DNI recibido: " + dni);
        System.out.println("Contraseña recibida: " + password);

        User user = userRepository.findUserByDni(dni);

        if (user != null) {
            System.out.println("Usuario encontrado en la base de datos: " + user.getDni());
            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
            System.out.println("Coincidencia de contraseñas: " + passwordMatches);

            if (passwordMatches) {
                return user;
            }
        } else {
            System.out.println("Usuario no encontrado en la base de datos.");
        }

        return null;
    }

    public boolean existsByDni(String dni) {
        return userRepository.existsByDni(dni);
    }


    public User getUser(String dni) {
        return userRepository.findUserByDni(dni);
    }
}