import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:splitspends_flutter/core/config/app_config.dart';

class UsersRepository {
  // Existing method
  Future<Map<String, dynamic>> getUserDetails(String userId, {String? token}) async {
    final headers = <String, String>{
      'Content-Type': 'application/json',
    };

    if (token != null) {
      headers['Authorization'] = 'Bearer $token';
    }

    final res = await http.get(
      Uri.parse('${AppConfig.apiBaseUrl}/api/users/$userId'),
      headers: headers,
    );
    if (res.statusCode == 200) {
      return jsonDecode(res.body);
    } else {
      throw Exception('Failed to load user details');
    }
  }

  // New method: fetch user by email
  Future<Map<String, dynamic>?> getUserByEmail(String email, {String? token}) async {
    try {
      final uri = Uri.parse('${AppConfig.apiBaseUrl}/api/users/email/$email');
      final headers = <String, String>{
        'Content-Type': 'application/json',
      };

      if (token != null) {
        headers['Authorization'] = 'Bearer $token';
      }

      final res = await http.get(uri, headers: headers);
      if (res.statusCode == 200) {
        return jsonDecode(res.body) as Map<String, dynamic>;
      } else if (res.statusCode == 404) {
        // User not found
        return null;
      } else if (res.statusCode == 401) {
        throw Exception('Unauthorized: Please log in again');
      } else {
        throw Exception('Failed to fetch user by email: ${res.statusCode} ${res.reasonPhrase}');
      }
    } catch (e) {
      throw Exception('Failed to fetch user by email: $e');
    }
  }
}
