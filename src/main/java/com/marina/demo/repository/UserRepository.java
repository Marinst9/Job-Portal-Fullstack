package com.marina.demo.repository;

import com.marina.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Ова овозможува проверка дали корисникот веќе постои
    Optional<User> findByEmail(String email);
}