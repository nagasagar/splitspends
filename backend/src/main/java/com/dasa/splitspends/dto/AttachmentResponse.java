package com.dasa.splitspends.dto;

import java.time.LocalDateTime;

import com.dasa.splitspends.entity.Attachment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttachmentResponse {
    private Long id;
    private Long expenseId;
    private UserResponse uploadedBy;
    private String originalFilename;
    private String storedFilename;
    private Long fileSize;
    private String contentType;
    private Attachment.AttachmentType attachmentType;
    private String description;
    private String checksum;
    private String storageProvider;
    private String thumbnailUrl;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    
    public static AttachmentResponse fromEntity(Attachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .expenseId(attachment.getExpense() != null ? attachment.getExpense().getId() : null)
                .uploadedBy(attachment.getUploadedBy() != null ? UserResponse.fromEntity(attachment.getUploadedBy()) : null)
                .originalFilename(attachment.getOriginalFilename())
                .storedFilename(attachment.getStoredFilename())
                .fileSize(attachment.getFileSize())
                .contentType(attachment.getContentType())
                .fileType(attachment.getFileType())
                .description(attachment.getDescription())
                .checksum(attachment.getChecksum())
                .storageProvider(attachment.getStorageProvider())
                .thumbnailUrl(attachment.getThumbnailUrl())
                .isDeleted(attachment.getIsDeleted())
                .createdAt(attachment.getCreatedAt())
                .deletedAt(attachment.getDeletedAt())
                .build();
    }
}