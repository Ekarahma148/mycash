package com.project.mycash.controllers;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.project.mycash.models.CashTransaction;
import com.project.mycash.models.TransactionType;
import com.project.mycash.repositories.CashTransactionRepository;
import org.springframework.ui.Model;
import java.util.Comparator;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;



@Controller
@RequiredArgsConstructor
public class DashboardController {
    private final CashTransactionRepository txRepo;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";

        BigDecimal totalIn = txRepo.findAll().stream()
                .filter(t -> t.getType() == TransactionType.IN)
                .map(CashTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOut = txRepo.findAll().stream()
                .filter(t -> t.getType() == TransactionType.OUT)
                .map(CashTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalIn", totalIn);
        model.addAttribute("totalOut", totalOut);
        model.addAttribute("balance", totalIn.subtract(totalOut));
        model.addAttribute("recent", txRepo.findAll().stream().sorted(Comparator.comparing(CashTransaction::getDate).reversed()).limit(10).toList());
        return "index";
    }
}