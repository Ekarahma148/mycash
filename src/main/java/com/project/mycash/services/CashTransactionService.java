package com.project.mycash.services;

import java.math.BigDecimal;
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

    private String formatRupiah(Number amount) {
        return "Rp " + String.format("%,.0f", amount)
                .replace(',', '.');
    }

    public CashTransaction save(CashTransaction tx) {

        boolean isNew = (tx.getId() == null);

        if (tx.getCategory() == null || tx.getCategory().getId() == null) {
            throw new RuntimeException("Kategori wajib dipilih");
        }

        // 🔥 AMBIL KATEGORI ASLI
        CategoryKas category = categoryRepo.findById(tx.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Kategori tidak ditemukan"));

        if (category.getAccountName() == null || category.getAccountName().isBlank()) {
            throw new RuntimeException("Kategori belum memiliki akun jurnal");
        }

        tx.setCategory(category);

        // =========================
        // 🔴 VALIDASI SALDO
        // =========================
        if (tx.getType() == TransactionType.OUT) {

            BigDecimal saldo = repo.getSaldoUser(tx.getUser());

            if (!isNew) {
                CashTransaction old = repo.findById(tx.getId()).orElse(null);
                if (old != null && old.getType() == TransactionType.OUT) {
                    saldo = saldo.add(old.getAmount());
                }
            }

            if (tx.getAmount().compareTo(saldo) > 0) {
                throw new RuntimeException(
                        "Saldo tidak mencukupi. Saldo tersedia: Rp " + saldo);
            }
        }

        // =========================
        // SIMPAN TRANSAKSI
        // =========================
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

        String tipe = saved.getType() == TransactionType.IN
                ? "pemasukan"
                : "pengeluaran";

        String nominal = formatRupiah(saved.getAmount());

        String aksi = isNew ? "Menambahkan" : "Mengubah";

        String keterangan = aksi + " " + tipe +
                " " + saved.getDescription() +
                " sebesar " + nominal;

        logService.log(
                saved.getUser(),
                isNew ? "CREATE" : "UPDATE",
                keterangan);

        return saved;
    }

    public void delete(Long id) {
        CashTransaction tx = repo.findById(id).orElse(null);
        if (tx == null)
            return;

        journalRepo.deleteByTransactionId(id);
        repo.deleteById(id);

        String tipe = tx.getType() == TransactionType.IN
                ? "pemasukan"
                : "pengeluaran";

        String nominal = formatRupiah(tx.getAmount());

        logService.log(
                tx.getUser(),
                "DELETE",
                "Menghapus " + tipe +
                        " " + tx.getDescription() +
                        " sebesar " + nominal);
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
