package com.project.mycash.services;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import com.project.mycash.models.CashTransaction;
import com.project.mycash.models.CategoryKas;
import com.project.mycash.models.JournalEntry;
import com.project.mycash.models.TransactionType;
import com.project.mycash.models.User;
import com.project.mycash.repositories.CashTransactionRepository;
import com.project.mycash.repositories.CategoryKasRepository;
import com.project.mycash.repositories.JournalEntryRepository;
import org.springframework.data.domain.Sort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CashTransactionService {

    private final CashTransactionRepository repo;
    private final JournalEntryRepository journalRepo;
    private final ActivityLogService logService;
    private final CategoryKasRepository categoryRepo;

    public CashTransaction save(CashTransaction tx) {

        boolean isNew = (tx.getId() == null);

        if (tx.getCategory() == null || tx.getCategory().getId() == null) {
            throw new RuntimeException("Kategori wajib dipilih");
        }

        // 🔥 AMBIL KATEGORI ASLI DARI DATABASE
        CategoryKas category = categoryRepo.findById(tx.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));

        if (category.getAccountName() == null ||
                category.getAccountName().isBlank()) {
            throw new RuntimeException("Kategori belum memiliki akun jurnal");
        }

        // 🔥 SET CATEGORY YANG SUDAH LENGKAP
        tx.setCategory(category);

        CashTransaction saved = repo.save(tx);

        JournalEntry journal = journalRepo
                .findByTransaction(saved)
                .orElseGet(JournalEntry::new);

        journal.setTransaction(saved);
        journal.setDate(saved.getDate());
        journal.setAmount(saved.getAmount());

        if (saved.getType() == TransactionType.IN) {
            journal.setDebitAccount("Kas");
            journal.setCreditAccount(category.getAccountName());
        } else {
            journal.setDebitAccount(category.getAccountName());
            journal.setCreditAccount("Kas");
        }

        journalRepo.save(journal);

        logService.log(
                saved.getUser(),
                isNew ? "CREATE" : "UPDATE",
                (isNew ? "Menambah" : "Mengubah") +
                        " transaksi: " + saved.getDescription());

        return saved;
    }

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

    public List<CashTransaction> findByUserAndPeriod(
            User user,
            LocalDate start,
            LocalDate end,
            String sortDir) {

        Sort sort = Sort.by("date");

        if ("asc".equalsIgnoreCase(sortDir)) {
            sort = sort.ascending();
        } else {
            sort = sort.descending(); // default
        }

        return repo.findByUserAndDateBetween(user, start, end, sort);
    }

    public CashTransaction findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaksi tidak ditemukan"));
    }

}
