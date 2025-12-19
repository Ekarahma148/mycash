package com.project.mycash.repositories;

import com.project.mycash.models.CashTransaction;
import com.project.mycash.models.JournalEntry;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JournalEntryRepository
        extends JpaRepository<JournalEntry, Long> {

    Optional<JournalEntry> findByTransaction(CashTransaction transaction);

    @Modifying
    @Transactional
    @Query("DELETE FROM JournalEntry j WHERE j.transaction.id = :id")
    void deleteByTransactionId(Long id);

}
