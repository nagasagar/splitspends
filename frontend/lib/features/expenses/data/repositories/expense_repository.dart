import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/config/app_config.dart';
import '../../../../core/logging/app_logger.dart';

class ExpenseRepository {
  final Dio _dio;

  ExpenseRepository(this._dio);

  Future<List<Map<String, dynamic>>> getUserExpenses(String userId) async {
    try {
      final response = await _dio.get('${AppConfig.apiBaseUrl}/expenses/user/$userId');
      final List<dynamic> data = response.data;
      
      return data.map((item) => _mapExpenseToModel(item)).toList();
    } catch (e) {
      AppLogger.repositoryError('ExpenseRepository', 'getUserExpenses', e);
      return [];
    }
  }

  Future<List<Map<String, dynamic>>> getGroupExpenses(String groupId, {int page = 0, int size = 20}) async {
    try {
      final response = await _dio.get(
        '${AppConfig.apiBaseUrl}/expenses/group/$groupId',
        queryParameters: {'page': page, 'size': size},
      );
      final List<dynamic> data = response.data;
      
      return data.map((item) => _mapExpenseToModel(item)).toList();
    } catch (e) {
      AppLogger.repositoryError('ExpenseRepository', 'getGroupExpenses', e);
      return [];
    }
  }

  Future<Map<String, dynamic>?> getExpense(String expenseId) async {
    try {
      // Note: The API doesn't have a direct get expense endpoint, 
      // so we'll need to fetch from user expenses or implement it
      // For now, returning null and handling in the UI
      return null;
    } catch (e) {
      AppLogger.repositoryError('ExpenseRepository', 'getExpense', e);
      return null;
    }
  }

  Future<Map<String, dynamic>?> getUserExpenseStats(String userId) async {
    try {
      final response = await _dio.get('${AppConfig.apiBaseUrl}/expenses/user/$userId/stats');
      final data = response.data;
      
      return {
        'totalExpenses': data['totalExpenses'] as int? ?? 0,
        'totalAmount': (data['totalAmount'] as num?)?.toDouble() ?? 0.0,
        'totalPaid': (data['totalPaid'] as num?)?.toDouble() ?? 0.0,
        'totalOwed': (data['totalOwed'] as num?)?.toDouble() ?? 0.0,
        'monthlyAverage': (data['monthlyAverage'] as num?)?.toDouble() ?? 0.0,
        'categoryBreakdown': data['categoryBreakdown'] as Map<String, dynamic>? ?? {},
      };
    } catch (e) {
      AppLogger.repositoryError('ExpenseRepository', 'getUserExpenseStats', e);
      return null;
    }
  }

  Map<String, dynamic> _mapExpenseToModel(Map<String, dynamic> item) {
    return {
      'id': item['id'].toString(),
      'description': item['description'] as String? ?? '',
      'amount': (item['amount'] as num?)?.toDouble() ?? 0.0,
      'category': _mapCategory(item['category'] as String?),
      'date': _formatDate(item['date'] as String?),
      'paidBy': item['paidByName'] as String? ?? 'Unknown',
      'groupName': item['groupName'] as String?,
      'userShare': (item['userShare'] as num?)?.toDouble() ?? 0.0,
      'isPaidByUser': item['isPaidByUser'] as bool? ?? false,
      'splitType': _mapSplitType(item['splitType'] as String?),
      'participantCount': item['participantCount'] as int? ?? 0,
      'currency': item['currency'] as String? ?? 'INR',
      'notes': item['notes'] as String?,
      'attachments': item['attachments'] as List<dynamic>? ?? [],
      'createdAt': item['createdAt'] as String?,
      'updatedAt': item['updatedAt'] as String?,
    };
  }

  String _mapCategory(String? category) {
    if (category == null) return 'Other';
    
    switch (category.toUpperCase()) {
      case 'FOOD_DRINKS':
        return 'Food';
      case 'TRANSPORTATION':
        return 'Transport';
      case 'ACCOMMODATION':
        return 'Accommodation';
      case 'ENTERTAINMENT':
        return 'Entertainment';
      case 'SHOPPING':
        return 'Shopping';
      case 'UTILITIES':
        return 'Utilities';
      case 'HEALTH_FITNESS':
        return 'Health';
      case 'EDUCATION':
        return 'Education';
      case 'PERSONAL_CARE':
        return 'Personal Care';
      case 'GIFTS_DONATIONS':
        return 'Gifts';
      case 'SERVICES':
        return 'Services';
      case 'TAXES':
        return 'Taxes';
      case 'INSURANCE':
        return 'Insurance';
      case 'DEBT_PAYMENTS':
        return 'Debt';
      case 'INVESTMENTS':
        return 'Investment';
      case 'MISCELLANEOUS':
        return 'Other';
      default:
        return 'Other';
    }
  }

  String _mapSplitType(String? splitType) {
    if (splitType == null) return 'equal';
    
    switch (splitType.toUpperCase()) {
      case 'EQUAL':
        return 'equal';
      case 'PERCENTAGE':
        return 'percentage';
      case 'SHARE':
        return 'custom';
      case 'EXACT_AMOUNT':
        return 'custom';
      default:
        return 'equal';
    }
  }

  String _formatDate(String? dateString) {
    if (dateString == null) return 'Unknown';
    
    try {
      final date = DateTime.parse(dateString);
      final now = DateTime.now();
      final difference = now.difference(date);
      
      if (difference.inDays == 0) {
        return 'Today';
      } else if (difference.inDays == 1) {
        return 'Yesterday';
      } else if (difference.inDays < 7) {
        return '${difference.inDays} days ago';
      } else {
        return '${date.day}/${date.month}/${date.year}';
      }
    } catch (e) {
      return 'Unknown';
    }
  }
}

final expenseRepositoryProvider = Provider<ExpenseRepository>((ref) {
  final dio = Dio();
  return ExpenseRepository(dio);
});
