package com.valenciaBank.valenciaBank.service;

import com.valenciaBank.valenciaBank.model.User;
import com.valenciaBank.valenciaBank.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImplementation - Tests unitarios")
class UserServiceImplementationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImplementation userService;

    @Test
    @DisplayName("saveUser codifica la contraseña para nuevo usuario")
    void saveUserCodificaPassword() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("plainPassword");
        user.setDni("12345678A");

        when(passwordEncoder.encode("plainPassword")).thenReturn("$2a$10$encodedHash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        User saved = userService.saveUser(user);

        assertNotNull(saved);
        assertEquals("$2a$10$encodedHash", saved.getPassword());
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("saveUser no recodifica contraseña ya hasheada")
    void saveUserNoRecodifica() {
        User user = new User();
        user.setId(1L); // Usuario existente
        user.setUsername("testuser");
        user.setPassword("$2a$10$alreadyHashed");
        user.setDni("12345678A");

        when(userRepository.save(any(User.class))).thenReturn(user);

        User saved = userService.saveUser(user);

        assertEquals("$2a$10$alreadyHashed", saved.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("getAllUsers retorna lista de usuarios")
    void getAllUsersRetornaLista() {
        User user1 = new User();
        user1.setUsername("user1");
        User user2 = new User();
        user2.setUsername("user2");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> users = userService.getAllUsers();

        assertEquals(2, users.size());
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("getUserByDniAndPassword retorna usuario con credenciales correctas")
    void getUserByDniAndPasswordCorrecto() {
        User user = new User();
        user.setDni("12345678A");
        user.setPassword("$2a$10$hashedPassword");

        when(userRepository.findUserByDni("12345678A")).thenReturn(user);
        when(passwordEncoder.matches("plainPassword", "$2a$10$hashedPassword")).thenReturn(true);

        User result = userService.getUserByDniAndPassword("12345678A", "plainPassword");

        assertNotNull(result);
        assertEquals("12345678A", result.getDni());
    }

    @Test
    @DisplayName("getUserByDniAndPassword retorna null con contraseña incorrecta")
    void getUserByDniAndPasswordIncorrecto() {
        User user = new User();
        user.setDni("12345678A");
        user.setPassword("$2a$10$hashedPassword");

        when(userRepository.findUserByDni("12345678A")).thenReturn(user);
        when(passwordEncoder.matches("wrongPassword", "$2a$10$hashedPassword")).thenReturn(false);

        User result = userService.getUserByDniAndPassword("12345678A", "wrongPassword");

        assertNull(result);
    }

    @Test
    @DisplayName("getUserByDniAndPassword retorna null si usuario no existe")
    void getUserByDniAndPasswordNoExiste() {
        when(userRepository.findUserByDni("00000000X")).thenReturn(null);

        User result = userService.getUserByDniAndPassword("00000000X", "password");

        assertNull(result);
    }

    @Test
    @DisplayName("existsByDni retorna true si el usuario existe")
    void existsByDniTrue() {
        when(userRepository.existsByDni("12345678A")).thenReturn(true);
        assertTrue(userService.existsByDni("12345678A"));
    }

    @Test
    @DisplayName("existsByDni retorna false si no existe")
    void existsByDniFalse() {
        when(userRepository.existsByDni("00000000X")).thenReturn(false);
        assertFalse(userService.existsByDni("00000000X"));
    }

    @Test
    @DisplayName("getUser busca usuario por DNI")
    void getUser() {
        User user = new User();
        user.setDni("12345678A");
        user.setUsername("testuser");

        when(userRepository.findUserByDni("12345678A")).thenReturn(user);

        User result = userService.getUser("12345678A");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }
}
