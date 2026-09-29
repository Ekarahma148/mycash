package com.project.mycash.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.project.mycash.dto.BudgetSummary;
import com.project.mycash.dto.BudgetOverview;
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
    public String list(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        List<BudgetSummary> budgets = budgetService.getBudgetSummaries(user);

        model.addAttribute("budgets", budgets);
        model.addAttribute("overview", budgetService.getOverview(budgets));
        return "budget/list";
    }

    @GetMapping("/add")
    public String addForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        prepareForm(model, new Budget(), user);
        return "budget/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Budget budget, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        try {
            budgetService.save(budget, user);
            return "redirect:/budgets";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            prepareForm(model, budget, user);
            return "budget/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        try {
            prepareForm(model, budgetService.findById(id, user), user);
            return "budget/form";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return list(session, model);
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        try {
            budgetService.delete(id, user);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            List<BudgetSummary> budgets = budgetService.getBudgetSummaries(user);
            model.addAttribute("budgets", budgets);
            model.addAttribute("overview", budgetService.getOverview(budgets));
            return "budget/list";
        }

        return "redirect:/budgets";
    }

    private void prepareForm(Model model, Budget budget, User user) {
        model.addAttribute("budget", budget);
        model.addAttribute("categories", categoryRepo.findByUser(user));
    }
}
