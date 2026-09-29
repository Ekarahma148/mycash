package com.project.mycash.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BudgetOverview {

    private BigDecimal totalBudget;
    private BigDecimal totalUsed;
    private BigDecimal totalRemaining;
    private long activeCount;
    private long totalCount;
}
