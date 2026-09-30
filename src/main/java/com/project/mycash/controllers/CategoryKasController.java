package com.project.mycash.controllers;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.mycash.models.CategoryKas;
import com.project.mycash.models.User;
import com.project.mycash.services.CategoryKasService;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryKasController {

    private final CategoryKasService categoryService;


    // =========================
    // READ
    // =========================

    @GetMapping
    public String list(
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("user");

        List<CategoryKas> categories =
                categoryService.findByUser(user);

        model.addAttribute(
                "categories",
                categories);

        return "category/list";
    }


    // =========================
    // CREATE FORM
    // =========================

    @GetMapping("/add")
    public String addForm(
            Model model) {

        model.addAttribute(
                "category",
                new CategoryKas());

        return "category/form";
    }


    // =========================
    // CREATE / UPDATE
    // =========================

    @PostMapping("/save")
    public String save(
            @ModelAttribute CategoryKas category,
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("user");

        try {

            categoryService.save(
                    category,
                    user);

            return "redirect:/categories";

        } catch (RuntimeException e) {

            model.addAttribute(
                    "error",
                    e.getMessage());

            model.addAttribute(
                    "category",
                    category);

            return "category/form";
        }
    }


    // =========================
    // UPDATE FORM
    // =========================

    @GetMapping("/edit/{id}")
    public String editForm(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("user");

        CategoryKas category =
                categoryService.findById(
                        id,
                        user);

        model.addAttribute(
                "category",
                category);

        return "category/form";
    }


    // =========================
    // DELETE
    // =========================

   @PostMapping("/delete/{id}")
public String delete(
        @PathVariable Long id,
        HttpSession session,
        RedirectAttributes redirectAttributes) {

    User user =
            (User) session.getAttribute("user");

    try {

        categoryService.delete(
                id,
                user);

        redirectAttributes.addFlashAttribute(
                "success",
                "Kategori berhasil dihapus.");

    } catch (DataIntegrityViolationException e) {

        redirectAttributes.addFlashAttribute(
                "error",
                "Kategori tidak dapat dihapus karena masih digunakan oleh data transaksi, budget, atau data lainnya."
        );

    } catch (RuntimeException e) {

        redirectAttributes.addFlashAttribute(
                "error",
                e.getMessage()
        );
    }

    return "redirect:/categories";
}
}