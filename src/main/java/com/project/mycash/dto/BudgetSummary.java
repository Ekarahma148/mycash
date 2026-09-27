package com.project.mycash.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BudgetSummary {

    private Long id;

    private String categoryName;

    private BigDecimal budgetAmount;

    private BigDecimal usedAmount;

    private BigDecimal remainingAmount;

    private int percentage;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status;
}