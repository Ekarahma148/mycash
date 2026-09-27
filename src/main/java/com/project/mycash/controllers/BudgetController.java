package com.project.mycash.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.project.mycash.dto.BudgetSummary;
import com.project.mycash.models.Budget;
import com.project.mycash.models.User;
import com.project.mycash.repositories.CategoryKasRepository;
import com.project.mycash.services.BudgetService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final CategoryKasRepository categoryRepo;

    @GetMapping
    public String list(
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("user");

        List<BudgetSummary> budgets =
                budgetService.getBudgetSummaries(user);

        model.addAttribute(
                "budgets",
                budgets);

        return "budget/list";
    }

    @GetMapping("/add")
    public String addForm(
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("user");

        Budget budget = new Budget();

        model.addAttribute(
                "budget",
                budget);

        model.addAttribute(
                "categories",
                categoryRepo.findByUser(user));

        return "budget/form";
    }

    @PostMapping("/save")
    public String save(
            @ModelAttribute Budget budget,
            HttpSession session) {

        User user =
                (User) session.getAttribute("user");

        budgetService.save(
                budget,
                user);

        return "redirect:/budgets";
    }

    @GetMapping("/edit/{id}")
    public String editForm(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        User user =
                (User) session.getAttribute("user");

        Budget budget =
                budgetService.findById(
                        id,
                        user);

        model.addAttribute(
                "budget",
                budget);

        model.addAttribute(
                "categories",
                categoryRepo.findByUser(user));

        return "budget/form";
    }

    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable Long id,
            HttpSession session) {

        User user =
                (User) session.getAttribute("user");

        budgetService.delete(
                id,
                user);

        return "redirect:/budgets";
    }
}