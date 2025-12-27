package com.project.mycash.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.project.mycash.models.CashTransaction;
import com.project.mycash.models.TransactionType;
import com.project.mycash.models.User;
import com.project.mycash.repositories.CategoryKasRepository;
import com.project.mycash.services.CashTransactionService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CashTransactionController {

    private final CashTransactionService service;
    private final CategoryKasRepository categoryRepo;

    /* ================= LIST ================= */
    @GetMapping("/transactions")
    public String list(
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate start,

            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate end,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(defaultValue = "desc") String sort,

            HttpSession session,
            Model model) {

        if (start == null)
            start = LocalDate.now().withDayOfMonth(1);
        if (end == null)
            end = LocalDate.now();

        User user = (User) session.getAttribute("user");

        List<CashTransaction> list = service.findByUserAndPeriod(user, start, end, type, sort);

        model.addAttribute("transactions", list);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAttribute("type", type);
        model.addAttribute("sort", sort);

        return "transactions/list";
    }

    /* ================= CREATE FORM ================= */
    @GetMapping("/transactions/new")
    public String createForm(Model model, HttpSession session) {

        User user = (User) session.getAttribute("user");

        model.addAttribute("transaction",
                CashTransaction.builder()
                        .date(LocalDate.now())
                        .build());

        model.addAttribute("types", TransactionType.values());
        model.addAttribute("categories",
                categoryRepo.findByUser(user));

        return "transactions/form";
    }

    /* ================= SAVE ================= */
    @PostMapping("/transactions")
    public String save(
            @Valid @ModelAttribute("transaction") CashTransaction transaction,
            BindingResult result,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("user");
        transaction.setUser(user);

        if (result.hasErrors()) {
            model.addAttribute("types", TransactionType.values());
            model.addAttribute("categories",
                    categoryRepo.findByUser(user));
            return "transactions/form";
        }

        try {
            service.save(transaction);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("types", TransactionType.values());
            model.addAttribute("categories",
                    categoryRepo.findByUser(user));
            return "transactions/form";
        }

        return "redirect:/transactions";
    }

    /* ================= EDIT FORM ================= */
    @GetMapping("/transactions/edit/{id}")
    public String editForm(
            @PathVariable Long id,
            Model model,
            HttpSession session) {

        User user = (User) session.getAttribute("user");

        CashTransaction tx = service.findById(id);

        model.addAttribute("transaction", tx);
        model.addAttribute("types", TransactionType.values());
        model.addAttribute("categories",
                categoryRepo.findByUser(user));

        return "transactions/form";
    }

    /* ================= DELETE ================= */
    @GetMapping("/transactions/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/transactions";
    }

    /* ================= CONFIRM DELETE ================= */
    @GetMapping("/transactions/confirm-delete/{id}")
    public String confirmDelete(@PathVariable Long id, Model model) {
        CashTransaction tx = service.findById(id);
        model.addAttribute("transaction", tx);
        return "transactions/confirm-delete";
    }
}
