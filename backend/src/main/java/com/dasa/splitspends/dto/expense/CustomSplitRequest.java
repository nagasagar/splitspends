package com.dasa.splitspends.dto.expense;

import java.math.BigDecimal;
import java.util.Map;

public class CustomSplitRequest {
    private Long paidByUserId;
    private String description;
    private BigDecimal amount;
    private Map<Long, BigDecimal> userAmountMap;
    private String category;

    public Long getPaidByUserId() {
        return paidByUserId;
    }

    public void setPaidByUserId(Long paidByUserId) {
        this.paidByUserId = paidByUserId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Map<Long, BigDecimal> getUserAmountMap() {
        return userAmountMap;
    }

    public void setUserAmountMap(Map<Long, BigDecimal> userAmountMap) {
        this.userAmountMap = userAmountMap;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
