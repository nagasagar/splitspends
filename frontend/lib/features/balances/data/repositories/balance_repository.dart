import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/config/app_config.dart';
import '../../../../core/logging/app_logger.dart';

class BalanceRepository {
  final Dio _dio;

  BalanceRepository(this._dio);

  Future<List<Map<String, dynamic>>> getUserBalances(String userId) async {
    try {
      // Get user's settlements to calculate who owes what
      final pendingResponse = await _dio.get('${AppConfig.apiBaseUrl}/settlements/user/$userId/pending');
      final List<dynamic> pendingData = pendingResponse.data;
      
      // Group balances by user and calculate net amounts
      final Map<String, Map<String, dynamic>> balanceMap = {};
      
      for (final settlement in pendingData) {
        final payerId = settlement['payerId'].toString();
        final payeeId = settlement['payeeId'].toString();
        final amount = (settlement['amount'] as num?)?.toDouble() ?? 0.0;
        
        // If current user is the payer (they owe money)
        if (payerId == userId) {
          final payeeKey = payeeId;
          if (!balanceMap.containsKey(payeeKey)) {
            balanceMap[payeeKey] = {
              'userId': payeeId,
              'userName': settlement['payeeName'] as String? ?? 'Unknown',
              'userEmail': settlement['payeeEmail'] as String? ?? '',
              'amount': 0.0,
              'expenseCount': 0,
              'lastExpense': '',
              'groupName': settlement['groupName'] as String?,
            };
          }
          balanceMap[payeeKey]!['amount'] = 
              (balanceMap[payeeKey]!['amount'] as double) + amount;
          balanceMap[payeeKey]!['expenseCount'] = 
              (balanceMap[payeeKey]!['expenseCount'] as int) + 1;
          balanceMap[payeeKey]!['lastExpense'] = settlement['description'] as String? ?? '';
        }
        
        // If current user is the payee (they are owed money)
        if (payeeId == userId) {
          final payerKey = payerId;
          if (!balanceMap.containsKey(payerKey)) {
            balanceMap[payerKey] = {
              'userId': payerId,
              'userName': settlement['payerName'] as String? ?? 'Unknown',
              'userEmail': settlement['payerEmail'] as String? ?? '',
              'amount': 0.0,
              'expenseCount': 0,
              'lastExpense': '',
              'groupName': settlement['groupName'] as String?,
            };
          }
          balanceMap[payerKey]!['amount'] = 
              (balanceMap[payerKey]!['amount'] as double) - amount;
          balanceMap[payerKey]!['expenseCount'] = 
              (balanceMap[payerKey]!['expenseCount'] as int) + 1;
          balanceMap[payerKey]!['lastExpense'] = settlement['description'] as String? ?? '';
        }
      }
      
      return balanceMap.values.toList();
    } catch (e) {
      AppLogger.repositoryError('BalanceRepository', 'getUserBalances', e);
      return [];
    }
  }

  Future<List<Map<String, dynamic>>> getWhoOwesYou(String userId) async {
    try {
      final balances = await getUserBalances(userId);
      return balances.where((balance) => (balance['amount'] as double) < 0).map((balance) => {
        ...balance,
        'amount': -(balance['amount'] as double), // Make positive for display
      }).toList();
    } catch (e) {
      AppLogger.repositoryError('BalanceRepository', 'getWhoOwesYou', e);
      return [];
    }
  }

  Future<List<Map<String, dynamic>>> getWhoYouOwe(String userId) async {
    try {
      final balances = await getUserBalances(userId);
      return balances.where((balance) => (balance['amount'] as double) > 0).toList();
    } catch (e) {
      AppLogger.repositoryError('BalanceRepository', 'getWhoYouOwe', e);
      return [];
    }
  }

  Future<Map<String, dynamic>> getBalanceSummary(String userId) async {
    try {
      final pendingAmountResponse = await _dio.get('${AppConfig.apiBaseUrl}/settlements/user/$userId/pending-amount');
      final data = pendingAmountResponse.data;
      
      return {
        'totalOwed': (data['totalOwed'] as num?)?.toDouble() ?? 0.0,
        'totalToReceive': (data['totalToReceive'] as num?)?.toDouble() ?? 0.0,
        'netBalance': (data['netBalance'] as num?)?.toDouble() ?? 0.0,
      };
    } catch (e) {
      AppLogger.repositoryError('BalanceRepository', 'getBalanceSummary', e);
      return {
        'totalOwed': 0.0,
        'totalToReceive': 0.0,
        'netBalance': 0.0,
      };
    }
  }

  Future<List<Map<String, dynamic>>> getGroupBalances(String groupId) async {
    try {
      final response = await _dio.get('${AppConfig.apiBaseUrl}/groups/$groupId/balances');
      final List<dynamic> data = response.data;
      
      return data.map((item) => {
        'userId': item['userId'].toString(),
        'userName': item['userName'] as String? ?? 'Unknown',
        'balance': (item['balance'] as num?)?.toDouble() ?? 0.0,
        'totalPaid': (item['totalPaid'] as num?)?.toDouble() ?? 0.0,
        'totalOwed': (item['totalOwed'] as num?)?.toDouble() ?? 0.0,
      }).toList();
    } catch (e) {
      AppLogger.repositoryError('BalanceRepository', 'getGroupBalances', e);
      return [];
    }
  }
}
final balanceRepositoryProvider = Provider<BalanceRepository>((ref) {
  final dio = Dio();
  return BalanceRepository(dio);
});
