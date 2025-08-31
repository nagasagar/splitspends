import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/config/app_config.dart';
import '../../../../core/logging/app_logger.dart';

class ActivityRepository {
  final Dio _dio;

  ActivityRepository(this._dio);

  Future<List<Map<String, dynamic>>> getUserActivities(String userId, {int limit = 20}) async {
    try {
      final response = await _dio.get(
        '${AppConfig.apiBaseUrl}/activities/user/$userId',
        queryParameters: {'limit': limit},
      );
      final List<dynamic> data = response.data;
      
      return data.map((item) => _mapActivityToModel(item)).toList();
    } catch (e) {
      AppLogger.repositoryError('ActivityRepository', 'getUserActivities', e);
      return [];
    }
  }

  Future<List<Map<String, dynamic>>> getGroupActivities(String groupId, {int limit = 20}) async {
    try {
      final response = await _dio.get(
        '${AppConfig.apiBaseUrl}/activities/group/$groupId',
        queryParameters: {'limit': limit},
      );
      final List<dynamic> data = response.data;
      
      return data.map((item) => _mapActivityToModel(item)).toList();
    } catch (e) {
      AppLogger.repositoryError('ActivityRepository', 'getGroupActivities', e);
      return [];
    }
  }

  Future<List<Map<String, dynamic>>> getRecentActivities({int limit = 20}) async {
    try {
      final response = await _dio.get(
        '${AppConfig.apiBaseUrl}/activities/recent',
        queryParameters: {'limit': limit},
      );
      final List<dynamic> data = response.data;
      
      return data.map((item) => _mapActivityToModel(item)).toList();
    } catch (e) {
      AppLogger.repositoryError('ActivityRepository', 'getRecentActivities', e);
      return [];
    }
  }

  Map<String, dynamic> _mapActivityToModel(Map<String, dynamic> item) {
    final action = item['action'] as String? ?? '';
    final type = _mapActionToType(action);
    
    return {
      'id': item['id'].toString(),
      'type': type,
      'title': _generateTitle(type, item),
      'description': item['description'] as String? ?? '',
      'amount': (item['amount'] as num?)?.toDouble(),
      'timestamp': DateTime.tryParse(item['createdAt'] as String? ?? '') ?? DateTime.now(),
      'userName': item['userName'] as String? ?? 'Unknown User',
      'groupName': item['groupName'] as String?,
      'category': _mapTypeToCategory(type),
      'isUserAction': item['isUserAction'] as bool? ?? false,
      'splitType': _mapSplitType(item['splitType'] as String?),
      'participantCount': item['participantCount'] as int?,
    };
  }

  String _mapActionToType(String action) {
    switch (action.toUpperCase()) {
      case 'CREATE':
        return 'expense_added';
      case 'UPDATE':
        return 'expense_edited';
      case 'DELETE':
        return 'expense_deleted';
      case 'COMPLETE':
      case 'ACCEPT_SETTLEMENT':
        return 'settlement_completed';
      case 'REQUEST_SETTLEMENT':
        return 'settlement_requested';
      case 'REJECT_SETTLEMENT':
        return 'settlement_rejected';
      case 'JOIN_GROUP':
        return 'group_joined';
      case 'LEAVE_GROUP':
        return 'group_left';
      case 'CREATE_GROUP':
        return 'group_created';
      case 'ADD_MEMBER':
        return 'member_added';
      case 'REMOVE_MEMBER':
        return 'member_removed';
      default:
        return 'activity';
    }
  }

  String _generateTitle(String type, Map<String, dynamic> item) {
    switch (type) {
      case 'expense_added':
        return item['description'] as String? ?? 'New Expense';
      case 'expense_edited':
        return 'Expense Updated';
      case 'expense_deleted':
        return 'Expense Deleted';
      case 'settlement_completed':
        return 'Settlement Completed';
      case 'settlement_requested':
        return 'Settlement Requested';
      case 'settlement_rejected':
        return 'Settlement Rejected';
      case 'group_created':
        return 'Group Created';
      case 'group_joined':
        return 'Joined Group';
      case 'group_left':
        return 'Left Group';
      case 'member_added':
        return 'Member Added';
      case 'member_removed':
        return 'Member Removed';
      default:
        return 'Activity';
    }
  }

  String _mapTypeToCategory(String type) {
    switch (type) {
      case 'expense_added':
      case 'expense_edited':
      case 'expense_deleted':
        return 'expense';
      case 'settlement_completed':
      case 'settlement_requested':
      case 'settlement_rejected':
        return 'settlement';
      case 'group_created':
      case 'group_joined':
      case 'group_left':
      case 'member_added':
      case 'member_removed':
        return 'group';
      default:
        return 'general';
    }
  }

  String? _mapSplitType(String? splitType) {
    if (splitType == null) return null;
    
    switch (splitType.toUpperCase()) {
      case 'EQUAL':
        return 'equal';
      case 'PERCENTAGE':
        return 'percentage';
      case 'SHARE':
      case 'EXACT_AMOUNT':
        return 'custom';
      default:
        return null;
    }
  }
}

final activityRepositoryProvider = Provider<ActivityRepository>((ref) {
  final dio = Dio();
  return ActivityRepository(dio);
});
