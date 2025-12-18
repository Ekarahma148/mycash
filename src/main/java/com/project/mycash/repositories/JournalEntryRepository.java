package com.project.mycash.repositories;

import com.project.mycash.models.CashTransaction;
import com.project.mycash.models.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JournalEntryRepository
        extends JpaRepository<JournalEntry, Long> {

    Optional<JournalEntry> findByTransaction(CashTransaction transaction);
}
