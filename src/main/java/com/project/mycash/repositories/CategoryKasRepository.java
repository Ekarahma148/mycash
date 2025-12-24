package com.project.mycash.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.mycash.models.CategoryKas;
import com.project.mycash.models.User;

public interface CategoryKasRepository extends JpaRepository<CategoryKas, Long> {
    List<CategoryKas> findByUser(User user);
}