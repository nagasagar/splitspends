import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:splitspends_flutter/shared/app_config.dart';

class ExpensesRepository {
  Future<Map<String, dynamic>> getUserExpenseStats(String userId) async {
    final res = await http.get(Uri.parse('${AppConfig.apiBaseUrl}/api/expenses/user/$userId/stats'));
    if (res.statusCode == 200) {
      return jsonDecode(res.body);
    } else {
      throw Exception('Failed to load expense stats');
    }
  }
}