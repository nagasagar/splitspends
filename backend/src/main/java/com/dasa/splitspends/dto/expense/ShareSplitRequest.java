package com.dasa.splitspends.dto.expense;

import java.math.BigDecimal;
import java.util.Map;

public class ShareSplitRequest {
    private Long paidByUserId;
    private String description;
    private BigDecimal amount;
    private Map<Long, Integer> userShareMap;
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

    public Map<Long, Integer> getUserShareMap() {
        return userShareMap;
    }

    public void setUserShareMap(Map<Long, Integer> userShareMap) {
        this.userShareMap = userShareMap;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
