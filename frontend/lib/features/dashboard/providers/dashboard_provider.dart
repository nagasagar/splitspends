import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../auth/provider/auth_provider.dart';
import '../data/models/dashboard_models.dart';
import '../data/repositories/dashboard_repository.dart';

// Dashboard data provider
final dashboardDataProvider = FutureProvider<DashboardData?>((ref) async {
  final authState = ref.watch(authProvider);
  if (authState.user == null) return null;

  final repository = ref.watch(dashboardRepositoryProvider);
  return repository.getDashboardData(authState.user!.id.toString());
});

// Dashboard summary provider
final dashboardSummaryProvider = FutureProvider<DashboardSummary?>((ref) async {
  final authState = ref.watch(authProvider);
  if (authState.user == null) return null;

  final repository = ref.watch(dashboardRepositoryProvider);
  return repository.getDashboardSummary(authState.user!.id.toString());
});

// Recent activities provider
final recentActivitiesProvider = FutureProvider<List<RecentActivity>>((ref) async {
  final authState = ref.watch(authProvider);
  if (authState.user == null) return [];

  final repository = ref.watch(dashboardRepositoryProvider);
  return repository.getRecentActivities(authState.user!.id.toString());
});

// User groups provider
final userGroupsProvider = FutureProvider<List<GroupSummary>>((ref) async {
  final authState = ref.watch(authProvider);
  if (authState.user == null) return [];

  final repository = ref.watch(dashboardRepositoryProvider);
  return repository.getUserGroups(authState.user!.id.toString());
});
