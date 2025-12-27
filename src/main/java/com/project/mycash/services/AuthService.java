package com.project.mycash.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.project.mycash.models.CategoryKas;
import com.project.mycash.models.User;
import com.project.mycash.repositories.CategoryKasRepository;
import com.project.mycash.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final CategoryKasRepository categoryRepo;

    public User register(User user) {

        // 🔥 SET DEFAULT VALUE
        user.setRole("USER");
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLogin(null);

        User savedUser = userRepo.save(user);

        // ===== DEFAULT CATEGORY =====
        List<CategoryKas> defaults = List.of(
                new CategoryKas("Gaji", "Pendapatan", savedUser),
                new CategoryKas("Makanan", "Beban Konsumsi", savedUser),
                new CategoryKas("Transportasi", "Beban Transportasi", savedUser),
                new CategoryKas("Listrik", "Beban Listrik", savedUser),
                new CategoryKas("Umum", "Beban lainnya", savedUser));

        categoryRepo.saveAll(defaults);

        return savedUser;
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
