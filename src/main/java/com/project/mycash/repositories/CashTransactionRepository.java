package com.project.mycash.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.mycash.models.CashTransaction;
// import com.project.mycash.models.TransactionType;
import java.util.List;
import java.util.Optional;
public interface CashTransactionRepository extends JpaRepository<CashTransaction, Long> {
List<CashTransaction> findByDescriptionContainingIgnoreCase(String keyword, String sortField);
Optional<CashTransaction> findById(Long id);
}