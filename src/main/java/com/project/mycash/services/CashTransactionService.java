package com.project.mycash.services;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.project.mycash.models.CashTransaction;
import com.project.mycash.models.JournalEntry;
import com.project.mycash.models.TransactionType;
import com.project.mycash.repositories.CashTransactionRepository;
import com.project.mycash.repositories.JournalEntryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashTransactionService {
    private final CashTransactionRepository repo;
    private final JournalEntryRepository journalRepo;

    public List<CashTransaction> findByPeriod(LocalDate start, LocalDate end) {
        return repo.findAll().stream()
                .filter(tx -> !tx.getDate().isBefore(start) && !tx.getDate().isAfter(end))
                .toList();
    }

    public CashTransaction findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Transactional
    public void delete(Long id) {
        journalRepo.deleteByTransactionId(id);
        repo.deleteById(id);
    }

    public List<CashTransaction> getAll(String keyword, String sortField) {
        Sort sort = Sort.by(sortField).ascending();
        if (keyword != null && !keyword.isEmpty()) {
            return repo.findByDescriptionContainingIgnoreCase(keyword, sortField);
        }
        return repo.findAll(sort);
    }

    @Transactional
    public CashTransaction save(CashTransaction tx) {
        CashTransaction saved = repo.save(tx);

        JournalEntry journal = journalRepo
                .findByTransaction(saved)
                .orElse(new JournalEntry());

        journal.setTransaction(saved);
        journal.setDate(saved.getDate());
        journal.setAmount(saved.getAmount());

        if (saved.getType() == TransactionType.IN) {
            journal.setDebitAccount("Kas");
            journal.setCreditAccount("Pendapatan");
        } else {
            journal.setDebitAccount("Beban");
            journal.setCreditAccount("Kas");
        }

        journalRepo.save(journal);
        return saved;
    }

    private void createJournal(CashTransaction tx) {
        JournalEntry j = new JournalEntry();
        j.setDate(tx.getDate());
        j.setAmount(tx.getAmount());
        j.setTransaction(tx);

        if (tx.getType() == TransactionType.IN) {
            j.setDebitAccount("Kas");
            j.setCreditAccount("Pendapatan");
        } else {
            j.setDebitAccount("Beban");
            j.setCreditAccount("Kas");
        }
        journalRepo.save(j);
    }
}