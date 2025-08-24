import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:splitspends_flutter/shared/app_config.dart';

class UsersRepository {
  // Existing method
  Future<Map<String, dynamic>> getUserDetails(String userId) async {
    final res = await http.get(Uri.parse('${AppConfig.apiBaseUrl}/api/users/$userId'));
    if (res.statusCode == 200) {
      return jsonDecode(res.body);
    } else {
      throw Exception('Failed to load user details');
    }
  }

  // New method: fetch user by email
  Future<Map<String, dynamic>?> getUserByEmail(String email) async {
    final uri = Uri.parse('${AppConfig.apiBaseUrl}/api/users/email/$email');
    final res = await http.get(uri);
    if (res.statusCode == 200) {
      return jsonDecode(res.body) as Map<String, dynamic>;
    } else if (res.statusCode == 404) {
      // User not found
      return null;
    } else {
      throw Exception('Failed to fetch user by email');
    }
  }
}
