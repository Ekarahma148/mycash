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
import java.time.LocalDate;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final CashTransactionRepository txRepo;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        // =========================
        // TRANSAKSI USER
        // =========================

        List<CashTransaction> tx = txRepo.findByUser(user);

        // =========================
        // TOTAL PEMASUKAN
        // =========================

        BigDecimal totalIn = tx.stream()
                .filter(t -> t.getType() == TransactionType.IN)
                .map(CashTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // =========================
        // TOTAL PENGELUARAN
        // =========================

        BigDecimal totalOut = tx.stream()
                .filter(t -> t.getType() == TransactionType.OUT)
                .map(CashTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // =========================
        // TRANSAKSI TERBARU
        // =========================

        List<CashTransaction> recentTx = tx.stream()
                .sorted(
                        Comparator.comparing(
                                CashTransaction::getDate,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
                )
                .limit(5)
                .toList();

        // =========================
        // ANALYTICS
        // =========================

        int currentYear = LocalDate.now().getYear();

        List<Object[]> monthlyData =
                txRepo.getMonthlySummary(user, currentYear);

        List<Object[]> categoryData =
                txRepo.getExpenseByCategory(user, currentYear);

        // =========================
        // DATA UNTUK CHART BULANAN
        // =========================

        BigDecimal[] monthlyIncome = new BigDecimal[12];
        BigDecimal[] monthlyExpense = new BigDecimal[12];

        Arrays.fill(monthlyIncome, BigDecimal.ZERO);
        Arrays.fill(monthlyExpense, BigDecimal.ZERO);

        for (Object[] row : monthlyData) {

            Integer month = ((Number) row[0]).intValue();

            TransactionType type =
                    (TransactionType) row[1];

            BigDecimal amount =
                    (BigDecimal) row[2];

            if (type == TransactionType.IN) {
                monthlyIncome[month - 1] =
                        monthlyIncome[month - 1].add(amount);
            } else {
                monthlyExpense[month - 1] =
                        monthlyExpense[month - 1].add(amount);
            }
        }

        // =========================
        // DATA KATEGORI
        // =========================

        List<String> categoryNames = new ArrayList<>();
        List<BigDecimal> categoryAmounts = new ArrayList<>();

        for (Object[] row : categoryData) {

            String categoryName = (String) row[0];

            BigDecimal amount =
                    (BigDecimal) row[1];

            categoryNames.add(categoryName);
            categoryAmounts.add(amount);
        }

        // =========================
        // KIRIM KE THYMELEAF
        // =========================

        model.addAttribute("totalIn", totalIn);
        model.addAttribute("totalOut", totalOut);
        model.addAttribute(
                "balance",
                totalIn.subtract(totalOut)
        );

        model.addAttribute(
                "transactions",
                recentTx
        );

        model.addAttribute(
                "currentYear",
                currentYear
        );

        model.addAttribute(
                "monthlyIncome",
                monthlyIncome
        );

        model.addAttribute(
                "monthlyExpense",
                monthlyExpense
        );

        model.addAttribute(
                "categoryNames",
                categoryNames
        );

        model.addAttribute(
                "categoryAmounts",
                categoryAmounts
        );

        return "index";
    }
}