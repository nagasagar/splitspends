package com.dasa.splitspends.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.dasa.splitspends.entity.Attachment;
import com.dasa.splitspends.entity.Expense;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.repository.AttachmentRepository;
import com.dasa.splitspends.repository.ExpenseRepository;
import com.dasa.splitspends.repository.UserRepository;
import com.dasa.splitspends.service.ActivityLogService;
import com.dasa.splitspends.service.AttachmentService;

@Service
@Transactional
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public AttachmentServiceImpl(AttachmentRepository attachmentRepository,
                                ExpenseRepository expenseRepository,
                                UserRepository userRepository,
                                ActivityLogService activityLogService) {
        this.attachmentRepository = attachmentRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
    }

    @Override
    public Attachment uploadFile(MultipartFile file, Long expenseId, Long uploadedByUserId, String description) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new RuntimeException("Expense not found"));
        User uploadedBy = userRepository.findById(uploadedByUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate file size (10MB limit)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("File size exceeds 10MB limit");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (!isValidFileType(contentType)) {
            throw new RuntimeException("Invalid file type. Only images and PDFs are allowed");
        }

        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save file to disk
            Path filePath = uploadPath.resolve(uniqueFilename);
            file.transferTo(filePath.toFile());

            // Calculate file checksum
            String checksum = calculateChecksum(file.getBytes());

            // Create attachment record
            Attachment attachment = new Attachment();
            attachment.setExpense(expense);
            attachment.setUploadedBy(uploadedBy);
            attachment.setOriginalFilename(originalFilename);
            attachment.setStoredFilename(uniqueFilename);
            attachment.setFilePath(filePath.toString());
            attachment.setFileSize(file.getSize());
            attachment.setContentType(contentType);
            attachment.setChecksum(checksum);
            attachment.setDescription(description);
            attachment.setStorageProvider("local");
            attachment.setCreatedAt(LocalDateTime.now());

            // Set file type based on content type
            if (contentType.startsWith("image/")) {
                attachment.setAttachmentType(Attachment.AttachmentType.RECEIPT_IMAGE);
            } else if (contentType.equals("application/pdf")) {
                attachment.setAttachmentType(Attachment.AttachmentType.PDF_DOCUMENT);
            } else {
                attachment.setAttachmentType(Attachment.AttachmentType.OTHER);
            }

            Attachment saved = attachmentRepository.save(attachment);

            // Log activity
            activityLogService.logAttachmentUploaded(saved, uploadedBy);

            return saved;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public byte[] downloadFile(Long attachmentId, Long requestingUserId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new RuntimeException("Attachment not found"));
        
        // Validate user has access to this attachment
        validateUserAccess(attachment, requestingUserId);

        try {
            Path filePath = Paths.get(attachment.getFilePath());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file: " + e.getMessage());
        }
    }

    @Override
    public void deleteAttachment(Long attachmentId, Long deletedByUserId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new RuntimeException("Attachment not found"));
        User deletedBy = userRepository.findById(deletedByUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate user has access to delete this attachment
        validateUserAccess(attachment, deletedByUserId);

        // Soft delete
        attachment.setDeletedAt(LocalDateTime.now());
        attachmentRepository.save(attachment);

        // Log activity
        activityLogService.logAttachmentDeleted(attachment, deletedBy);
    }

    @Override
    public List<Attachment> getExpenseAttachments(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new RuntimeException("Expense not found"));
        return attachmentRepository.findActiveByExpense(expense);
    }

    @Override
    public List<Attachment> getUserAttachments(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return attachmentRepository.findActiveByUser(user);
    }

    @Override
    public long getTotalStorageUsedByUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return attachmentRepository.getTotalStorageByUser(user);
    }

    @Override
    public List<Attachment> getAttachmentsByType(Attachment.AttachmentType attachmentType) {
        // For now, return all attachments - can be optimized later
        return attachmentRepository.findAll().stream()
            .filter(a -> a.getAttachmentType() == attachmentType && a.getDeletedAt() == null)
            .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
            .toList();
    }

    @Override
    public Attachment getAttachmentById(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new RuntimeException("Attachment not found"));
    }

    @Override
    public List<Attachment> findDuplicateFiles(String checksum) {
        return attachmentRepository.findDuplicatesByChecksum(checksum);
    }

    @Override
    public void cleanupDeletedFiles(int daysOld) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysOld);
        List<Attachment> attachments = attachmentRepository.findOldDeletedAttachments(threshold);
        
        for (Attachment attachment : attachments) {
            try {
                // Delete physical file
                Path filePath = Paths.get(attachment.getFilePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
                // Delete database record
                attachmentRepository.delete(attachment);
            } catch (IOException e) {
                System.err.println("Failed to delete file: " + attachment.getFilePath());
            }
        }
    }

    private boolean isValidFileType(String contentType) {
        if (contentType == null) return false;
        
        return contentType.startsWith("image/") || 
               contentType.equals("application/pdf") ||
               contentType.equals("text/plain") ||
               contentType.equals("application/msword") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String calculateChecksum(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to calculate checksum");
        }
    }

    private void validateUserAccess(Attachment attachment, Long userId) {
        // User can access attachment if they are a member of the expense's group
        boolean hasAccess = attachment.getExpense().getGroup().getMembers()
            .stream()
            .anyMatch(member -> member.getId().equals(userId));
            
        if (!hasAccess) {
            throw new RuntimeException("Access denied to this attachment");
        }
    }
}