import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:splitspends_flutter/shared/app_config.dart';

class GroupsRepository {
  /// Fetches a list of all groups where the user is a member.
  Future<List> getGroupsByUserId(String userId) async {
    final uri = Uri.parse('${AppConfig.apiBaseUrl}/api/groups/user/$userId');
    final res = await http.get(uri);
    if (res.statusCode == 200) {
      return jsonDecode(res.body) as List;
    } else {
      throw Exception('Failed to fetch groups');
    }
  }

  /// Fetches details of a specific group (optionally by groupId).
  Future<Map<String, dynamic>> getGroupDetails(String groupId) async {
    final uri = Uri.parse('${AppConfig.apiBaseUrl}/api/groups/$groupId');
    final res = await http.get(uri);
    if (res.statusCode == 200) {
      return jsonDecode(res.body) as Map<String, dynamic>;
    } else {
      throw Exception('Failed to fetch group details');
    }
  }

  /// Creates a new group. [members] should be a List of userIds or emails, as required by your backend.
  Future<Map<String, dynamic>> createGroup({
    required String name,
    required List members,
  }) async {
    final uri = Uri.parse('${AppConfig.apiBaseUrl}/api/groups');
    final res = await http.post(
      uri,
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'name': name, 'members': members}),
    );
    if (res.statusCode == 201 || res.statusCode == 200) {
      return jsonDecode(res.body) as Map<String, dynamic>;
    } else {
      throw Exception('Failed to create group');
    }
  }

  /// Updates an existing group (rename, add/remove members, etc)
  Future<Map<String, dynamic>> updateGroup({
    required String groupId,
    String? name,
    List? members,
  }) async {
    final uri = Uri.parse('${AppConfig.apiBaseUrl}/api/groups/$groupId');
    final Map<String, dynamic> data = {};
    if (name != null) data['name'] = name;
    if (members != null) data['members'] = members;
    final res = await http.put(
      uri,
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode(data),
    );
    if (res.statusCode == 200) {
      return jsonDecode(res.body) as Map<String, dynamic>;
    } else {
      throw Exception('Failed to update group');
    }
  }
}
