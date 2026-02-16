package com.marina.demo.controller;

import com.marina.demo.dto.AuthRequest;
import com.marina.demo.model.User;
import com.marina.demo.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Емаил адресата веќе постои!"));
        }
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // ВНИМАНИЕ: Во реален проект тука се користи BCrypt за лозинки. 
            // За овој проект користиме обичен стринг за полесно тестирање.
            if (user.getPassword().equals(request.getPassword())) {
                return ResponseEntity.ok(user); // Враќаме цел User објект со Role
            }
        }
        
        return ResponseEntity.status(401).body(Map.of("error", "Погрешен емаил или лозинка!"));
    }
}