package com.dasa.splitspends.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ExpenseStats {
    private Long totalExpenses;
    private BigDecimal totalAmount;
    private BigDecimal settledAmount;
    private BigDecimal averageExpenseAmount;

    public BigDecimal getUnsettledAmount() {
        if (totalAmount == null || settledAmount == null)
            return BigDecimal.ZERO;
        return totalAmount.subtract(settledAmount);
    }

    public double getSettlementPercentage() {
        if (totalAmount == null || totalAmount.equals(BigDecimal.ZERO))
            return 0.0;
        return settledAmount.divide(totalAmount, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
}
