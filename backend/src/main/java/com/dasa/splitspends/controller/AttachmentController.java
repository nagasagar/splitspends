package com.dasa.splitspends.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dasa.splitspends.dto.AttachmentResponse;
import com.dasa.splitspends.entity.Attachment;
import com.dasa.splitspends.service.AttachmentService;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping(value = "/expense/{expenseId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> uploadFile(
            @PathVariable Long expenseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam Long uploadedByUserId,
            @RequestParam(required = false) String description) {
        Attachment attachment = attachmentService.uploadFile(file, expenseId, uploadedByUserId, description);
        return ResponseEntity.ok(AttachmentResponse.fromEntity(attachment));
    }

    @GetMapping("/{attachmentId}")
    public ResponseEntity<AttachmentResponse> getAttachment(@PathVariable Long attachmentId) {
        Attachment attachment = attachmentService.getAttachmentById(attachmentId);
        return ResponseEntity.ok(AttachmentResponse.fromEntity(attachment));
    }

    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable Long attachmentId,
            @RequestParam Long requestingUserId) {
        Attachment attachment = attachmentService.getAttachmentById(attachmentId);
        byte[] fileData = attachmentService.downloadFile(attachmentId, requestingUserId);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + attachment.getOriginalFilename() + "\"")
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .contentLength(fileData.length)
                .body(fileData);
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long attachmentId,
            @RequestParam Long deletedByUserId) {
        attachmentService.deleteAttachment(attachmentId, deletedByUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/expense/{expenseId}")
    public ResponseEntity<List<AttachmentResponse>> getExpenseAttachments(@PathVariable Long expenseId) {
        List<Attachment> attachments = attachmentService.getExpenseAttachments(expenseId);
        List<AttachmentResponse> response = attachments.stream()
                .map(AttachmentResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AttachmentResponse>> getUserAttachments(@PathVariable Long userId) {
        List<Attachment> attachments = attachmentService.getUserAttachments(userId);
        List<AttachmentResponse> response = attachments.stream()
                .map(AttachmentResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/storage-used")
    public ResponseEntity<Long> getTotalStorageUsedByUser(@PathVariable Long userId) {
        long storage = attachmentService.getTotalStorageUsedByUser(userId);
        return ResponseEntity.ok(storage);
    }

    @GetMapping("/type/{attachmentType}")
    public ResponseEntity<List<AttachmentResponse>> getAttachmentsByType(@PathVariable Attachment.AttachmentType attachmentType) {
        List<Attachment> attachments = attachmentService.getAttachmentsByType(fileType);
        List<AttachmentResponse> response = attachments.stream()
                .map(AttachmentResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/duplicates/{checksum}")
    public ResponseEntity<List<AttachmentResponse>> findDuplicateFiles(@PathVariable String checksum) {
        List<Attachment> attachments = attachmentService.findDuplicateFiles(checksum);
        List<AttachmentResponse> response = attachments.stream()
                .map(AttachmentResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cleanup")
    public ResponseEntity<Void> cleanupDeletedFiles(@RequestParam(defaultValue = "30") int daysOld) {
        attachmentService.cleanupDeletedFiles(daysOld);
        return ResponseEntity.ok().build();
    }
}