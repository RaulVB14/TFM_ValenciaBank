package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.model.Account;
import com.valenciaBank.valenciaBank.model.User;
import com.valenciaBank.valenciaBank.service.AccountService;
import com.valenciaBank.valenciaBank.service.UserServiceImplementation;
import com.valenciaBank.valenciaBank.utils.Jwt;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController - Tests unitarios")
class UserControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserServiceImplementation userService;

    @Mock
    private Jwt jwt;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        // Configurar la clave JWT usando la instancia mock (setSecretKey es método de instancia con @Value)
        Jwt jwtInstance = new Jwt();
        jwtInstance.setSecretKey("TestSecretKeyForJWT2026");
    }

    private User crearUsuarioTest() {
        User user = new User();
        user.setId(1L);
        user.setUsername("raul_vb");
        user.setDni("12345678A");
        user.setPassword("password123");
        user.setEmail("raul@test.com");
        user.setNombre("Raúl");
        user.setApellidos("Vega Bolufer");
        return user;
    }

    @Test
    @DisplayName("POST /user/add registra usuario y crea cuenta")
    void registerUser() throws Exception {
        User user = crearUsuarioTest();
        Account account = new Account();
        account.setId(1L);
        account.setBalance(0.0);
        account.setNumber("1234567890123456");

        when(userService.saveUser(any(User.class))).thenReturn(user);
        when(accountService.saveAccount(any(Account.class))).thenReturn(account);

        mockMvc.perform(post("/user/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        verify(userService, atLeast(1)).saveUser(any(User.class));
        verify(accountService).saveAccount(any(Account.class));
    }

    @Test
    @DisplayName("GET /user/getAll retorna lista de usuarios")
    void getAllUsers() throws Exception {
        User user1 = crearUsuarioTest();
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setDni("22222222B");

        when(userService.getAllUsers()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/user/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("POST /user/login retorna token con credenciales correctas")
    void loginExitoso() throws Exception {
        User user = crearUsuarioTest();
        when(userService.getUserByDniAndPassword("12345678A", "password123")).thenReturn(user);

        User loginRequest = new User();
        loginRequest.setDni("12345678A");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("POST /user/login retorna 401 con credenciales incorrectas")
    void loginFallido() throws Exception {
        when(userService.getUserByDniAndPassword(anyString(), anyString())).thenReturn(null);

        User loginRequest = new User();
        loginRequest.setDni("00000000X");
        loginRequest.setPassword("wrongPassword");

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /user/get/{dni} retorna usuario por DNI")
    void getUsuarioPorDni() throws Exception {
        User user = crearUsuarioTest();
        when(userService.getUser("12345678A")).thenReturn(user);

        mockMvc.perform(get("/user/get/12345678A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("raul_vb"))
                .andExpect(jsonPath("$.dni").value("12345678A"));
    }

    @Test
    @DisplayName("GET /user/exists/{dni} retorna true si existe")
    void existsByDniTrue() throws Exception {
        when(userService.existsByDni("12345678A")).thenReturn(true);

        mockMvc.perform(get("/user/exists/12345678A"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("PUT /user/update/{dni} actualiza datos del usuario")
    void updateUser() throws Exception {
        User user = crearUsuarioTest();
        when(userService.getUser("12345678A")).thenReturn(user);
        when(userService.saveUser(any(User.class))).thenReturn(user);

        String updates = "{\"username\":\"nuevo_username\",\"email\":\"nuevo@test.com\"}";

        mockMvc.perform(put("/user/update/12345678A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updates))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /user/update/{dni} retorna 404 si usuario no existe")
    void updateUserNoExiste() throws Exception {
        when(userService.getUser("00000000X")).thenReturn(null);

        String updates = "{\"username\":\"test\"}";

        mockMvc.perform(put("/user/update/00000000X")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updates))
                .andExpect(status().isNotFound());
    }
}
