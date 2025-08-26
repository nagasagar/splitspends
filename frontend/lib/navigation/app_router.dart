import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../features/activity/presentation/activity_list_screen.dart';
import '../features/auth/presentation/login_screen.dart';
import '../features/auth/presentation/signup_screen.dart';
// Import your auth and screens
import '../features/auth/provider/auth_provider.dart';
import '../features/balances/presentation/you_are_owed_screen.dart';
import '../features/balances/presentation/you_owe_screen.dart';
import '../features/dashboard/presentation/dashboard_screen.dart';
import '../features/expenses/presentation/expense_detail_screen.dart';
import '../features/expenses/presentation/expenses_list_screen.dart';
import '../features/groups/presentation/screens/add_expense_screen.dart';
import '../features/groups/presentation/screens/create_group_screen.dart';
import '../features/groups/presentation/screens/group_details_screen.dart';
import '../features/groups/presentation/screens/groups_list_screen.dart';
import '../features/settlements/presentation/settle_up_screen.dart';

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
      
      // Group routes
      GoRoute(
        path: '/groups',
        builder: (context, state) => const GroupsListScreen(),
      ),
      GoRoute(
        path: '/groups/create',
        builder: (context, state) => const CreateGroupScreen(),
      ),
      GoRoute(
        path: '/groups/:groupId',
        builder: (context, state) {
          final groupId = state.pathParameters['groupId']!;
          return GroupDetailsScreen(groupId: groupId);
        },
      ),

      // Expense routes
      GoRoute(
        path: '/expenses',
        builder: (context, state) => const ExpensesListScreen(),
      ),
      GoRoute(
        path: '/expenses/create',
        builder: (context, state) {
          final extra = state.extra as Map<String, dynamic>?;
          final groupId = extra?['groupId'] as String? ?? '';
          return AddExpenseScreen(groupId: groupId);
        },
      ),
      GoRoute(
        path: '/expenses/:expenseId',
        builder: (context, state) {
          final expenseId = state.pathParameters['expenseId']!;
          return ExpenseDetailScreen(expenseId: expenseId);
        },
      ),

      // Balance routes
      GoRoute(
        path: '/balances/you-owe',
        builder: (context, state) => const YouOweScreen(),
      ),
      GoRoute(
        path: '/balances/you-are-owed',
        builder: (context, state) => const YouAreOwedScreen(),
      ),

      // Settlement routes
      GoRoute(
        path: '/settle-up',
        builder: (context, state) {
          final extra = state.extra as Map<String, dynamic>?;
          final groupId = extra?['groupId'] as String?;
          return SettleUpScreen(groupId: groupId);
        },
      ),

      // Activity route
      GoRoute(
        path: '/activity',
        builder: (context, state) => const ActivityListScreen(),
      ),
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
