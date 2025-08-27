package com.dasa.splitspends.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dasa.splitspends.dto.activity.ActivityLogResponse;
import com.dasa.splitspends.entity.ActivityLog;
import com.dasa.splitspends.service.ActivityLogService;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityLogService activityLogService;

    public ActivityController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ActivityLogResponse>> getUserActivities(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "20") int limit) {

        PageRequest pageRequest = PageRequest.of(0, limit,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ActivityLog> activities = activityLogService.getUserActivities(userId, pageRequest);

        List<ActivityLogResponse> response = activities.getContent().stream()
                .map(ActivityLogResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<ActivityLogResponse>> getGroupActivities(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "20") int limit) {

        PageRequest pageRequest = PageRequest.of(0, limit,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ActivityLog> activities = activityLogService.getGroupActivities(groupId, pageRequest);

        List<ActivityLogResponse> response = activities.getContent().stream()
                .map(ActivityLogResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ActivityLogResponse>> getRecentActivities(
            @RequestParam(defaultValue = "50") int limit) {

        PageRequest pageRequest = PageRequest.of(0, limit,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ActivityLog> activities = activityLogService.getRecentActivities(pageRequest);

        List<ActivityLogResponse> response = activities.getContent().stream()
                .map(ActivityLogResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }
}
