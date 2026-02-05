package com.valenciaBank.valenciaBank.controller;

import com.valenciaBank.valenciaBank.model.Account;
import com.valenciaBank.valenciaBank.model.User;
import com.valenciaBank.valenciaBank.service.AccountService;
import com.valenciaBank.valenciaBank.service.UserServiceImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import static com.valenciaBank.valenciaBank.utils.Methods.generateAccountNumber;
import com.valenciaBank.valenciaBank.utils.*;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:5173") // Permitir CORS desde tu frontend
public class UserController {

    @Autowired
    private UserServiceImplementation userService;


    @Autowired
    private Jwt jwt;

    @Autowired
    private AccountService accountService;

    @PostMapping("/add")
    public ResponseEntity<User> add(@RequestBody User user) {
        User savedUser = userService.saveUser(user);
        Account account = new Account();
        account.setNumber(generateAccountNumber());
        account.setBalance(0.0);
        account.setUser(savedUser);
        Account savedAccount = accountService.saveAccount(account);
        savedUser.setAccount(savedAccount);
        userService.saveUser(savedUser);
        return ResponseEntity.ok(savedUser);
    }

    @GetMapping("/getAll")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        User authenticatedUser = userService.getUserByDniAndPassword(user.getDni(), user.getPassword());

        if (authenticatedUser != null) {
            String token = Jwt.generateToken(String.valueOf(authenticatedUser.getDni()));
            return ResponseEntity.ok(new TokenResponse(token)); // Devuelve el token en un objeto JSON
        } else {
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }
    }


    @GetMapping("/get/{dni}")
    public User findUser(@PathVariable String dni) {
        return userService.getUser(dni);
    }

    @GetMapping("/exists/{dni}")
    public boolean existsByDni(@PathVariable String dni) {
        return userService.existsByDni(dni);
    }
}