package com.dasa.splitspends.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dasa.splitspends.dto.dashboard.DashboardSummaryResponse;
import com.dasa.splitspends.service.DashboardService;

/**
 * Dashboard Controller providing both aggregated and granular endpoints.
 * 
 * Design Philosophy:
 * - /summary/{userId} - Aggregated endpoint for mobile/web dashboards
 * (performance optimized)
 * - /stats/{userId} - Individual endpoints for flexible UI composition
 * - Query parameters for customization where needed
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /**
     * Aggregated dashboard endpoint - optimal for performance-critical clients
     * Use this when you need all dashboard data in a single request
     */
    @GetMapping("/summary/{userId}")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(@PathVariable Long userId) {
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary(userId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Flexible dashboard endpoint with selective data loading
     * Use query parameters to customize what data to include
     * 
     * Example: /dashboard/flexible/1?include=stats,groups,activities&limit=5
     */
    @GetMapping("/flexible/{userId}")
    public ResponseEntity<Map<String, Object>> getFlexibleDashboard(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "stats,groups,expenses,settlements,activities") String include,
            @RequestParam(required = false, defaultValue = "10") int limit) {

        Map<String, Object> response = dashboardService.getFlexibleDashboard(userId, include, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Individual granular endpoints for maximum flexibility
     */

    @GetMapping("/stats/{userId}")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable Long userId) {
        Map<String, Object> stats = dashboardService.getUserStats(userId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/groups/{userId}")
    public ResponseEntity<Map<String, Object>> getUserGroups(@PathVariable Long userId) {
        Map<String, Object> groups = dashboardService.getUserGroups(userId);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/recent-expenses/{userId}")
    public ResponseEntity<Map<String, Object>> getRecentExpenses(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "10") int limit) {
        Map<String, Object> expenses = dashboardService.getRecentExpenses(userId, limit);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/pending-settlements/{userId}")
    public ResponseEntity<Map<String, Object>> getPendingSettlements(@PathVariable Long userId) {
        Map<String, Object> settlements = dashboardService.getPendingSettlements(userId);
        return ResponseEntity.ok(settlements);
    }
}
