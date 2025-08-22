import 'dart:convert';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:http/http.dart' as http;

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  return AuthRepository();
});

class AuthRepository {refactor 
  // Replace with your backend API base URL
  static const String _baseUrl = 'http://localhost:8080/api/auth';

  Future<String?> login(String email, String password) async {
    final response = await http.post(
      Uri.parse('$_baseUrl/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'email': email, 'password': password}),
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      // Assuming the backend returns a JSON with a 'token' field
      return data['token'] as String?;
    } else {
      return null;
    }
  }

  // Add signup, logout, and Google SSO methods as needed
}