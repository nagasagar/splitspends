import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:splitspends_flutter/features/auth/data/auth_repository.dart';
import 'package:splitspends_flutter/features/auth/models/auth_models.dart';
import 'package:splitspends_flutter/shared/utils/auth_token_storage.dart';

final authProvider = StateNotifierProvider<AuthNotifier, AuthState>(
  (ref) => AuthNotifier(ref.read(authRepositoryProvider)),
);

class AuthState {
  final bool isAuthenticated;
  final String? token;
  final User? user;
  final String? error;

  AuthState({
    this.isAuthenticated = false,
    this.token,
    this.user,
    this.error,
  });

  AuthState copyWith({
    bool? isAuthenticated,
    String? token,
    User? user,
    String? error,
  }) {
    return AuthState(
      isAuthenticated: isAuthenticated ?? this.isAuthenticated,
      token: token ?? this.token,
      user: user ?? this.user,
      error: error ?? this.error,
    );
  }

  // Named constructor for unauthenticated
  factory AuthState.unauthenticated() => AuthState(isAuthenticated: false);

  // Named constructor for authenticated
  factory AuthState.authenticated({
    required String token,
    required User user,
  }) {
    return AuthState(
      isAuthenticated: true,
      token: token,
      user: user,
      error: null,
    );
  }
}

class AuthNotifier extends StateNotifier<AuthState> {
  final AuthRepository _authRepository;

  AuthNotifier(this._authRepository) : super(AuthState.unauthenticated()) {
    _loadFromStorage();
  }

  Future<void> _loadFromStorage() async {
    final token = await AuthTokenStorage.readToken();
    if (token != null && token.isNotEmpty) {
      final user = await _authRepository.getCurrentUser(token);
      if (user != null) {
        state = AuthState.authenticated(token: token, user: user);
      } else {
        state = AuthState.unauthenticated();
      }
    } else {
      state = AuthState.unauthenticated();
    }
  }

  Future<AuthResult> login(String email, String password) async {
    try {
      final resp = await _authRepository.login(email, password);
      if (resp.token.isNotEmpty) {
        await AuthTokenStorage.saveToken(resp.token);
        final user = await _authRepository.getCurrentUser(resp.token);
        if (user != null) {
          state = AuthState.authenticated(token: resp.token, user: user);
          return AuthResult(success: true);
        } else {
          state = AuthState.unauthenticated().copyWith(error: "Failed to fetch user info.");
          return AuthResult(success: false, error: "Failed to fetch user info.");
        }
      } else {
        state = AuthState.unauthenticated().copyWith(error: resp.error ?? 'Invalid credentials');
        return AuthResult(success: false, error: resp.error ?? 'Invalid credentials');
      }
    } catch (e) {
      state = AuthState.unauthenticated().copyWith(error: e.toString());
      return AuthResult(success: false, error: e.toString());
    }
  }

  Future<bool> signup(String name, String email, String password) async {
    try {
      final resp = await _authRepository.signup(name, email, password);
      if (resp.token.isNotEmpty) {
        await AuthTokenStorage.saveToken(resp.token);
        // Fetch user object using the received token.
        final user = await _authRepository.getCurrentUser(resp.token);
        if (user != null) {
          state = AuthState.authenticated(token: resp.token, user: user);
          return true;
        } else {
          state = AuthState.unauthenticated().copyWith(error: "Failed to fetch user info.");
          return false;
        }
      }
      state = AuthState.unauthenticated().copyWith(error: resp.error);
      return false;
    } catch (e) {
      state = AuthState.unauthenticated().copyWith(error: e.toString());
      return false;
    }
  }

  Future<void> logout() async {
    await AuthTokenStorage.deleteToken();
    state = AuthState.unauthenticated();
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
