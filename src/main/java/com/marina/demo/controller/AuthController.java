package com.marina.demo.controller;

import com.marina.demo.dto.AuthRequest;
import com.marina.demo.exception.BadRequestException;
import com.marina.demo.exception.ConflictException;
import com.marina.demo.model.User;
import com.marina.demo.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        if (isBlank(user.getEmail()) || isBlank(user.getPassword()) || isBlank(user.getFullName())) {
            throw new BadRequestException("Име, емаил и лозинка се задолжителни.");
        }
        String email = user.getEmail().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("Емаил адресата веќе постои!");
        }

        // Никогаш не му веруваме на id од клиентот: без ова, POST со постоечко id
        // би го препишал туѓиот профил (JPA save со id = merge).
        user.setId(null);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole(User.Role.CANDIDATE);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(userRepository.save(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        if (isBlank(request.getEmail()) || isBlank(request.getPassword())) {
            throw new BadRequestException("Емаил и лозинка се задолжителни.");
        }
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail().trim().toLowerCase());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword() != null && passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                // Лозинката е WRITE_ONLY, па не се враќа во одговорот.
                return ResponseEntity.ok(user);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Погрешен емаил или лозинка!"));
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
