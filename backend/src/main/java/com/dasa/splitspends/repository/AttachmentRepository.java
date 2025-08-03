package com.dasa.splitspends.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dasa.splitspends.entity.Attachment;
import com.dasa.splitspends.entity.Expense;
import com.dasa.splitspends.entity.User;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

        // ========== BASIC QUERIES ==========

        /**
         * Find attachments by expense
         */
        List<Attachment> findByExpenseOrderByCreatedAtDesc(Expense expense);

        /**
         * Find attachments by uploader
         */
        List<Attachment> findByUploadedByOrderByCreatedAtDesc(User uploadedBy);

        /**
         * Find attachments by type
         */
        List<Attachment> findByAttachmentTypeOrderByCreatedAtDesc(Attachment.AttachmentType attachmentType);

        /**
         * Find attachments by content type
         */
        List<Attachment> findByContentTypeOrderByCreatedAtDesc(String contentType);

        // ========== ACTIVE/DELETED QUERIES ==========

        /**
         * Find active (non-deleted) attachments for an expense
         */
        @Query("SELECT a FROM Attachment a WHERE a.expense = :expense AND a.deletedAt IS NULL " +
                        "ORDER BY a.createdAt DESC")
        List<Attachment> findActiveByExpense(@Param("expense") Expense expense);

        /**
         * Find active attachments by user
         */
        @Query("SELECT a FROM Attachment a WHERE a.uploadedBy = :user AND a.deletedAt IS NULL " +
                        "ORDER BY a.createdAt DESC")
        List<Attachment> findActiveByUser(@Param("user") User user);

        /**
         * Find deleted attachments
         */
        @Query("SELECT a FROM Attachment a WHERE a.deletedAt IS NOT NULL " +
                        "ORDER BY a.deletedAt DESC")
        List<Attachment> findDeletedAttachments();

        /**
         * Find deleted attachments by user
         */
        @Query("SELECT a FROM Attachment a WHERE a.uploadedBy = :user AND a.deletedAt IS NOT NULL " +
                        "ORDER BY a.deletedAt DESC")
        List<Attachment> findDeletedByUser(@Param("user") User user);

        // ========== FILE-SPECIFIC QUERIES ==========

        /**
         * Find attachment by stored filename
         */
        @Query("SELECT a FROM Attachment a WHERE a.storedFilename = :filename AND a.deletedAt IS NULL")
        Attachment findByStoredFilename(@Param("filename") String filename);

        /**
         * Find attachment by file path
         */
        @Query("SELECT a FROM Attachment a WHERE a.filePath = :filePath AND a.deletedAt IS NULL")
        Attachment findByFilePath(@Param("filePath") String filePath);

        /**
         * Find attachment by checksum
         */
        @Query("SELECT a FROM Attachment a WHERE a.fileChecksum = :checksum AND a.deletedAt IS NULL")
        List<Attachment> findByChecksum(@Param("checksum") String checksum);

        /**
         * Check if stored filename exists
         */
        boolean existsByStoredFilename(String storedFilename);

        /**
         * Check if file path exists
         */
        boolean existsByFilePath(String filePath);

        // ========== TYPE-SPECIFIC QUERIES ==========

        /**
         * Find image attachments
         */
        @Query("SELECT a FROM Attachment a WHERE a.contentType LIKE 'image/%' " +
                        "AND a.deletedAt IS NULL ORDER BY a.createdAt DESC")
        List<Attachment> findImageAttachments();

        /**
         * Find PDF attachments
         */
        @Query("SELECT a FROM Attachment a WHERE a.contentType = 'application/pdf' " +
                        "AND a.deletedAt IS NULL ORDER BY a.createdAt DESC")
        List<Attachment> findPdfAttachments();

        /**
         * Find receipt images for an expense
         */
        @Query("SELECT a FROM Attachment a WHERE a.expense = :expense " +
                        "AND a.attachmentType = 'RECEIPT_IMAGE' AND a.deletedAt IS NULL " +
                        "ORDER BY a.createdAt DESC")
        List<Attachment> findReceiptsByExpense(@Param("expense") Expense expense);

        /**
         * Find attachments by type for an expense
         */
        @Query("SELECT a FROM Attachment a WHERE a.expense = :expense " +
                        "AND a.attachmentType = :type AND a.deletedAt IS NULL " +
                        "ORDER BY a.createdAt DESC")
        List<Attachment> findByExpenseAndType(@Param("expense") Expense expense,
                        @Param("type") Attachment.AttachmentType type);

        // ========== SIZE-BASED QUERIES ==========

        /**
         * Find large attachments (over specified size in bytes)
         */
        @Query("SELECT a FROM Attachment a WHERE a.fileSize > :sizeThreshold " +
                        "AND a.deletedAt IS NULL ORDER BY a.fileSize DESC")
        List<Attachment> findLargeAttachments(@Param("sizeThreshold") Long sizeThreshold);

        /**
         * Find attachments by size range
         */
        @Query("SELECT a FROM Attachment a WHERE a.fileSize BETWEEN :minSize AND :maxSize " +
                        "AND a.deletedAt IS NULL ORDER BY a.fileSize DESC")
        List<Attachment> findBySizeRange(@Param("minSize") Long minSize, @Param("maxSize") Long maxSize);

        /**
         * Get total storage used by user
         */
        @Query("SELECT COALESCE(SUM(a.fileSize), 0) FROM Attachment a WHERE a.uploadedBy = :user " +
                        "AND a.deletedAt IS NULL")
        Long getTotalStorageByUser(@Param("user") User user);

        /**
         * Get total storage used by expense
         */
        @Query("SELECT COALESCE(SUM(a.fileSize), 0) FROM Attachment a WHERE a.expense = :expense " +
                        "AND a.deletedAt IS NULL")
        Long getTotalStorageByExpense(@Param("expense") Expense expense);

        // ========== DATE-BASED QUERIES ==========

        /**
         * Find attachments uploaded within date range
         */
        @Query("SELECT a FROM Attachment a WHERE a.createdAt BETWEEN :startDate AND :endDate " +
                        "AND a.deletedAt IS NULL ORDER BY a.createdAt DESC")
        List<Attachment> findByUploadDateRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Find recent attachments (last 7 days)
         */
        @Query("SELECT a FROM Attachment a WHERE a.createdAt >= :sevenDaysAgo " +
                        "AND a.deletedAt IS NULL ORDER BY a.createdAt DESC")
        List<Attachment> findRecentAttachments(@Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);

        /**
         * Find old attachments (older than specified date)
         */
        @Query("SELECT a FROM Attachment a WHERE a.createdAt < :cutoffDate " +
                        "ORDER BY a.createdAt ASC")
        List<Attachment> findOldAttachments(@Param("cutoffDate") LocalDateTime cutoffDate);

        /**
         * Find attachments uploaded today
         */
        @Query(value = "SELECT * FROM attachment WHERE DATE(created_at) = CURRENT_DATE " +
                        "ORDER BY created_at DESC", nativeQuery = true)
        List<Attachment> findTodaysAttachments();

        // ========== STORAGE PROVIDER QUERIES ==========

        /**
         * Find attachments by storage provider
         */
        @Query("SELECT a FROM Attachment a WHERE a.storageProvider = :provider " +
                        "AND a.deletedAt IS NULL ORDER BY a.createdAt DESC")
        List<Attachment> findByStorageProvider(@Param("provider") String provider);

        /**
         * Find local storage attachments
         */
        @Query("SELECT a FROM Attachment a WHERE a.storageProvider = 'local' " +
                        "AND a.deletedAt IS NULL ORDER BY a.createdAt DESC")
        List<Attachment> findLocalStorageAttachments();

        /**
         * Find cloud storage attachments
         */
        @Query("SELECT a FROM Attachment a WHERE a.storageProvider IN ('s3', 'gcs', 'azure') " +
                        "AND a.deletedAt IS NULL ORDER BY a.createdAt DESC")
        List<Attachment> findCloudStorageAttachments();

        // ========== PAGINATION QUERIES ==========

        /**
         * Find attachments by expense with pagination
         */
        Page<Attachment> findByExpenseAndDeletedAtIsNullOrderByCreatedAtDesc(Expense expense, Pageable pageable);

        /**
         * Find attachments by user with pagination
         */
        Page<Attachment> findByUploadedByAndDeletedAtIsNullOrderByCreatedAtDesc(User uploadedBy, Pageable pageable);

        /**
         * Find attachments by type with pagination
         */
        Page<Attachment> findByAttachmentTypeAndDeletedAtIsNullOrderByCreatedAtDesc(Attachment.AttachmentType type,
                        Pageable pageable);

        // ========== STATISTICS QUERIES ==========

        /**
         * Count attachments by user
         */
        @Query("SELECT COUNT(a) FROM Attachment a WHERE a.uploadedBy = :user AND a.deletedAt IS NULL")
        long countByUser(@Param("user") User user);

        /**
         * Count attachments by expense
         */
        @Query("SELECT COUNT(a) FROM Attachment a WHERE a.expense = :expense AND a.deletedAt IS NULL")
        long countByExpense(@Param("expense") Expense expense);

        /**
         * Count attachments by type
         */
        @Query("SELECT COUNT(a) FROM Attachment a WHERE a.attachmentType = :type AND a.deletedAt IS NULL")
        long countByType(@Param("type") Attachment.AttachmentType type);

        /**
         * Get attachment statistics by type
         */
        @Query("SELECT a.attachmentType, COUNT(a), AVG(a.fileSize) FROM Attachment a " +
                        "WHERE a.deletedAt IS NULL GROUP BY a.attachmentType")
        List<Object[]> getAttachmentStatsByType();

        /**
         * Get storage statistics by provider
         */
        @Query("SELECT a.storageProvider, COUNT(a), SUM(a.fileSize) FROM Attachment a " +
                        "WHERE a.deletedAt IS NULL GROUP BY a.storageProvider")
        List<Object[]> getStorageStatsByProvider();

        /**
         * Get daily upload statistics for the last month
         */
        @Query("SELECT DATE(a.createdAt), COUNT(a), SUM(a.fileSize) FROM Attachment a " +
                        "WHERE a.createdAt >= :monthAgo AND a.deletedAt IS NULL " +
                        "GROUP BY DATE(a.createdAt) ORDER BY DATE(a.createdAt)")
        List<Object[]> getDailyUploadStats(@Param("monthAgo") LocalDateTime monthAgo);

        // ========== BULK OPERATIONS ==========

        /**
         * Soft delete attachments for an expense
         */
        @Modifying
        @Query("UPDATE Attachment a SET a.deletedAt = CURRENT_TIMESTAMP WHERE a.expense = :expense " +
                        "AND a.deletedAt IS NULL")
        int softDeleteByExpense(@Param("expense") Expense expense);

        /**
         * Permanently delete old soft-deleted attachments
         */
        @Modifying
        @Query("DELETE FROM Attachment a WHERE a.deletedAt IS NOT NULL " +
                        "AND a.deletedAt < :cutoffDate")
        int permanentlyDeleteOldAttachments(@Param("cutoffDate") LocalDateTime cutoffDate);

        /**
         * Count soft-deleted attachments for cleanup
         */
        @Query("SELECT COUNT(a) FROM Attachment a WHERE a.deletedAt IS NOT NULL " +
                        "AND a.deletedAt < :cutoffDate")
        long countSoftDeletedForCleanup(@Param("cutoffDate") LocalDateTime cutoffDate);

        // ========== VALIDATION QUERIES ==========

        /**
         * Find duplicate attachments (same checksum)
         */
        @Query("SELECT a FROM Attachment a WHERE a.fileChecksum = :checksum " +
                        "AND a.deletedAt IS NULL ORDER BY a.createdAt ASC")
        List<Attachment> findDuplicatesByChecksum(@Param("checksum") String checksum);

        /**
         * Find orphaned attachments (no associated expense)
         */
        @Query("SELECT a FROM Attachment a WHERE a.expense IS NULL AND a.deletedAt IS NULL " +
                        "ORDER BY a.createdAt ASC")
        List<Attachment> findOrphanedAttachments();

        /**
         * Find attachments with missing files (file doesn't exist on disk)
         */
        @Query("SELECT a FROM Attachment a WHERE a.deletedAt IS NULL ORDER BY a.createdAt DESC")
        List<Attachment> findAllActiveForFileValidation();

        // ========== COMPLEX BUSINESS QUERIES ==========

        /**
         * Find user's recent uploads for quota checking
         */
        @Query("SELECT a FROM Attachment a WHERE a.uploadedBy = :user " +
                        "AND a.createdAt >= :since AND a.deletedAt IS NULL " +
                        "ORDER BY a.createdAt DESC")
        List<Attachment> findRecentUploadsByUser(@Param("user") User user,
                        @Param("since") LocalDateTime since);

        /**
         * Find expense attachments for export
         */
        @Query("SELECT a FROM Attachment a WHERE a.expense IN :expenses " +
                        "AND a.deletedAt IS NULL ORDER BY a.expense.id, a.createdAt")
        List<Attachment> findForExpenseExport(@Param("expenses") List<Expense> expenses);

        /**
         * Find attachments needing thumbnail generation
         */
        @Query("SELECT a FROM Attachment a WHERE a.contentType LIKE 'image/%' " +
                        "AND a.imageWidth IS NULL AND a.deletedAt IS NULL " +
                        "ORDER BY a.createdAt DESC")
        List<Attachment> findNeedingThumbnailGeneration();
}
