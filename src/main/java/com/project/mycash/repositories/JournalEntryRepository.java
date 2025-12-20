package com.project.mycash.repositories;

import com.project.mycash.models.CashTransaction;
import com.project.mycash.models.JournalEntry;
import com.project.mycash.models.User;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface JournalEntryRepository
        extends JpaRepository<JournalEntry, Long> {

    Optional<JournalEntry> findByTransaction(CashTransaction transaction);

    @Modifying
    @Transactional
    @Query("DELETE FROM JournalEntry j WHERE j.transaction.id = :id")
    void deleteByTransactionId(Long id);

    @Query("""
                SELECT j FROM JournalEntry j
                WHERE j.transaction.user = :user
            """)
    List<JournalEntry> findByUser(User user);
}
