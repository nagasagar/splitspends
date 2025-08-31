import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/config/app_config.dart';
import '../../../../core/logging/app_logger.dart';

class GroupRepository {
  final Dio _dio;

  GroupRepository(this._dio);

  Future<List<Map<String, dynamic>>> getUserGroups(String userId) async {
    try {
      final response = await _dio.get('${AppConfig.apiBaseUrl}/groups/user/$userId');
      final List<dynamic> data = response.data;
      
      return data.map((item) => {
        'id': item['id'].toString(),
        'name': item['name'] as String? ?? '',
        'description': item['description'] as String? ?? '',
        'memberCount': item['memberCount'] as int? ?? 0,
        'totalExpenses': (item['totalExpenses'] as num?)?.toDouble() ?? 0.0,
        'userBalance': (item['userBalance'] as num?)?.toDouble() ?? 0.0,
        'lastActivity': _formatDate(item['updatedAt'] as String?),
        'isActive': item['status'] == 'ACTIVE',
        'imageUrl': item['groupImageUrl'] as String?,
        'category': _mapCategoryFromName(item['name'] as String?),
      }).toList();
    } catch (e) {
      AppLogger.repositoryError('GroupRepository', 'getUserGroups', e);
      return [];
    }
  }

  Future<Map<String, dynamic>?> getGroup(String groupId) async {
    try {
      final response = await _dio.get('${AppConfig.apiBaseUrl}/groups/$groupId');
      final data = response.data;
      
      return {
        'id': data['id'].toString(),
        'name': data['name'] as String? ?? '',
        'description': data['description'] as String? ?? '',
        'memberCount': data['memberCount'] as int? ?? 0,
        'totalExpenses': (data['totalExpenses'] as num?)?.toDouble() ?? 0.0,
        'userBalance': (data['userBalance'] as num?)?.toDouble() ?? 0.0,
        'lastActivity': _formatDate(data['updatedAt'] as String?),
        'isActive': data['status'] == 'ACTIVE',
        'imageUrl': data['groupImageUrl'] as String?,
        'category': _mapCategoryFromName(data['name'] as String?),
        'currency': data['defaultCurrency'] as String? ?? 'INR',
        'createdBy': data['createdBy'] as String?,
        'createdAt': _formatDate(data['createdAt'] as String?),
      };
    } catch (e) {
      AppLogger.repositoryError('GroupRepository', 'getGroup', e);
      return null;
    }
  }

  Future<List<Map<String, dynamic>>> getGroupMembers(String groupId) async {
    try {
      final response = await _dio.get('${AppConfig.apiBaseUrl}/groups/$groupId/members');
      final List<dynamic> data = response.data;
      
      return data.map((item) => {
        'id': item['id'].toString(),
        'name': item['name'] as String? ?? '',
        'email': item['email'] as String? ?? '',
        'isAdmin': item['isAdmin'] as bool? ?? false,
        'joinedAt': _formatDate(item['joinedAt'] as String?),
        'profilePictureUrl': item['profilePictureUrl'] as String?,
      }).toList();
    } catch (e) {
      AppLogger.repositoryError('GroupRepository', 'getGroupMembers', e);
      return [];
    }
  }

  Future<List<Map<String, dynamic>>> getGroupBalances(String groupId) async {
    try {
      final response = await _dio.get('${AppConfig.apiBaseUrl}/groups/$groupId/balances');
      final List<dynamic> data = response.data;
      
      return data.map((item) => {
        'userId': item['userId'].toString(),
        'userName': item['userName'] as String? ?? '',
        'balance': (item['balance'] as num?)?.toDouble() ?? 0.0,
        'totalPaid': (item['totalPaid'] as num?)?.toDouble() ?? 0.0,
        'totalOwed': (item['totalOwed'] as num?)?.toDouble() ?? 0.0,
      }).toList();
    } catch (e) {
      AppLogger.repositoryError('GroupRepository', 'getGroupBalances', e);
      return [];
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
      } else if (difference.inDays < 30) {
        final weeks = (difference.inDays / 7).floor();
        return weeks == 1 ? '1 week ago' : '$weeks weeks ago';
      } else {
        final months = (difference.inDays / 30).floor();
        return months == 1 ? '1 month ago' : '$months months ago';
      }
    } catch (e) {
      return 'Unknown';
    }
  }

  String _mapCategoryFromName(String? name) {
    if (name == null) return 'General';
    
    final lowerName = name.toLowerCase();
    if (lowerName.contains('trip') || lowerName.contains('vacation') || lowerName.contains('travel')) {
      return 'Travel';
    } else if (lowerName.contains('lunch') || lowerName.contains('dinner') || lowerName.contains('food')) {
      return 'Food';
    } else if (lowerName.contains('rent') || lowerName.contains('apartment') || lowerName.contains('house')) {
      return 'Housing';
    } else if (lowerName.contains('office') || lowerName.contains('work')) {
      return 'Work';
    } else if (lowerName.contains('movie') || lowerName.contains('entertainment')) {
      return 'Entertainment';
    } else {
      return 'General';
    }
  }
}

final groupRepositoryProvider = Provider<GroupRepository>((ref) {
  final dio = Dio();
  return GroupRepository(dio);
});
