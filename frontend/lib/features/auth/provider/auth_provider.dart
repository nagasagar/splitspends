import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/auth_repository.dart';

final authProvider = StateNotifierProvider<AuthNotifier, AuthState>(
  (ref) => AuthNotifier(ref.read(authRepositoryProvider)),
);

class AuthState {
  final bool isAuthenticated;
  final String? error;

  AuthState({this.isAuthenticated = false, this.error});

  AuthState copyWith({bool? isAuthenticated, String? error}) {
    return AuthState(
      isAuthenticated: isAuthenticated ?? this.isAuthenticated,
      error: error,
    );
  }
}

class AuthNotifier extends StateNotifier<AuthState> {
  final AuthRepository _authRepository;

  AuthNotifier(this._authRepository) : super(AuthState());

  Future<AuthResult> login(String email, String password) async {
    try {
      final token = await _authRepository.login(email, password);
      if (token != null) {
        state = state.copyWith(isAuthenticated: true, error: null);
        return AuthResult(success: true);
      } else {
        state = state.copyWith(error: 'Invalid credentials');
        return AuthResult(success: false, error: 'Invalid credentials');
      }
    } catch (e) {
      state = state.copyWith(error: e.toString());
      return AuthResult(success: false, error: e.toString());
    }
  }

  Future<void> loginWithGoogle(BuildContext context) async {
    // TODO: Implement Google SSO logic
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Google SSO not implemented yet')),
    );
  }
}

class AuthResult {
  final bool success;
  final String? error;

  AuthResult({required this.success, this.error});
}