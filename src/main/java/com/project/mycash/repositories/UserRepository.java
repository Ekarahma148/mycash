package com.project.mycash.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.mycash.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

}
