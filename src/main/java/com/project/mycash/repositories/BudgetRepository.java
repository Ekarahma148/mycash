package com.project.mycash.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.mycash.models.Budget;
import com.project.mycash.models.User;

public interface BudgetRepository
        extends JpaRepository<Budget, Long> {

    List<Budget> findByUserOrderByStartDateDesc(User user);

    Budget findByIdAndUser(Long id, User user);

    @Query("""
        SELECT COUNT(b)
        FROM Budget b
        WHERE b.user = :user
          AND b.category.id = :categoryId
          AND (:budgetId IS NULL OR b.id <> :budgetId)
          AND b.startDate <= :endDate
          AND b.endDate >= :startDate
    """)
    long countOverlappingBudgets(
            @Param("user") User user,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("budgetId") Long budgetId
    );

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM CashTransaction t
        WHERE t.user = :user
          AND t.category.id = :categoryId
          AND t.type = com.project.mycash.models.TransactionType.OUT
          AND t.date BETWEEN :startDate AND :endDate
    """)
    BigDecimal getUsedAmount(
            @Param("user") User user,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}