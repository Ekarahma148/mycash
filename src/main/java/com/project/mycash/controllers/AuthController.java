package com.project.mycash.controllers;

import com.project.mycash.models.User;
import com.project.mycash.services.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 🔥 FIRST RUN → LOGIN
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@ModelAttribute User user, Model model) {

        // USERNAME
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            model.addAttribute("error", "Username wajib diisi");
            return "register";
        }

        // PASSWORD
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            model.addAttribute("error", "Password wajib diisi");
            return "register";
        }

        if (user.getPassword().length() > 6) {
            model.addAttribute("error", "Password maksimal 6 karakter");
            return "register";
        }

        // ✅ VALIDASI NAMA LENGKAP
        String fullName = user.getFullName();

        if (fullName == null || fullName.isBlank()) {
            model.addAttribute("error", "Nama lengkap wajib diisi");
            return "register";
        }

        // hanya huruf & spasi
        if (!fullName.matches("[A-Za-z ]+")) {
            model.addAttribute("error", "Nama lengkap hanya boleh huruf");
            return "register";
        }

        // minimal 2 huruf (tanpa spasi)
        if (fullName.replace(" ", "").length() < 2) {
            model.addAttribute("error", "Nama lengkap minimal 2 huruf");
            return "register";
        }

        // CEK USERNAME
        if (authService.existsByUsername(user.getUsername())) {
            model.addAttribute("error", "Username sudah digunakan");
            return "register";
        }

        authService.register(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        User user = authService.login(username, password);
        if (user == null) {
            model.addAttribute("error", "Username atau password salah");
            return "login";
        }

        session.setAttribute("user", user);
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
