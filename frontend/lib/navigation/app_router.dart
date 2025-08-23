import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../features/auth/presentation/login_screen.dart';
import '../features/auth/presentation/signup_screen.dart';
// Import your auth and screens
import '../features/auth/provider/auth_provider.dart';
import '../features/dashboard/presentation/dashboard_screen.dart';

// Derived provider that returns true if fully authenticated
final isAuthenticatedProvider = Provider<bool>((ref) {
  final authState = ref.watch(authProvider);
  return authState.isAuthenticated && authState.user != null;
});

final appRouterProvider = Provider<GoRouter>((ref) {
  // Reactively rebuild router on auth state change (important for redir!)
  final isAuthenticated = ref.watch(isAuthenticatedProvider);

  return GoRouter(
    initialLocation: isAuthenticated ? '/dashboard' : '/login',
    refreshListenable: GoRouterRefreshStream(
      ref.watch(authProvider.notifier).stream,
    ),
    routes: [
      GoRoute(
        path: '/login',
        builder: (context, state) => const LoginScreen(),
      ),
      GoRoute(
        path: '/dashboard',
        builder: (context, state) => const DashboardScreen(),
      ),
      GoRoute(
        path: '/signup',
        builder: (context, state) => const SignupScreen(),
      ),
      // Add more routes/screens as you build more features!
    ],
    redirect: (BuildContext context, GoRouterState state) {
      // Not authenticated? Allow access to login and signup pages
      final loggingIn = state.matchedLocation == '/login';
      final signingUp = state.matchedLocation == '/signup';
      if (!isAuthenticated && !loggingIn && !signingUp) {
        return '/login';
      }
      // Authenticated and trying to go to login or signup? Go to dashboard instead
      if (isAuthenticated && (loggingIn || signingUp)) {
        return '/dashboard';
      }
      return null;
    },
  );
});

// Helper for GoRouter to listen to Riverpod StateNotifier (for auth refresh)
class GoRouterRefreshStream extends ChangeNotifier {
  GoRouterRefreshStream(Stream<dynamic> stream) {
    notifyListener = () => notifyListeners();
    _subscription = stream.asBroadcastStream().listen((_) => notifyListener());
  }
  late final VoidCallback notifyListener;
  late final StreamSubscription<dynamic> _subscription;

  @override
  void dispose() {
    _subscription.cancel();
    super.dispose();
  }
}
