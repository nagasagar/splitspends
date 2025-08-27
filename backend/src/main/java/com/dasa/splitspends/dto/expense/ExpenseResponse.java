package com.dasa.splitspends.dto.expense;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.dasa.splitspends.entity.Expense;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {

    private Long id;
    private String description;
    private BigDecimal amount;
    private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;

    private String category;
    private String status;
    private String notes;

    // Payer information
    private Long paidById;
    private String paidByName;
    private String paidByEmail;

    // Group information
    private Long groupId;
    private String groupName;

    // Split information
    private Integer participantCount;
    private BigDecimal totalSplitAmount;
    private Boolean isFullySettled;
    private BigDecimal unsettledAmount;

    // Attachment information
    private Boolean hasReceipts;
    private String attachmentUrl;
    private String attachmentFilename;

    // Audit information
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private Long ageInDays;

    // Optional: List of splits (if needed for detailed view)
    private List<ExpenseSplitResponse> splits;

    /**
     * Convert entity to response DTO
     */
    public static ExpenseResponse fromEntity(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .currency(expense.getCurrency())
                .date(expense.getDate())
                .category(expense.getCategory() != null ? expense.getCategory().getDisplayName() : null)
                .status(expense.getStatus() != null ? expense.getStatus().name() : null)
                .notes(expense.getNotes())
                .paidById(expense.getPaidBy().getId())
                .paidByName(expense.getPaidBy().getName())
                .paidByEmail(expense.getPaidBy().getEmail())
                .groupId(expense.getGroup().getId())
                .groupName(expense.getGroup().getName())
                .participantCount(expense.getParticipantCount())
                .totalSplitAmount(expense.getTotalSplitAmount())
                .isFullySettled(expense.isFullySettled())
                .unsettledAmount(expense.getUnsettledAmount())
                .hasReceipts(expense.hasReceipts())
                .attachmentUrl(expense.getAttachmentUrl())
                .attachmentFilename(expense.getAttachmentFilename())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .ageInDays(expense.getAgeInDays())
                .build();
    }

    /**
     * Convert entity to response DTO with splits
     */
    public static ExpenseResponse fromEntityWithSplits(Expense expense) {
        ExpenseResponse response = fromEntity(expense);
        response.setSplits(expense.getSplits().stream()
                .map(ExpenseSplitResponse::fromEntity)
                .collect(Collectors.toList()));
        return response;
    }

    /**
     * Get formatted amount string
     */
    public String getFormattedAmount() {
        return String.format("%s %.2f", currency, amount);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseSplitResponse {
        private Long id;
        private Long userId;
        private String userName;
        private BigDecimal shareAmount;
        private BigDecimal sharePercentage;
        private String splitType;
        private Boolean isSettled;

        public static ExpenseSplitResponse fromEntity(com.dasa.splitspends.entity.ExpenseSplit split) {
            return ExpenseSplitResponse.builder()
                    .id(split.getId())
                    .userId(split.getUser().getId())
                    .userName(split.getUser().getName())
                    .shareAmount(split.getShareAmount())
                    .sharePercentage(split.getPercentage())
                    .splitType(split.getSplitType() != null ? split.getSplitType().name() : null)
                    .isSettled(split.isSettled())
                    .build();
        }
    }
}
