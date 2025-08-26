import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../dashboard/data/models/dashboard_models.dart';
import '../models/group_models.dart';

class GroupsRepository {
  final Dio _dio;
  static const String baseUrl = 'http://localhost:8080';

  GroupsRepository(this._dio);

  Future<List<GroupSummary>> getUserGroups(String userId) async {
    try {
      final response = await _dio.get('$baseUrl/api/groups/user/$userId');
      final List<dynamic> data = response.data;
      return data.map((item) => GroupSummary.fromJson(item)).toList();
    } catch (e) {
      // Return mock data
      return [
        GroupSummary(
          id: '1',
          name: 'Friends',
          description: 'Weekend hangouts',
          memberCount: 4,
          userBalance: -25.50,
          lastActivity: DateTime.now().subtract(const Duration(hours: 2)),
        ),
        GroupSummary(
          id: '2',
          name: 'Work Team',
          description: 'Office expenses',
          memberCount: 6,
          userBalance: 15.75,
          lastActivity: DateTime.now().subtract(const Duration(days: 1)),
        ),
        GroupSummary(
          id: '3',
          name: 'Roommates',
          description: 'Shared apartment costs',
          memberCount: 3,
          userBalance: -45.25,
          lastActivity: DateTime.now().subtract(const Duration(days: 2)),
        ),
      ];
    }
  }

  Future<GroupDetails> getGroupDetails(String groupId) async {
    try {
      final response = await _dio.get('$baseUrl/api/groups/$groupId');
      return GroupDetails.fromJson(response.data);
    } catch (e) {
      // Return mock data
      return GroupDetails(
        id: groupId,
        name: 'Friends',
        description: 'Weekend hangouts and shared expenses',
        imageUrl: null,
        members: [
          GroupMember(
            id: '1',
            name: 'John Doe',
            email: 'john@example.com',
            isAdmin: true,
            joinedAt: DateTime.now().subtract(const Duration(days: 30)),
          ),
          GroupMember(
            id: '2',
            name: 'Jane Smith',
            email: 'jane@example.com',
            isAdmin: false,
            joinedAt: DateTime.now().subtract(const Duration(days: 25)),
          ),
          GroupMember(
            id: '3',
            name: 'Bob Wilson',
            email: 'bob@example.com',
            isAdmin: false,
            joinedAt: DateTime.now().subtract(const Duration(days: 20)),
          ),
          GroupMember(
            id: '4',
            name: 'Alice Brown',
            email: 'alice@example.com',
            isAdmin: false,
            joinedAt: DateTime.now().subtract(const Duration(days: 15)),
          ),
        ],
        expenses: [
          GroupExpense(
            id: '1',
            name: 'Dinner at Pizza Palace',
            description: 'Group dinner after movie',
            amount: 120.50,
            currency: 'USD',
            paidById: '1',
            paidByName: 'John Doe',
            splitAmongIds: const ['1', '2', '3', '4'],
            createdAt: DateTime.now().subtract(const Duration(hours: 2)),
            isSettled: false,
          ),
          GroupExpense(
            id: '2',
            name: 'Movie Tickets',
            description: 'Avengers: Endgame tickets',
            amount: 60.00,
            currency: 'USD',
            paidById: '2',
            paidByName: 'Jane Smith',
            splitAmongIds: ['1', '2', '3', '4'],
            createdAt: DateTime.now().subtract(const Duration(hours: 4)),
            isSettled: true,
          ),
          GroupExpense(
            id: '3',
            name: 'Uber Ride',
            description: 'Shared ride to restaurant',
            amount: 25.75,
            currency: 'USD',
            paidById: '3',
            paidByName: 'Bob Wilson',
            splitAmongIds: const ['1', '2', '3'],
            createdAt: DateTime.now().subtract(const Duration(days: 1)),
            isSettled: false,
          ),
        ],
        createdAt: DateTime.now().subtract(const Duration(days: 30)),
        createdById: '1',
      );
    }
  }

  Future<GroupDetails> createGroup(CreateGroupRequest request) async {
    try {
      final response = await _dio.post(
        '$baseUrl/api/groups',
        data: request.toJson(),
      );
      return GroupDetails.fromJson(response.data);
    } catch (e) {
      // Return mock success
      return GroupDetails(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        name: request.name,
        description: request.description,
        imageUrl: null,
        members: [
          GroupMember(
            id: '1',
            name: 'Current User',
            email: 'current@example.com',
            isAdmin: true,
            joinedAt: DateTime.now(),
          ),
        ],
        expenses: [],
        createdAt: DateTime.now(),
        createdById: '1',
      );
    }
  }

  Future<GroupMember> addMember(String groupId, AddMemberRequest request) async {
    try {
      final response = await _dio.post(
        '$baseUrl/api/groups/$groupId/members',
        data: request.toJson(),
      );
      return GroupMember.fromJson(response.data);
    } catch (e) {
      // Return mock member
      return GroupMember(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        name: request.email.split('@')[0],
        email: request.email,
        isAdmin: false,
        joinedAt: DateTime.now(),
      );
    }
  }

  Future<void> removeMember(String groupId, String memberId) async {
    try {
      await _dio.delete('$baseUrl/api/groups/$groupId/members/$memberId');
    } catch (e) {
      // Mock success
    }
  }

  Future<void> makeAdmin(String groupId, String memberId) async {
    try {
      await _dio.patch('$baseUrl/api/groups/$groupId/members/$memberId/admin');
    } catch (e) {
      // Mock success
    }
  }

  Future<GroupExpense> createExpense(CreateExpenseRequest request) async {
    try {
      final response = await _dio.post(
        '$baseUrl/api/expenses',
        data: request.toJson(),
      );
      return GroupExpense.fromJson(response.data);
    } catch (e) {
      // Return mock expense
      return GroupExpense(
        id: DateTime.now().millisecondsSinceEpoch.toString(),
        name: request.name,
        description: request.description,
        amount: request.amount,
        currency: request.currency,
        paidById: '1',
        paidByName: 'Current User',
        splitAmongIds: request.splitAmongIds,
        createdAt: DateTime.now(),
        isSettled: false,
      );
    }
  }

  Future<void> updateGroup(String groupId, Map<String, dynamic> updates) async {
    try {
      await _dio.patch('$baseUrl/api/groups/$groupId', data: updates);
    } catch (e) {
      // Mock success
    }
  }

  Future<void> deleteGroup(String groupId) async {
    try {
      await _dio.delete('$baseUrl/api/groups/$groupId');
    } catch (e) {
      // Mock success
    }
  }

  Future<void> leaveGroup(String groupId, String userId) async {
    try {
      await _dio.delete('$baseUrl/api/groups/$groupId/members/$userId');
    } catch (e) {
      // Mock success
    }
  }
}

// Need to import GroupSummary from dashboard models
// (Defined in dashboard_models.dart as part of the dashboard feature)

final groupsRepositoryProvider = Provider<GroupsRepository>((ref) {
  final dio = Dio();
  return GroupsRepository(dio);
});
