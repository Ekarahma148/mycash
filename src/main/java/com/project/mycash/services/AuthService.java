package com.project.mycash.services;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.project.mycash.models.User;
import com.project.mycash.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;

    public User register(User user) {

        // 🔥 SET DEFAULT VALUE
        user.setRole("USER");
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLogin(null);

        return userRepo.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepo.existsByUsername(username);
    }

    public User login(String username, String password) {
        User user = userRepo.findByUsername(username).orElse(null);

        if (user != null && user.getPassword().equals(password)) {
            // update last login
            user.setLastLogin(LocalDateTime.now());
            userRepo.save(user);
            return user;
        }
        return null;
    }
}
