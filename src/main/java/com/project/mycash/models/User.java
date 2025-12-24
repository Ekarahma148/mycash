package com.project.mycash.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== LOGIN =====
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // ===== IDENTITAS =====
    @Column(nullable = false)
    private String fullName;

    @Column(unique = true)
    private String email;

    private String phone;

    // ===== DATA PRIBADI =====
    private String gender; // L / P

    private LocalDate dateOfBirth;

    @Column(length = 255)
    private String address;

    private String city;

    private String occupation;

    // ===== STATUS AKUN =====
    private String role = "USER";

    private Boolean active = true;

    private LocalDateTime createdAt;

    private LocalDateTime lastLogin;

    @PrePersist
    public void prePersist() {

        if (role == null)
            role = "USER";
        if (active == null)
            active = true;

        if (createdAt == null)
            createdAt = LocalDateTime.now();

        // FIELD OPSIONAL → JANGAN NULL
        if (email == null)
            email = "";
        if (phone == null)
            phone = "";
        if (gender == null)
            gender = "";
        if (address == null)
            address = "";
        if (city == null)
            city = "";
        if (occupation == null)
            occupation = "";
    }
}
