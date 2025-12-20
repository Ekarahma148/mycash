package com.project.mycash.controllers;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.project.mycash.models.CashTransaction;
import com.project.mycash.models.TransactionType;
import com.project.mycash.models.User;
import com.project.mycash.repositories.CategoryKasRepository;
import com.project.mycash.services.CashTransactionService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@Controller
@RequiredArgsConstructor
public class CashTransactionController {
    private final CashTransactionService service;
    private final CategoryKasRepository categoryRepo;

    @GetMapping("/transactions")
    public String list(
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate start,

            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate end,

            HttpSession session,
            Model model) {
        if (start == null)
            start = LocalDate.now().withDayOfMonth(1);
        if (end == null)
            end = LocalDate.now();

        User user = (User) session.getAttribute("user");

        List<CashTransaction> list = service.findByUserAndPeriod(user, start, end);

        model.addAttribute("transactions", list);
        model.addAttribute("start", start);
        model.addAttribute("end", end);

        return "transactions/list";
    }

    @GetMapping("/transactions/new")
    public String createForm(Model model) {
        model.addAttribute("transaction",
                CashTransaction.builder().date(LocalDate.now()).build());
        model.addAttribute("types", TransactionType.values());
        model.addAttribute("categories", categoryRepo.findAll());
        return "transactions/form";
    }

    @PostMapping("/transactions")
    public String save(
            @Valid @ModelAttribute("transaction") CashTransaction transaction,
            BindingResult result,
            HttpSession session,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("types", TransactionType.values());
            model.addAttribute("categories", categoryRepo.findAll());
            return "transactions/form";
        }

        User user = (User) session.getAttribute("user");
        transaction.setUser(user); // 🔥 INI KUNCI

        service.save(transaction);
        return "redirect:/transactions";
    }

    @GetMapping("/transactions/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        CashTransaction tx = service.findById(id);
        model.addAttribute("transaction", tx);
        model.addAttribute("types", TransactionType.values());
        model.addAttribute("categories", categoryRepo.findAll());
        return "transactions/form";
    }

    @GetMapping("/transactions/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/transactions";
    }

    @GetMapping("/transactions/confirm-delete/{id}")
    public String confirmDelete(@PathVariable Long id, Model model) {
        CashTransaction tx = service.findById(id);
        model.addAttribute("transaction", tx);
        return "transactions/confirm-delete";
    }
}
