package com.project.mycash.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.project.mycash.models.CashTransaction;
import com.project.mycash.models.JournalEntry;
import com.project.mycash.models.TransactionType;
import com.project.mycash.models.User;
import com.project.mycash.repositories.CashTransactionRepository;
import com.project.mycash.repositories.JournalEntryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashTransactionService {

    private final CashTransactionRepository repo;
    private final JournalEntryRepository journalRepo;
    private final ActivityLogService logService;

    public List<CashTransaction> findByPeriod(LocalDate start, LocalDate end) {
        return repo.findAll().stream()
                .filter(tx -> !tx.getDate().isBefore(start) && !tx.getDate().isAfter(end))
                .toList();
    }

    public List<CashTransaction> findByUserAndPeriod(
            User user,
            LocalDate start,
            LocalDate end) {
        return repo.findByUserAndDateBetween(user, start, end);
    }

    public CashTransaction findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public List<CashTransaction> getAll(String keyword, String sortField) {
        Sort sort = Sort.by(sortField).ascending();
        if (keyword != null && !keyword.isEmpty()) {
            return repo.findByDescriptionContainingIgnoreCase(keyword, sort);
        }
        return repo.findAll(sort);
    }

    @Transactional
    public CashTransaction save(CashTransaction tx) {

        boolean isNew = (tx.getId() == null);

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

        // 🔥 HISTORY
        if (isNew) {
            logService.log(
                    saved.getUser(),
                    "CREATE",
                    "Menambah transaksi: " + saved.getDescription());
        } else {
            logService.log(
                    saved.getUser(),
                    "UPDATE",
                    "Mengubah transaksi: " + saved.getDescription());
        }

        return saved;
    }

    @Transactional
    public void delete(Long id) {
        CashTransaction tx = repo.findById(id).orElse(null);
        if (tx == null)
            return;

        journalRepo.deleteByTransactionId(id);
        repo.deleteById(id);

        logService.log(
                tx.getUser(),
                "DELETE",
                "Menghapus transaksi: " + tx.getDescription());
    }
}
