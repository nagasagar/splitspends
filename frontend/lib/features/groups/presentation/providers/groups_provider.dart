import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../dashboard/data/models/dashboard_models.dart';
import '../../data/models/group_models.dart';
import '../../data/repositories/groups_repository.dart';

// State classes for groups
class GroupsState {
  final List<GroupSummary> groups;
  final bool isLoading;
  final String? error;

  const GroupsState({
    this.groups = const [],
    this.isLoading = false,
    this.error,
  });

  GroupsState copyWith({
    List<GroupSummary>? groups,
    bool? isLoading,
    String? error,
  }) {
    return GroupsState(
      groups: groups ?? this.groups,
      isLoading: isLoading ?? this.isLoading,
      error: error,
    );
  }
}

class GroupDetailsState {
  final GroupDetails? groupDetails;
  final bool isLoading;
  final String? error;

  const GroupDetailsState({
    this.groupDetails,
    this.isLoading = false,
    this.error,
  });

  GroupDetailsState copyWith({
    GroupDetails? groupDetails,
    bool? isLoading,
    String? error,
  }) {
    return GroupDetailsState(
      groupDetails: groupDetails ?? this.groupDetails,
      isLoading: isLoading ?? this.isLoading,
      error: error,
    );
  }
}

// Groups list provider
class GroupsNotifier extends StateNotifier<GroupsState> {
  final GroupsRepository _repository;

  GroupsNotifier(this._repository) : super(const GroupsState());

  Future<void> loadUserGroups(String userId) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final groups = await _repository.getUserGroups(userId);
      state = state.copyWith(groups: groups, isLoading: false);
    } catch (e) {
      state = state.copyWith(error: e.toString(), isLoading: false);
    }
  }

  Future<void> refreshGroups(String userId) async {
    await loadUserGroups(userId);
  }

  Future<GroupDetails?> createGroup(CreateGroupRequest request) async {
    try {
      final newGroup = await _repository.createGroup(request);
      // Refresh the groups list
      // Note: In a real app, you'd get the current user ID from auth
      await loadUserGroups('1');
      return newGroup;
    } catch (e) {
      state = state.copyWith(error: e.toString());
      return null;
    }
  }

  void clearError() {
    state = state.copyWith(error: null);
  }
}

// Group details provider
class GroupDetailsNotifier extends StateNotifier<GroupDetailsState> {
  final GroupsRepository _repository;

  GroupDetailsNotifier(this._repository) : super(const GroupDetailsState());

  Future<void> loadGroupDetails(String groupId) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final groupDetails = await _repository.getGroupDetails(groupId);
      state = state.copyWith(groupDetails: groupDetails, isLoading: false);
    } catch (e) {
      state = state.copyWith(error: e.toString(), isLoading: false);
    }
  }

  Future<void> addMember(String groupId, AddMemberRequest request) async {
    try {
      await _repository.addMember(groupId, request);
      // Refresh group details
      await loadGroupDetails(groupId);
    } catch (e) {
      state = state.copyWith(error: e.toString());
    }
  }

  Future<void> removeMember(String groupId, String memberId) async {
    try {
      await _repository.removeMember(groupId, memberId);
      // Refresh group details
      await loadGroupDetails(groupId);
    } catch (e) {
      state = state.copyWith(error: e.toString());
    }
  }

  Future<void> makeAdmin(String groupId, String memberId) async {
    try {
      await _repository.makeAdmin(groupId, memberId);
      // Refresh group details
      await loadGroupDetails(groupId);
    } catch (e) {
      state = state.copyWith(error: e.toString());
    }
  }

  Future<void> createExpense(CreateExpenseRequest request) async {
    try {
      await _repository.createExpense(request);
      // Refresh group details
      await loadGroupDetails(request.groupId);
    } catch (e) {
      state = state.copyWith(error: e.toString());
    }
  }

  Future<void> updateGroup(String groupId, Map<String, dynamic> updates) async {
    try {
      await _repository.updateGroup(groupId, updates);
      // Refresh group details
      await loadGroupDetails(groupId);
    } catch (e) {
      state = state.copyWith(error: e.toString());
    }
  }

  Future<void> deleteGroup(String groupId) async {
    try {
      await _repository.deleteGroup(groupId);
      // Clear the current group details
      state = const GroupDetailsState();
    } catch (e) {
      state = state.copyWith(error: e.toString());
    }
  }

  Future<void> leaveGroup(String groupId, String userId) async {
    try {
      await _repository.leaveGroup(groupId, userId);
      // Clear the current group details
      state = const GroupDetailsState();
    } catch (e) {
      state = state.copyWith(error: e.toString());
    }
  }

  void clearError() {
    state = state.copyWith(error: null);
  }
}

// Providers
final groupsProvider = StateNotifierProvider<GroupsNotifier, GroupsState>((ref) {
  final repository = ref.watch(groupsRepositoryProvider);
  return GroupsNotifier(repository);
});

final groupDetailsProvider = StateNotifierProvider<GroupDetailsNotifier, GroupDetailsState>((ref) {
  final repository = ref.watch(groupsRepositoryProvider);
  return GroupDetailsNotifier(repository);
});

// Selected group provider for navigation
final selectedGroupProvider = StateProvider<String?>((ref) => null);
