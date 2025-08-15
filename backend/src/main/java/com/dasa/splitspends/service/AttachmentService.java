package com.dasa.splitspends.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.dasa.splitspends.entity.Attachment;

public interface AttachmentService {

    /**
     * Upload a file attachment.
     */
    Attachment uploadFile(MultipartFile file, Long expenseId, Long uploadedByUserId, String description);

    /**
     * Download a file attachment.
     */
    byte[] downloadFile(Long attachmentId, Long requestingUserId);

    /**
     * Delete an attachment.
     */
    void deleteAttachment(Long attachmentId, Long deletedByUserId);

    /**
     * Get all attachments for an expense.
     */
    List<Attachment> getExpenseAttachments(Long expenseId);

    /**
     * Get all attachments uploaded by a user.
     */
    List<Attachment> getUserAttachments(Long userId);

    /**
     * Get total storage used by a user.
     */
    long getTotalStorageUsedByUser(Long userId);

    /**
     * Get attachments by file type.
     */
    List<Attachment> getAttachmentsByType(Attachment.FileType fileType);

    /**
     * Get attachment by ID.
     */
    Attachment getAttachmentById(Long attachmentId);

    /**
     * Find duplicate files by checksum.
     */
    List<Attachment> findDuplicateFiles(String checksum);

    /**
     * Cleanup deleted files older than specified days.
     */
    void cleanupDeletedFiles(int daysOld);
}