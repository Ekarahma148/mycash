package com.project.mycash.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import com.project.mycash.dto.BudgetSummary;
import com.project.mycash.models.Budget;
import com.project.mycash.models.CategoryKas;
import com.project.mycash.models.User;
import com.project.mycash.repositories.BudgetRepository;
import com.project.mycash.repositories.CategoryKasRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepo;
    private final CategoryKasRepository categoryRepo;
    private final ActivityLogService logService;

    public Budget save(Budget budget, User user) {

        if (budget.getAmount() == null ||
                budget.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new RuntimeException(
                    "Nominal budget harus lebih dari 0");
        }

        if (budget.getStartDate() == null ||
                budget.getEndDate() == null) {

            throw new RuntimeException(
                    "Tanggal budget wajib diisi");
        }

        if (budget.getEndDate()
                .isBefore(budget.getStartDate())) {

            throw new RuntimeException(
                    "Tanggal selesai tidak boleh sebelum tanggal mulai");
        }

        if (budget.getCategory() == null ||
                budget.getCategory().getId() == null) {

            throw new RuntimeException(
                    "Kategori wajib dipilih");
        }

        CategoryKas category =
                categoryRepo.findByIdAndUser(
                        budget.getCategory().getId(),
                        user);

        if (category == null) {
            throw new RuntimeException(
                    "Kategori tidak ditemukan");
        }

        /*
         * Pastikan budget selalu menggunakan
         * user dan category yang berasal dari database.
         */
        budget.setUser(user);
        budget.setCategory(category);

        boolean isNew = budget.getId() == null;

        Budget saved = budgetRepo.save(budget);

        logService.log(
                user,
                isNew ? "CREATE" : "UPDATE",
                (isNew
                        ? "Membuat budget kategori "
                        : "Mengubah budget kategori ")
                        + category.getName()
                        + " sebesar "
                        + formatRupiah(saved.getAmount())
        );

        return saved;
    }

    public List<Budget> findByUser(User user) {

        return budgetRepo
                .findByUserOrderByStartDateDesc(user);
    }

    public Budget findById(Long id, User user) {

        Budget budget =
                budgetRepo.findByIdAndUser(id, user);

        if (budget == null) {
            throw new RuntimeException(
                    "Budget tidak ditemukan");
        }

        return budget;
    }

    public void delete(Long id, User user) {

        Budget budget =
                budgetRepo.findByIdAndUser(id, user);

        if (budget == null) {
            throw new RuntimeException(
                    "Budget tidak ditemukan");
        }

        budgetRepo.delete(budget);

        logService.log(
                user,
                "DELETE",
                "Menghapus budget kategori "
                        + budget.getCategory().getName()
                        + " sebesar "
                        + formatRupiah(budget.getAmount())
        );
    }

    public BudgetSummary createSummary(Budget budget) {

        BigDecimal usedAmount =
                budgetRepo.getUsedAmount(
                        budget.getUser(),
                        budget.getCategory().getId(),
                        budget.getStartDate(),
                        budget.getEndDate()
                );

        if (usedAmount == null) {
            usedAmount = BigDecimal.ZERO;
        }

        BigDecimal remainingAmount =
                budget.getAmount()
                        .subtract(usedAmount);

        int percentage =
                calculatePercentage(
                        usedAmount,
                        budget.getAmount());

        String status =
                determineStatus(
                        usedAmount,
                        budget.getAmount());

        return new BudgetSummary(
                budget.getId(),
                budget.getCategory().getName(),
                budget.getAmount(),
                usedAmount,
                remainingAmount,
                percentage,
                budget.getStartDate(),
                budget.getEndDate(),
                status
        );
    }

    public List<BudgetSummary> getBudgetSummaries(
            User user) {

        return findByUser(user)
                .stream()
                .map(this::createSummary)
                .toList();
    }

    private int calculatePercentage(
            BigDecimal used,
            BigDecimal budget) {

        if (budget == null ||
                budget.compareTo(BigDecimal.ZERO) <= 0) {

            return 0;
        }

        int percentage =
                used.multiply(BigDecimal.valueOf(100))
                        .divide(
                                budget,
                                0,
                                RoundingMode.HALF_UP)
                        .intValue();

        /*
         * Untuk progress bar UI,
         * maksimal 100%.
         *
         * Tetapi status tetap bisa OVER.
         */
        return Math.min(percentage, 100);
    }

    private String determineStatus(
            BigDecimal used,
            BigDecimal budget) {

        if (used.compareTo(budget) > 0) {
            return "OVER";
        }

        BigDecimal percentage =
                used.multiply(BigDecimal.valueOf(100))
                        .divide(
                                budget,
                                2,
                                RoundingMode.HALF_UP);

        if (percentage.compareTo(
                BigDecimal.valueOf(80)) >= 0) {

            return "WARNING";
        }

        return "SAFE";
    }

    private String formatRupiah(
            BigDecimal amount) {

        return "Rp " +
                String.format(
                        "%,.0f",
                        amount)
                        .replace(',', '.');
    }
}