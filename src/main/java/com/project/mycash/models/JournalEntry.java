package com.project.mycash.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "journal_entries")
@Data
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private BigDecimal amount;
    private String debitAccount;
    private String creditAccount;

    @OneToOne
    @JoinColumn(name = "transaction_id", unique = true)
    private CashTransaction transaction;
}
