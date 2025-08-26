import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../models/dashboard_models.dart';

class DashboardRepository {
  final Dio _dio;
  static const String baseUrl = 'http://localhost:8080';

  DashboardRepository(this._dio);

  Future<DashboardData> getDashboardData(String userId) async {
    try {
      final response = await _dio.get('$baseUrl/api/dashboard/data/$userId');
      return DashboardData.fromJson(response.data);
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
      final response = await _dio.get('$baseUrl/api/dashboard/summary/$userId');
      return DashboardSummary.fromJson(response.data);
    } catch (e) {
      // Return mock data for now
      return const DashboardSummary(
        totalOwed: 150.75,
        totalToReceive: 45.50,
        groupCount: 3,
        expenseCount: 12,
        monthlySpending: 890.25,
      );
    }
  }

  Future<List<RecentActivity>> getRecentActivities(String userId, {int limit = 10}) async {
    try {
      final response = await _dio.get(
        '$baseUrl/api/dashboard/activities/$userId',
        queryParameters: {'limit': limit},
      );
      final List<dynamic> data = response.data;
      return data.map((item) => RecentActivity.fromJson(item)).toList();
    } catch (e) {
      // Return mock data for now
      return [
        RecentActivity(
          id: '1',
          type: 'expense_added',
          description: 'Dinner at Pizza Palace',
          timestamp: DateTime.now().subtract(const Duration(hours: 2)),
          amount: 45.50,
          groupName: 'Friends',
          currency: 'USD',
        ),
        RecentActivity(
          id: '2',
          type: 'settlement_completed',
          description: 'You settled up with John',
          timestamp: DateTime.now().subtract(const Duration(days: 1)),
          amount: 25.00,
          groupName: 'Work',
          currency: 'USD',
        ),
        RecentActivity(
          id: '3',
          type: 'expense_added',
          description: 'Grocery shopping',
          timestamp: DateTime.now().subtract(const Duration(days: 2)),
          amount: 85.30,
          groupName: 'Roommates',
          currency: 'USD',
        ),
      ];
    }
  }

  Future<List<GroupSummary>> getUserGroups(String userId) async {
    try {
      final response = await _dio.get('$baseUrl/api/dashboard/groups/$userId');
      final List<dynamic> data = response.data;
      return data.map((item) => GroupSummary.fromJson(item)).toList();
    } catch (e) {
      // Return mock data for now
      return [
        GroupSummary(
          id: '1',
          name: 'Friends',
          description: 'Weekend hangouts',
          memberCount: 4,
          userBalance: -25.50,
          lastActivity: DateTime.now().subtract(const Duration(hours: 2)),
        ),
        GroupSummary(
          id: '2',
          name: 'Work Team',
          description: 'Office expenses',
          memberCount: 6,
          userBalance: 15.75,
          lastActivity: DateTime.now().subtract(const Duration(days: 1)),
        ),
        GroupSummary(
          id: '3',
          name: 'Roommates',
          description: 'Shared apartment costs',
          memberCount: 3,
          userBalance: -45.25,
          lastActivity: DateTime.now().subtract(const Duration(days: 2)),
        ),
      ];
    }
  }
}

final dashboardRepositoryProvider = Provider<DashboardRepository>((ref) {
  final dio = Dio();
  return DashboardRepository(dio);
});
