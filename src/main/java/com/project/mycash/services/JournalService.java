package com.project.mycash.services;
import org.springframework.stereotype.Service;
import com.project.mycash.models.CashTransaction;
import com.project.mycash.models.JournalEntry;
import com.project.mycash.models.TransactionType;
import com.project.mycash.repositories.JournalEntryRepository;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class JournalService {

    private final JournalEntryRepository journalRepo;

    public void createJournal(CashTransaction tx) {
        JournalEntry journal = new JournalEntry();
        journal.setDate(tx.getDate());
        journal.setAmount(tx.getAmount());
        journal.setTransaction(tx);

        if (tx.getType() == TransactionType.IN) {
            journal.setDebitAccount("Kas");
            journal.setCreditAccount("Pendapatan");
        } else {
            journal.setDebitAccount("Beban");
            journal.setCreditAccount("Kas");
        }

        journalRepo.save(journal);
    }
}