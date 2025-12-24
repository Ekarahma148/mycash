package com.project.mycash.controllers;

import com.project.mycash.models.CategoryKas;
import com.project.mycash.models.User;
import com.project.mycash.repositories.CategoryKasRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
public class CategoryKasController {

    private final CategoryKasRepository categoryRepo;

    @GetMapping("/categories")
    public String list(Model model, HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        model.addAttribute("categories",
                categoryRepo.findByUser(user));

        model.addAttribute("category", new CategoryKas());
        return "category";
    }

    @PostMapping("/categories")
    public String save(
            @Valid @ModelAttribute("category") CategoryKas category,
            BindingResult result,
            HttpSession session,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepo.findAll());
            return "category";
        }

        User user = (User) session.getAttribute("user");
        category.setUser(user); // 🔥 WAJIB

        categoryRepo.save(category);
        return "redirect:/categories";
    }
}