package com.project.mycash.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import com.project.mycash.models.CashTransaction;
import com.project.mycash.models.User;

public interface CashTransactionRepository
                extends JpaRepository<CashTransaction, Long> {

        List<CashTransaction> findByUser(User user);

        List<CashTransaction> findByUserAndDateBetween(
                        User user,
                        LocalDate start,
                        LocalDate end);

        List<CashTransaction> findByDescriptionContainingIgnoreCase(
                        String keyword,
                        Sort sortField);
}
