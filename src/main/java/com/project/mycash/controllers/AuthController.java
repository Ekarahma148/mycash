package com.project.mycash.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.project.mycash.models.User;
import com.project.mycash.services.AuthService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(
            @ModelAttribute User user,
            Model model) {

        // VALIDASI MANUAL
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            model.addAttribute("error", "Username wajib diisi");
            return "register";
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            model.addAttribute("error", "Password wajib diisi");
            return "register";
        }

        // CEK USERNAME SUDAH ADA
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

        // ✅ VALIDASI FORM
        if (username == null || username.trim().isEmpty()) {
            model.addAttribute("error", "Username tidak boleh kosong");
            return "login";
        }

        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "Password tidak boleh kosong");
            return "login";
        }

        User u = authService.login(username, password);

        if (u == null) {
            model.addAttribute("error", "Login gagal: username atau password salah");
            return "login";
        }

        session.setAttribute("user", u);
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

}
