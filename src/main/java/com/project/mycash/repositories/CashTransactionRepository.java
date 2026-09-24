package com.project.mycash.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.mycash.models.CashTransaction;
import com.project.mycash.models.TransactionType;
import com.project.mycash.models.User;

public interface CashTransactionRepository
        extends JpaRepository<CashTransaction, Long> {

    List<CashTransaction> findByUser(User user);

    CashTransaction findByIdAndUser(Long id, User user);

    List<CashTransaction> findByUserAndDateBetween(
            User user,
            LocalDate start,
            LocalDate end,
            Sort sort);

    CashTransaction findFirstByUserAndTypeOrderByDateAsc(
            User user,
            TransactionType type);

    List<CashTransaction> findByUserAndTypeAndDateBetween(
            User user,
            TransactionType type,
            LocalDate start,
            LocalDate end,
            Sort sort);

    List<CashTransaction> findByUserAndDescriptionContainingIgnoreCase(
            User user,
            String keyword,
            Sort sort);

    @Query("""
            SELECT COALESCE(
                SUM(
                    CASE
                        WHEN t.type = 'IN' THEN t.amount
                        ELSE -t.amount
                    END
                ), 0
            )
            FROM CashTransaction t
            WHERE t.user = :user
            """)
    BigDecimal getSaldoUser(@Param("user") User user);

    // ==========================================
    // ANALYTICS - PEMASUKAN & PENGELUARAN BULANAN
    // ==========================================

    @Query("""
            SELECT
                MONTH(t.date),
                t.type,
                COALESCE(SUM(t.amount), 0)
            FROM CashTransaction t
            WHERE t.user = :user
              AND YEAR(t.date) = :year
            GROUP BY MONTH(t.date), t.type
            ORDER BY MONTH(t.date)
            """)
    List<Object[]> getMonthlySummary(
            @Param("user") User user,
            @Param("year") int year);

    // ==========================================
    // ANALYTICS - PENGELUARAN PER KATEGORI
    // ==========================================

    @Query("""
            SELECT
                t.category.name,
                COALESCE(SUM(t.amount), 0)
            FROM CashTransaction t
            WHERE t.user = :user
              AND t.type = 'OUT'
              AND YEAR(t.date) = :year
            GROUP BY t.category.name
            ORDER BY SUM(t.amount) DESC
            """)
    List<Object[]> getExpenseByCategory(
            @Param("user") User user,
            @Param("year") int year);
}