package com.dasa.splitspends.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dasa.splitspends.entity.Attachment;
import com.dasa.splitspends.entity.Expense;
import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.repository.AttachmentRepository;

@Service
@Transactional
public class AttachmentService {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private ActivityLogService activityLogService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.max-file-size:10485760}") // 10MB default
    private long maxFileSize;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp");

    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
            "application/pdf", "text/plain", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

    // ========== BASIC CRUD OPERATIONS ==========

    /**
     * Upload an attachment for an expense
     */
    public Attachment uploadExpenseAttachment(MultipartFile file, Expense expense,
            String description, User uploadedBy) {
        validateFileUpload(file, expense.getGroup(), uploadedBy);

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String storedFilename = generateUniqueFilename(fileExtension);
        String contentType = file.getContentType();

        // Save file to storage
        Path filePath = saveFileToStorage(file, storedFilename);

        Attachment attachment = Attachment.builder()
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .contentType(contentType)
                .description(description)
                .expense(expense)
                .uploadedBy(uploadedBy)
                .build();

        Attachment saved = attachmentRepository.save(attachment);

        // Log activity
        activityLogService.logAttachmentUploaded(uploadedBy, expense.getGroup(), saved);

        return saved;
    }

    /**
     * Get attachment by ID
     */
    @Transactional(readOnly = true)
    public Optional<Attachment> getAttachmentById(Long id) {
        return attachmentRepository.findById(id);
    }

    /**
     * Get attachment by ID with access validation
     */
    @Transactional(readOnly = true)
    public Optional<Attachment> getAttachmentById(Long id, User user) {
        Optional<Attachment> attachment = attachmentRepository.findById(id);

        if (attachment.isPresent() && canUserAccessAttachment(attachment.get(), user)) {
            return attachment;
        }

        return Optional.empty();
    }

    /**
     * Update attachment description
     */
    public Attachment updateAttachmentDescription(Long id, String description, User user) {
        Attachment attachment = getAttachmentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));

        validateAttachmentAccess(attachment, user);

        attachment.setDescription(description);
        return attachmentRepository.save(attachment);
    }

    /**
     * Delete an attachment
     */
    public void deleteAttachment(Long id, User user) {
        Attachment attachment = getAttachmentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));

        validateAttachmentDeletion(attachment, user);

        // Delete file from storage
        deleteFileFromStorage(attachment);

        // Delete from database
        attachmentRepository.delete(attachment);

        // Log activity
        if (attachment.getExpense() != null) {
            activityLogService.logAttachmentDeleted(user, attachment.getExpense().getGroup(), attachment);
        }
    }

    // ========== QUERY OPERATIONS ==========

    /**
     * Get attachments for an expense
     */
    @Transactional(readOnly = true)
    public List<Attachment> getAttachmentsForExpense(Expense expense) {
        return attachmentRepository.findByExpenseOrderByCreatedAtDesc(expense);
    }

    /**
     * Get attachments uploaded by a user
     */
    @Transactional(readOnly = true)
    public Page<Attachment> getAttachmentsByUser(User user, Pageable pageable) {
        return attachmentRepository.findByUploadedByAndDeletedAtIsNullOrderByCreatedAtDesc(user, pageable);
    }

    /**
     * Get recent attachments for a user
     */
    @Transactional(readOnly = true)
    public List<Attachment> getRecentAttachmentsForUser(User user, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return attachmentRepository.findRecentUploadsByUser(user, since);
    }

    /**
     * Get image attachments
     */
    @Transactional(readOnly = true)
    public List<Attachment> getImageAttachments() {
        return attachmentRepository.findImageAttachments();
    }

    /**
     * Search attachments by filename for user
     */
    @Transactional(readOnly = true)
    public List<Attachment> searchAttachmentsByFilename(String filename, User user) {
        return attachmentRepository.findActiveByUser(user).stream()
                .filter(a -> a.getOriginalFilename().toLowerCase().contains(filename.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get orphaned attachments (no associated expense)
     */
    @Transactional(readOnly = true)
    public List<Attachment> getOrphanedAttachments() {
        return attachmentRepository.findOrphanedAttachments();
    }

    // ========== FILE OPERATIONS ==========

    /**
     * Get file content as InputStream
     */
    @Transactional(readOnly = true)
    public InputStream getFileContent(Long attachmentId, User user) throws IOException {
        Attachment attachment = getAttachmentById(attachmentId, user)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found or access denied"));

        Path filePath = Paths.get(attachment.getFilePath());
        if (!Files.exists(filePath)) {
            throw new IOException("File not found on storage: " + attachment.getOriginalFilename());
        }

        // Log download activity
        if (attachment.getExpense() != null) {
            activityLogService.logAttachmentDownloaded(user, attachment.getExpense().getGroup(), attachment);
        }

        return Files.newInputStream(filePath);
    }

    // ========== BULK OPERATIONS ==========

    /**
     * Delete attachments for an expense
     */
    @Transactional
    public void deleteAttachmentsForExpense(Expense expense, User user) {
        List<Attachment> attachments = getAttachmentsForExpense(expense);

        for (Attachment attachment : attachments) {
            if (canUserDeleteAttachment(attachment, user)) {
                deleteFileFromStorage(attachment);
                attachmentRepository.delete(attachment);
            }
        }
    }

    /**
     * Bulk delete attachments by IDs
     */
    @Transactional
    public void bulkDeleteAttachments(List<Long> attachmentIds, User user) {
        for (Long id : attachmentIds) {
            try {
                deleteAttachment(id, user);
            } catch (Exception e) {
                // Log error but continue with other deletions
                System.err.println("Failed to delete attachment " + id + ": " + e.getMessage());
            }
        }
    }

    // ========== MAINTENANCE OPERATIONS ==========

    /**
     * Clean up orphaned files
     */
    @Transactional
    public int cleanupOrphanedFiles() {
        List<Attachment> orphaned = getOrphanedAttachments();
        int cleaned = 0;

        for (Attachment attachment : orphaned) {
            try {
                deleteFileFromStorage(attachment);
                attachmentRepository.delete(attachment);
                cleaned++;
            } catch (Exception e) {
                System.err
                        .println("Failed to cleanup orphaned attachment " + attachment.getId() + ": " + e.getMessage());
            }
        }

        return cleaned;
    }

    /**
     * Clean up old attachments
     */
    @Transactional
    public int cleanupOldAttachments(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        List<Attachment> oldAttachments = attachmentRepository.findOldAttachments(cutoffDate);
        int cleaned = 0;

        for (Attachment attachment : oldAttachments) {
            try {
                deleteFileFromStorage(attachment);
                attachmentRepository.delete(attachment);
                cleaned++;
            } catch (Exception e) {
                System.err.println("Failed to cleanup old attachment " + attachment.getId() + ": " + e.getMessage());
            }
        }

        return cleaned;
    }

    /**
     * Validate file system integrity
     */
    @Transactional(readOnly = true)
    public FileSystemReport validateFileSystemIntegrity() {
        List<Attachment> allAttachments = attachmentRepository.findAll();
        int totalFiles = allAttachments.size();
        int missingFiles = 0;

        for (Attachment attachment : allAttachments) {
            // Check main file
            if (!Files.exists(Paths.get(attachment.getFilePath()))) {
                missingFiles++;
            }
        }

        return new FileSystemReport(totalFiles, missingFiles, 0);
    }

    // ========== STATISTICS AND ANALYTICS ==========

    /**
     * Get storage statistics for a user
     */
    @Transactional(readOnly = true)
    public StorageStats getStorageStatsForUser(User user) {
        long totalFiles = attachmentRepository.countByUser(user);
        long totalSize = attachmentRepository.getTotalStorageByUser(user);
        long imageCount = attachmentRepository.findActiveByUser(user).stream()
                .mapToLong(a -> a.isImage() ? 1 : 0)
                .sum();

        return new StorageStats(totalFiles, totalSize, imageCount);
    }

    // ========== UTILITY METHODS ==========

    private Path saveFileToStorage(MultipartFile file, String storedFilename) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(storedFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }

            return filePath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), e);
        }
    }

    private void deleteFileFromStorage(Attachment attachment) {
        try {
            // Delete main file
            Path filePath = Paths.get(attachment.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            System.err.println("Failed to delete file for attachment " + attachment.getId() + ": " + e.getMessage());
        }
    }

    private String generateUniqueFilename(String extension) {
        return UUID.randomUUID().toString() + (extension != null ? extension : "");
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private boolean isImageFile(String contentType) {
        return contentType != null && ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase());
    }

    private boolean isAllowedFileType(String contentType) {
        if (contentType == null) {
            return false;
        }

        // Use isImageFile method for consistency
        return isImageFile(contentType) || ALLOWED_DOCUMENT_TYPES.contains(contentType.toLowerCase());
    }

    // ========== VALIDATION METHODS ==========

    private void validateFileUpload(MultipartFile file, Group group, User user) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (!group.isMember(user)) {
            throw new IllegalArgumentException("User must be a member of the group to upload attachments");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size: " + maxFileSize + " bytes");
        }

        if (!isAllowedFileType(file.getContentType())) {
            throw new IllegalArgumentException("File type not allowed: " + file.getContentType());
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be empty");
        }
    }

    private void validateAttachmentAccess(Attachment attachment, User user) {
        if (!canUserAccessAttachment(attachment, user)) {
            throw new IllegalArgumentException("User does not have access to this attachment");
        }
    }

    private void validateAttachmentDeletion(Attachment attachment, User user) {
        if (!canUserDeleteAttachment(attachment, user)) {
            throw new IllegalArgumentException("User does not have permission to delete this attachment");
        }
    }

    private boolean canUserAccessAttachment(Attachment attachment, User user) {
        // User can access if they are a member of the expense's group
        if (attachment.getExpense() != null) {
            return attachment.getExpense().getGroup().isMember(user);
        }
        return false;
    }

    private boolean canUserDeleteAttachment(Attachment attachment, User user) {
        // User can delete if they uploaded it or are an admin of the group
        if (attachment.getUploadedBy().equals(user)) {
            return true;
        }

        if (attachment.getExpense() != null) {
            return attachment.getExpense().getGroup().isAdmin(user);
        }

        return false;
    }

    // ========== HELPER CLASSES ==========

    public static class StorageStats {
        private final long totalFiles;
        private final long totalSize;
        private final long imageCount;

        public StorageStats(long totalFiles, long totalSize, long imageCount) {
            this.totalFiles = totalFiles;
            this.totalSize = totalSize;
            this.imageCount = imageCount;
        }

        public long getTotalFiles() {
            return totalFiles;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public long getImageCount() {
            return imageCount;
        }

        public long getDocumentCount() {
            return totalFiles - imageCount;
        }

        public String getFormattedSize() {
            if (totalSize < 1024)
                return totalSize + " B";
            if (totalSize < 1024 * 1024)
                return String.format("%.1f KB", totalSize / 1024.0);
            if (totalSize < 1024 * 1024 * 1024)
                return String.format("%.1f MB", totalSize / (1024.0 * 1024));
            return String.format("%.1f GB", totalSize / (1024.0 * 1024 * 1024));
        }
    }

    public static class FileSystemReport {
        private final int totalFiles;
        private final int missingFiles;
        private final int invalidThumbnails;

        public FileSystemReport(int totalFiles, int missingFiles, int invalidThumbnails) {
            this.totalFiles = totalFiles;
            this.missingFiles = missingFiles;
            this.invalidThumbnails = invalidThumbnails;
        }

        public int getTotalFiles() {
            return totalFiles;
        }

        public int getMissingFiles() {
            return missingFiles;
        }

        public int getInvalidThumbnails() {
            return invalidThumbnails;
        }

        public int getValidFiles() {
            return totalFiles - missingFiles;
        }

        public double getIntegrityPercentage() {
            return totalFiles > 0 ? (double) getValidFiles() / totalFiles * 100 : 100.0;
        }
    }
}
