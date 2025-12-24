package com.project.mycash.services;

import com.project.mycash.models.User;
import com.project.mycash.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepo;

    public User findById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    @Transactional
    public void updateProfile(User updatedUser) {
        User user = userRepo.findById(updatedUser.getId()).orElseThrow();

        user.setFullName(updatedUser.getFullName());
        user.setEmail(updatedUser.getEmail());
        user.setPhone(updatedUser.getPhone());
        user.setGender(updatedUser.getGender());
        user.setDateOfBirth(updatedUser.getDateOfBirth());
        user.setAddress(updatedUser.getAddress());
        user.setCity(updatedUser.getCity());
        user.setOccupation(updatedUser.getOccupation());

        userRepo.save(user);
    }
}
