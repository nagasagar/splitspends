import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/config/app_config.dart';
import '../../../../core/logging/app_logger.dart';
import '../models/dashboard_models.dart';

class DashboardRepository {
  final Dio _dio;

  DashboardRepository(this._dio);

  Future<DashboardData> getDashboardData(String userId) async {
    try {
      // Use the flexible dashboard endpoint which combines multiple data sources
      final response = await _dio.get(
        '${AppConfig.apiBaseUrl}/dashboard/flexible/$userId',
        queryParameters: {
          'include': 'stats,groups,expenses,settlements,activities',
          'limit': 10,
        },
      );

      final data = response.data;

      // Extract dashboard summary from the flexible response
      final userSummary = data['userSummary'] ?? {};
      final financialSummary = data['financialSummary'] ?? {};

      final summary = DashboardSummary(
        totalOwed: (financialSummary['totalOwed'] as num?)?.toDouble() ?? 0.0,
        totalToReceive: (financialSummary['totalOwing'] as num?)?.toDouble() ?? 0.0,
        groupCount: userSummary['totalGroups'] as int? ?? 0,
        expenseCount: userSummary['totalExpenses'] as int? ?? 0,
        monthlySpending: (financialSummary['totalPaid'] as num?)?.toDouble() ?? 0.0,
      );

      // Extract recent activities
      final activitiesData = data['recentActivities'] as List<dynamic>? ?? [];
      final activities = activitiesData
          .map((item) => RecentActivity(
                id: item['id'].toString(),
                type: _mapActionToType(item['action'] ?? 'activity'),
                description: item['description'] as String? ?? '',
                timestamp: DateTime.tryParse(item['createdAt'] as String? ?? '') ?? DateTime.now(),
                amount: (item['amount'] as num?)?.toDouble(),
                groupName: item['groupName'] as String?,
                currency: item['currency'] as String?,
              ))
          .toList();

      // Extract groups
      final groupsData = data['userGroups'] as List<dynamic>? ?? [];
      final groups = groupsData
          .map((item) => GroupSummary(
                id: item['id'].toString(),
                name: item['name'] as String? ?? '',
                description: item['description'] as String?,
                memberCount: item['memberCount'] as int? ?? 0,
                userBalance: (item['userBalance'] as num?)?.toDouble() ?? 0.0,
                groupImageUrl: item['groupImageUrl'] as String?,
                lastActivity: DateTime.tryParse(item['updatedAt'] as String? ?? '') ?? DateTime.now(),
              ))
          .toList();

      return DashboardData(
        summary: summary,
        recentActivities: activities,
        groups: groups,
      );
    } catch (e) {
      // Fallback: get data separately if combined endpoint fails
      final summary = await getDashboardSummary(userId);
      final activities = await getRecentActivities(userId);
      final groups = await getUserGroups(userId);
      
      return DashboardData(
        summary: summary,
        recentActivities: activities,
        groups: groups,
      );
    }
  }

  Future<DashboardSummary> getDashboardSummary(String userId) async {
    try {
      final response = await _dio.get('${AppConfig.apiBaseUrl}/dashboard/summary/$userId');
      final data = response.data;

      // Map backend response to frontend model
      final userSummary = data['userSummary'] ?? {};
      final financialSummary = data['financialSummary'] ?? {};

      return DashboardSummary(
        totalOwed: (financialSummary['totalOwed'] as num?)?.toDouble() ?? 0.0,
        totalToReceive: (financialSummary['totalOwing'] as num?)?.toDouble() ?? 0.0,
        groupCount: userSummary['totalGroups'] as int? ?? 0,
        expenseCount: userSummary['totalExpenses'] as int? ?? 0,
        monthlySpending: (financialSummary['totalPaid'] as num?)?.toDouble() ?? 0.0,
      );
    } catch (e) {
      AppLogger.repositoryError('DashboardRepository', 'getDashboardSummary', e);
      // Return empty data instead of mock data on error
      return const DashboardSummary(
        totalOwed: 0.0,
        totalToReceive: 0.0,
        groupCount: 0,
        expenseCount: 0,
        monthlySpending: 0.0,
      );
    }
  }

  Future<List<RecentActivity>> getRecentActivities(String userId, {int limit = 10}) async {
    try {
      final response = await _dio.get(
        '${AppConfig.apiBaseUrl}/activities/user/$userId',
        queryParameters: {'limit': limit},
      );
      final List<dynamic> data = response.data;
      
      return data
          .map((item) => RecentActivity(
                id: item['id'].toString(),
                type: _mapActionToType(item['action'] ?? 'activity'),
                description: item['description'] as String? ?? '',
                timestamp: DateTime.tryParse(item['createdAt'] as String? ?? '') ?? DateTime.now(),
                amount: (item['amount'] as num?)?.toDouble(),
                groupName: item['groupName'] as String?,
                currency: item['currency'] as String?,
              ))
          .toList();
    } catch (e) {
      AppLogger.repositoryError('DashboardRepository', 'getRecentActivities', e);
      // Return empty list instead of mock data
      return [];
    }
  }

  String _mapActionToType(String action) {
    switch (action) {
      case 'CREATE':
        return 'expense_added';
      case 'COMPLETE':
        return 'settlement_completed';
      case 'REQUEST_SETTLEMENT':
        return 'settlement_requested';
      case 'ACCEPT_SETTLEMENT':
        return 'settlement_completed';
      case 'REJECT_SETTLEMENT':
        return 'settlement_rejected';
      case 'UPDATE':
        return 'expense_updated';
      case 'DELETE':
        return 'expense_deleted';
      case 'JOIN_GROUP':
        return 'group_joined';
      case 'LEAVE_GROUP':
        return 'group_left';
      default:
        return 'activity';
    }
  }

  Future<List<GroupSummary>> getUserGroups(String userId) async {
    try {
      final response = await _dio.get('${AppConfig.apiBaseUrl}/groups/user/$userId');
      final List<dynamic> data = response.data;
      
      return data
          .map((item) => GroupSummary(
                id: item['id'].toString(),
                name: item['name'] as String? ?? '',
                description: item['description'] as String?,
                memberCount: item['memberCount'] as int? ?? 0,
                userBalance: (item['userBalance'] as num?)?.toDouble() ?? 0.0,
                groupImageUrl: item['groupImageUrl'] as String?,
                lastActivity: DateTime.tryParse(item['updatedAt'] as String? ?? '') ?? DateTime.now(),
              ))
          .toList();
    } catch (e) {
      AppLogger.repositoryError('DashboardRepository', 'getUserGroups', e);
      // Return empty list instead of mock data
      return [];
    }
  }
}

final dashboardRepositoryProvider = Provider<DashboardRepository>((ref) {
  final dio = Dio();
  return DashboardRepository(dio);
});
