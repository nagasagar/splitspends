import 'dart:convert';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:http/http.dart' as http;
import 'package:splitspends_flutter/features/auth/data/models/auth_response.dart';
import 'package:splitspends_flutter/features/auth/data/models/user.dart';
import 'package:splitspends_flutter/features/users/data/users_repository.dart';

// Provider for the repository
final authRepositoryProvider = Provider<AuthRepository>((ref) {
  return AuthRepository();
});

class AuthRepository {
  static const String _baseUrl = 'http://localhost:8080/api/auth'; // Adjust as needed

  // LOGIN: Regular email/password
  Future<AuthResponse> login(String email, String password) async {
    final response = await http.post(
      Uri.parse('$_baseUrl/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'email': email, 'password': password}),
    );
    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return AuthResponse.fromJson(data);
    } else {
      final error = response.body.isNotEmpty ? jsonDecode(response.body)['error'] ?? 'Login failed' : 'Login failed';
      return AuthResponse(
        token: '',
        email: '',
        refreshToken: null,
        user: null,
        error: error, //<-- Add this if you define an error field in model
      );
    }
  }

  // LOGIN: Google SSO
  Future<AuthResponse> loginWithGoogle(String idToken) async {
    final response = await http.post(
      Uri.parse('$_baseUrl/google'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'idToken': idToken}),
    );
    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return AuthResponse.fromJson(data);
    } else {
      final error = response.body.isNotEmpty
          ? jsonDecode(response.body)['error'] ?? 'Google Sign-In failed'
          : 'Google Sign-In failed';
      return AuthResponse(
        token: '',
        email: '',
        refreshToken: null,
        user: null,
        error: error, //<-- Add this if you define an error field in model
      );
    }
  }

  // SIGNUP
  Future<AuthResponse> signup(String name, String email, String password) async {
    final response = await http.post(
      Uri.parse('$_baseUrl/signup'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'name': name, 'email': email, 'password': password}),
    );
    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return AuthResponse.fromJson(data);
    } else {
      final error = response.body.isNotEmpty ? jsonDecode(response.body)['error'] ?? 'Signup failed' : 'Signup failed';
      return AuthResponse(
        token: '',
        email: '',
        refreshToken: null,
        user: null,
        error: error, //<-- Add this if you define an error field in model
      );
    }
  }

  // Get current user info using token (expects Authorization: Bearer ...)
  Future<User?> getCurrentUser(String token) async {
    final userId = getUserIdFromToken(token);
    if (userId == null) return null;

    final user = await getUserByEmail(userId, token);
    if (user != null) {
      return user;
    } else {
      return null;
    }
  }

  String? getUserIdFromToken(String token) {
    try {
      final parts = token.split('.');
      if (parts.length != 3) return null;
      final payload = utf8.decode(base64Url.decode(base64Url.normalize(parts[1])));
      final payloadMap = jsonDecode(payload);
      // Return sub as String (email)
      return payloadMap['sub']?.toString();
    } catch (_) {
      return null;
    }
  }

  String? getEmailFromToken(String token) {
    try {
      final parts = token.split('.');
      if (parts.length != 3) return null;
      final payload = utf8.decode(base64Url.decode(base64Url.normalize(parts[1])));
      final payloadMap = jsonDecode(payload);
      return payloadMap['email']?.toString();
    } catch (_) {
      return null;
    }
  }

  Future<User?> getUserByEmail(String email, String token) async {
    final usersRepo = UsersRepository();
    final data = await usersRepo.getUserByEmail(email);
    if (data != null) {
      return User.fromJson(data);
    } else {
      return null;
    }
  }

}
