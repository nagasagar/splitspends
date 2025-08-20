
package com.dasa.splitspends.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "attachments", indexes = {
        @Index(name = "idx_attachment_expense", columnList = "expense_id"),
        @Index(name = "idx_attachment_uploaded_by", columnList = "uploaded_by"),
        @Index(name = "idx_attachment_type", columnList = "attachment_type"),
        @Index(name = "idx_attachment_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== ATTACHMENT RELATIONSHIPS ==========

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    @JsonIgnore
    private Expense expense;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    @JsonIgnore
    private User uploadedBy;

    // ========== FILE INFORMATION ==========

    @NotBlank(message = "Original filename is required")
    @Size(max = 255, message = "Filename cannot exceed 255 characters")
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @NotBlank(message = "Stored filename is required")
    @Size(max = 255, message = "Stored filename cannot exceed 255 characters")
    @Column(name = "stored_filename", nullable = false, unique = true, length = 255)
    private String storedFilename;

    @NotBlank(message = "File path is required")
    @Size(max = 500, message = "File path cannot exceed 500 characters")
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_url", length = 500)
    private String fileUrl; // Public URL if stored in cloud

    @Positive(message = "File size must be positive")
    @Column(name = "file_size", nullable = false)
    private Long fileSize; // Size in bytes

    @NotBlank(message = "Content type is required")
    @Size(max = 100, message = "Content type cannot exceed 100 characters")
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_type", nullable = false, length = 20)
    private AttachmentType attachmentType;

    // ========== METADATA ==========

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Image-specific metadata
    @Column(name = "image_width")
    private Integer imageWidth;

    @Column(name = "image_height")
    private Integer imageHeight;

    // File checksum for integrity verification
    @Column(name = "file_checksum", length = 64)
    private String fileChecksum;

    // Storage provider (local, s3, gcs, etc.)
    @Column(name = "storage_provider", length = 20)
    @Builder.Default
    private String storageProvider = "local";

    // ========== BUSINESS LOGIC METHODS ==========

    /**
     * Check if attachment is an image
     */
    public boolean isImage() {
        return AttachmentType.RECEIPT_IMAGE.equals(attachmentType) ||
                AttachmentType.DOCUMENT_IMAGE.equals(attachmentType) ||
                (contentType != null && contentType.startsWith("image/"));
    }

    /**
     * Check if attachment is a PDF
     */
    public boolean isPdf() {
        return AttachmentType.PDF_DOCUMENT.equals(attachmentType) ||
                "application/pdf".equals(contentType);
    }

    /**
     * Check if attachment is deleted
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Soft delete the attachment
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Get formatted file size
     */
    public String getFormattedFileSize() {
        if (fileSize == null)
            return "Unknown";

        long bytes = fileSize;
        if (bytes < 1024)
            return bytes + " B";

        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String[] units = { "B", "KB", "MB", "GB", "TB" };

        return String.format("%.1f %s",
                bytes / Math.pow(1024, exp),
                units[exp]);
    }

    /**
     * Get file extension
     */
    public String getFileExtension() {
        if (originalFilename == null)
            return "";
        int lastDot = originalFilename.lastIndexOf('.');
        return lastDot > 0 ? originalFilename.substring(lastDot + 1).toLowerCase() : "";
    }

    /**
     * Check if file type is allowed
     */
    public boolean isAllowedFileType() {
        String extension = getFileExtension();
        String[] allowedExtensions = {
                "jpg", "jpeg", "png", "gif", "bmp", "webp", // Images
                "pdf", "doc", "docx", "txt", "rtf", // Documents
                "xls", "xlsx", "csv" // Spreadsheets
        };

        for (String allowed : allowedExtensions) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get display name for the attachment
     */
    public String getDisplayName() {
        return description != null && !description.trim().isEmpty()
                ? description
                : originalFilename;
    }

    /**
     * Generate thumbnail URL for images
     */
    public String getThumbnailUrl() {
        if (!isImage() || fileUrl == null)
            return null;

        // For cloud storage, generate thumbnail URL
        if ("s3".equals(storageProvider) || "gcs".equals(storageProvider)) {
            return fileUrl.replace("/original/", "/thumb/");
        }

        // For local storage, generate thumbnail path
        return fileUrl.replace(".", "_thumb.");
    }

    // ========== VALIDATION METHODS ==========

    @PrePersist
    private void validateAttachment() {
        // Determine attachment type based on content type if not set
        if (attachmentType == null && contentType != null) {
            if (contentType.startsWith("image/")) {
                attachmentType = AttachmentType.RECEIPT_IMAGE;
            } else if ("application/pdf".equals(contentType)) {
                attachmentType = AttachmentType.PDF_DOCUMENT;
            } else {
                attachmentType = AttachmentType.OTHER;
            }
        }

        // Validate file size (max 10MB)
        if (fileSize != null && fileSize > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size cannot exceed 10MB");
        }

        // Validate file type
        if (!isAllowedFileType()) {
            throw new IllegalArgumentException("File type not allowed: " + getFileExtension());
        }

        // Generate stored filename if not provided
        if (storedFilename == null || storedFilename.isEmpty()) {
            String extension = getFileExtension();
            storedFilename = java.util.UUID.randomUUID().toString() +
                    (extension.isEmpty() ? "" : "." + extension);
        }
    }

    // ========== STATIC FACTORY METHODS ==========

    /**
     * Create receipt image attachment
     */
    public static Attachment createReceiptImage(Expense expense, User uploadedBy,
            String originalFilename, String contentType, Long fileSize) {
        return Attachment.builder()
                .expense(expense)
                .uploadedBy(uploadedBy)
                .originalFilename(originalFilename)
                .contentType(contentType)
                .fileSize(fileSize)
                .attachmentType(AttachmentType.RECEIPT_IMAGE)
                .description("Receipt for " + expense.getDescription())
                .build();
    }

    // ========== ENUMS ==========

    public enum AttachmentType {
        RECEIPT_IMAGE("Receipt image"),
        DOCUMENT_IMAGE("Document image"),
        PDF_DOCUMENT("PDF document"),
        INVOICE("Invoice"),
        CONTRACT("Contract"),
        OTHER("Other attachment");

        private final String description;

        AttachmentType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
