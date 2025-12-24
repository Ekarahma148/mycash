package com.project.mycash.controllers;

import com.project.mycash.models.User;
import com.project.mycash.services.ProfileService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public String profile(Model model, HttpSession session) {

        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null)
            return "redirect:/login";

        User user = profileService.findById(sessionUser.getId());
        model.addAttribute("user", user);

        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(
            @ModelAttribute User user,
            HttpSession session,
            Model model) {

        // VALIDASI NAMA
        if (user.getFullName() == null ||
                !user.getFullName().matches("[A-Za-z ]+") ||
                user.getFullName().replace(" ", "").length() < 2) {

            model.addAttribute("error", "Nama lengkap tidak valid");
            model.addAttribute("user", user);
            return "profile";
        }

        profileService.updateProfile(user);

        // update session
        session.setAttribute("user", user);

        return "redirect:/profile?success";
    }
}
