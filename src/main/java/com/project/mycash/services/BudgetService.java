package com.project.mycash.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.project.mycash.dto.BudgetOverview;
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
        validateBudget(budget);

        CategoryKas category = categoryRepo.findByIdAndUser(
                budget.getCategory().getId(), user);

        if (category == null) {
            throw new IllegalArgumentException("Kategori tidak ditemukan atau bukan milik akunmu.");
        }

        long overlapCount = budgetRepo.countOverlappingBudgets(
                user,
                category.getId(),
                budget.getStartDate(),
                budget.getEndDate(),
                budget.getId());

        if (overlapCount > 0) {
            throw new IllegalArgumentException(
                    "Sudah ada budget untuk kategori " + category.getName()
                            + " pada periode yang beririsan. Pilih periode lain atau edit budget yang sudah ada.");
        }

        budget.setUser(user);
        budget.setCategory(category);

        boolean isNew = budget.getId() == null;
        Budget saved = budgetRepo.save(budget);

        logService.log(
                user,
                isNew ? "CREATE" : "UPDATE",
                (isNew ? "Membuat budget kategori " : "Mengubah budget kategori ")
                        + category.getName()
                        + " sebesar "
                        + formatRupiah(saved.getAmount()));

        return saved;
    }

    private void validateBudget(Budget budget) {
        if (budget.getAmount() == null || budget.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Nominal budget harus lebih dari 0.");
        }

        if (budget.getStartDate() == null || budget.getEndDate() == null) {
            throw new IllegalArgumentException("Tanggal mulai dan tanggal selesai wajib diisi.");
        }

        if (budget.getEndDate().isBefore(budget.getStartDate())) {
            throw new IllegalArgumentException("Tanggal selesai tidak boleh sebelum tanggal mulai.");
        }

        if (budget.getCategory() == null || budget.getCategory().getId() == null) {
            throw new IllegalArgumentException("Kategori wajib dipilih.");
        }
    }

    public List<Budget> findByUser(User user) {
        return budgetRepo.findByUserOrderByStartDateDesc(user);
    }

    public Budget findById(Long id, User user) {
        Budget budget = budgetRepo.findByIdAndUser(id, user);

        if (budget == null) {
            throw new IllegalArgumentException("Budget tidak ditemukan.");
        }

        return budget;
    }

    public void delete(Long id, User user) {
        Budget budget = budgetRepo.findByIdAndUser(id, user);

        if (budget == null) {
            throw new IllegalArgumentException("Budget tidak ditemukan.");
        }

        budgetRepo.delete(budget);

        logService.log(
                user,
                "DELETE",
                "Menghapus budget kategori "
                        + budget.getCategory().getName()
                        + " sebesar "
                        + formatRupiah(budget.getAmount()));
    }

    public BudgetSummary createSummary(Budget budget) {
        BigDecimal usedAmount = budgetRepo.getUsedAmount(
                budget.getUser(),
                budget.getCategory().getId(),
                budget.getStartDate(),
                budget.getEndDate());

        if (usedAmount == null) {
            usedAmount = BigDecimal.ZERO;
        }

        BigDecimal remainingAmount = budget.getAmount().subtract(usedAmount);
        int percentage = calculatePercentage(usedAmount, budget.getAmount());
        String status = determineStatus(usedAmount, budget.getAmount());
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Jakarta"));

        String periodStatus;
        long daysRemaining;

        if (today.isBefore(budget.getStartDate())) {
            periodStatus = "UPCOMING";
            daysRemaining = ChronoUnit.DAYS.between(today, budget.getStartDate());
        } else if (today.isAfter(budget.getEndDate())) {
            periodStatus = "ENDED";
            daysRemaining = 0;
        } else {
            periodStatus = "ACTIVE";
            daysRemaining = ChronoUnit.DAYS.between(today, budget.getEndDate());
        }

        return new BudgetSummary(
                budget.getId(),
                budget.getCategory().getName(),
                budget.getAmount(),
                usedAmount,
                remainingAmount,
                percentage,
                budget.getStartDate(),
                budget.getEndDate(),
                status,
                periodStatus,
                daysRemaining);
    }

    public List<BudgetSummary> getBudgetSummaries(User user) {
        return findByUser(user).stream().map(this::createSummary).toList();
    }

    public BudgetOverview getOverview(List<BudgetSummary> summaries) {
        BigDecimal totalBudget = summaries.stream()
                .map(BudgetSummary::getBudgetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUsed = summaries.stream()
                .map(BudgetSummary::getUsedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRemaining = summaries.stream()
                .map(BudgetSummary::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long activeCount = summaries.stream()
                .filter(item -> "ACTIVE".equals(item.getPeriodStatus()))
                .count();

        return new BudgetOverview(
                totalBudget,
                totalUsed,
                totalRemaining,
                activeCount,
                summaries.size());
    }

    private int calculatePercentage(BigDecimal used, BigDecimal budget) {
        if (budget == null || budget.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        return used.multiply(BigDecimal.valueOf(100))
                .divide(budget, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private String determineStatus(BigDecimal used, BigDecimal budget) {
        if (used.compareTo(budget) > 0) {
            return "OVER";
        }

        BigDecimal percentage = used.multiply(BigDecimal.valueOf(100))
                .divide(budget, 2, RoundingMode.HALF_UP);

        if (percentage.compareTo(BigDecimal.valueOf(80)) >= 0) {
            return "WARNING";
        }

        return "SAFE";
    }

    private String formatRupiah(BigDecimal amount) {
        return "Rp " + String.format("%,.0f", amount).replace(',', '.');
    }
}
