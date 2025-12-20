package com.project.mycash.controllers;

import com.project.mycash.models.CashTransaction;
import com.project.mycash.models.TransactionType;
import com.project.mycash.models.User;
import com.project.mycash.repositories.CashTransactionRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

        private final CashTransactionRepository txRepo;

        @GetMapping("/dashboard")
        public String dashboard(Model model, HttpSession session) {

                User user = (User) session.getAttribute("user");
                if (user == null)
                        return "redirect:/login";

                List<CashTransaction> transactions = txRepo.findByUser(user);

                BigDecimal totalIn = transactions.stream()
                                .filter(t -> t.getType() == TransactionType.IN)
                                .map(CashTransaction::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalOut = transactions.stream()
                                .filter(t -> t.getType() == TransactionType.OUT)
                                .map(CashTransaction::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                model.addAttribute("totalIn", totalIn);
                model.addAttribute("totalOut", totalOut);
                model.addAttribute("balance", totalIn.subtract(totalOut));

                model.addAttribute("recent",
                                transactions.stream()
                                                .sorted(Comparator.comparing(CashTransaction::getDate).reversed())
                                                .limit(10)
                                                .toList());

                return "index";
        }
}
