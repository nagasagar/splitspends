import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:splitspends_flutter/features/auth/data/auth_repository.dart';
import 'package:splitspends_flutter/features/auth/data/models/auth_response.dart';
import 'package:splitspends_flutter/features/auth/data/models/user.dart';
import 'package:splitspends_flutter/shared/utils/auth_token_storage.dart'; // <-- use your helper

class AuthState {
  final bool isAuthenticated;
  final String? token;
  final User? user;
  final String? error;

  AuthState({
    required this.isAuthenticated,
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
      error: error,
    );
  }

  factory AuthState.unauthenticated() => AuthState(isAuthenticated: false);
  factory AuthState.authenticated({required String token, required User user}) =>
      AuthState(isAuthenticated: true, token: token, user: user);
}

class AuthNotifier extends StateNotifier<AuthState> {
  final AuthRepository _authRepository;

  AuthNotifier(this._authRepository) : super(AuthState.unauthenticated()) {
    _loadFromStorage();
  }

  Future<void> _loadFromStorage() async {
    final token = await AuthTokenStorage.readToken();
    if (token != null) {
      final user = await _authRepository.getCurrentUser(token);
      if (user != null) {
        state = AuthState.authenticated(token: token, user: user);
      } else {
        state = AuthState.unauthenticated();
      }
    }
  }

  Future<bool> login(String email, String password) async {
    try {
      final AuthResponse resp = await _authRepository.login(email, password);
      // ignore: unnecessary_null_comparison
      if (resp.token != null) {
        await AuthTokenStorage.saveToken(resp.token);
        final user = await _authRepository.getCurrentUser(resp.token);
        state = AuthState.authenticated(token: resp.token, user: user!);
        return true;
      }
      state = AuthState.unauthenticated().copyWith(error: resp.error);
      return false;
    } catch (e) {
      state = AuthState.unauthenticated().copyWith(error: e.toString());
      return false;
    }
  }

  Future<bool> loginWithGoogle(String idToken) async {
    try {
      final AuthResponse resp = await _authRepository.loginWithGoogle(idToken);
      // ignore: unnecessary_null_comparison
      if (resp.token != null) {
        await AuthTokenStorage.saveToken(resp.token);
        final user = await _authRepository.getCurrentUser(resp.token);
        state = AuthState.authenticated(token: resp.token, user: user!);
        return true;
      }
      state = AuthState.unauthenticated().copyWith(error: resp.error);
      return false;
    } catch (e) {
      state = AuthState.unauthenticated().copyWith(error: e.toString());
      return false;
    }
  }

  Future<bool> signup(String name, String email, String password) async {
  try {
    final AuthResponse resp = await _authRepository.signup(name, email, password);
    if (resp.token.isNotEmpty) {
      await AuthTokenStorage.saveToken(resp.token);
      final user = await _authRepository.getCurrentUser(resp.token);
      state = AuthState.authenticated(token: resp.token, user: user!);
      return true;
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
}


final authProvider = StateNotifierProvider<AuthNotifier, AuthState>((ref) {
  final repo = ref.watch(authRepositoryProvider);
  return AuthNotifier(repo);
});
