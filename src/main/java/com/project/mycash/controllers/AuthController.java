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
        // Tambah pengecekan agar tidak null saat form dibuka ulang karena error
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@ModelAttribute User user, Model model) {

        // USERNAME
        if (user.getUsername() == null || user.getUsername().trim().isBlank()) {
            model.addAttribute("error", "Username wajib diisi");
            model.addAttribute("user", user); // penting agar input tetap terisi saat error
            return "register";
        }

        // Trim username agar tidak ada spasi di awal/akhir
        user.setUsername(user.getUsername().trim());

        // PASSWORD
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            model.addAttribute("error", "Password wajib diisi");
            model.addAttribute("user", user);
            return "register";
        }

        if (user.getPassword().length() > 6) {
            model.addAttribute("error", "Password maksimal 6 karakter");
            model.addAttribute("user", user);
            return "register";
        }

        // ✅ VALIDASI NAMA LENGKAP
        String fullName = user.getFullName();

        if (fullName == null || fullName.trim().isBlank()) {
            model.addAttribute("error", "Nama lengkap wajib diisi");
            model.addAttribute("user", user);
            return "register";
        }

        fullName = fullName.trim();
        user.setFullName(fullName); // simpan yang sudah di-trim

        // hanya huruf & spasi
        if (!fullName.matches("[A-Za-z ]+")) {
            model.addAttribute("error", "Nama lengkap hanya boleh huruf dan spasi");
            model.addAttribute("user", user);
            return "register";
        }

        // minimal 2 huruf (tanpa spasi)
        if (fullName.replace(" ", "").length() < 2) {
            model.addAttribute("error", "Nama lengkap minimal 2 huruf");
            model.addAttribute("user", user);
            return "register";
        }

        // CEK USERNAME SUDAH ADA
        if (authService.existsByUsername(user.getUsername())) {
            model.addAttribute("error", "Username sudah digunakan");
            model.addAttribute("user", user);
            return "register";
        }

        // Jika semua validasi lolos
        authService.register(user);
        return "redirect:/login?success=register"; // opsional: tambah parameter success
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

        // Validasi input kosong
        if (username == null || username.trim().isBlank()) {
            model.addAttribute("error", "Username wajib diisi");
            return "login";
        }

        if (password == null || password.isBlank()) {
            model.addAttribute("error", "Password wajib diisi");
            return "login";
        }

        User user = authService.login(username.trim(), password);

        if (user == null) {
            model.addAttribute("error", "Username atau password salah");
            return "login";
        }

        // Login sukses → simpan ke session
        session.setAttribute("user", user);
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}