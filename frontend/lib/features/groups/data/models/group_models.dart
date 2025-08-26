import 'package:equatable/equatable.dart';

class GroupMember extends Equatable {
  final String id;
  final String name;
  final String email;
  final String? profilePictureUrl;
  final bool isAdmin;
  final DateTime joinedAt;

  const GroupMember({
    required this.id,
    required this.name,
    required this.email,
    this.profilePictureUrl,
    required this.isAdmin,
    required this.joinedAt,
  });

  factory GroupMember.fromJson(Map<String, dynamic> json) {
    return GroupMember(
      id: json['id'] as String,
      name: json['name'] as String,
      email: json['email'] as String,
      profilePictureUrl: json['profilePictureUrl'] as String?,
      isAdmin: json['isAdmin'] as bool? ?? false,
      joinedAt: DateTime.parse(json['joinedAt'] as String),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'email': email,
      'profilePictureUrl': profilePictureUrl,
      'isAdmin': isAdmin,
      'joinedAt': joinedAt.toIso8601String(),
    };
  }

  @override
  List<Object?> get props => [id, name, email, profilePictureUrl, isAdmin, joinedAt];
}

class GroupExpense extends Equatable {
  final String id;
  final String name;
  final String description;
  final double amount;
  final String currency;
  final String paidById;
  final String paidByName;
  final List<String> splitAmongIds;
  final DateTime createdAt;
  final bool isSettled;

  const GroupExpense({
    required this.id,
    required this.name,
    required this.description,
    required this.amount,
    required this.currency,
    required this.paidById,
    required this.paidByName,
    required this.splitAmongIds,
    required this.createdAt,
    required this.isSettled,
  });

  factory GroupExpense.fromJson(Map<String, dynamic> json) {
    return GroupExpense(
      id: json['id'] as String,
      name: json['name'] as String,
      description: json['description'] as String? ?? '',
      amount: (json['amount'] as num).toDouble(),
      currency: json['currency'] as String? ?? 'USD',
      paidById: json['paidById'] as String,
      paidByName: json['paidByName'] as String,
      splitAmongIds: List<String>.from(json['splitAmongIds'] as List),
      createdAt: DateTime.parse(json['createdAt'] as String),
      isSettled: json['isSettled'] as bool? ?? false,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'description': description,
      'amount': amount,
      'currency': currency,
      'paidById': paidById,
      'paidByName': paidByName,
      'splitAmongIds': splitAmongIds,
      'createdAt': createdAt.toIso8601String(),
      'isSettled': isSettled,
    };
  }

  @override
  List<Object?> get props => [
        id,
        name,
        description,
        amount,
        currency,
        paidById,
        paidByName,
        splitAmongIds,
        createdAt,
        isSettled,
      ];
}

class GroupDetails extends Equatable {
  final String id;
  final String name;
  final String? description;
  final String? imageUrl;
  final List<GroupMember> members;
  final List<GroupExpense> expenses;
  final DateTime createdAt;
  final String createdById;

  const GroupDetails({
    required this.id,
    required this.name,
    this.description,
    this.imageUrl,
    required this.members,
    required this.expenses,
    required this.createdAt,
    required this.createdById,
  });

  factory GroupDetails.fromJson(Map<String, dynamic> json) {
    return GroupDetails(
      id: json['id'] as String,
      name: json['name'] as String,
      description: json['description'] as String?,
      imageUrl: json['imageUrl'] as String?,
      members: (json['members'] as List<dynamic>)
          .map((item) => GroupMember.fromJson(item as Map<String, dynamic>))
          .toList(),
      expenses: (json['expenses'] as List<dynamic>)
          .map((item) => GroupExpense.fromJson(item as Map<String, dynamic>))
          .toList(),
      createdAt: DateTime.parse(json['createdAt'] as String),
      createdById: json['createdById'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'description': description,
      'imageUrl': imageUrl,
      'members': members.map((member) => member.toJson()).toList(),
      'expenses': expenses.map((expense) => expense.toJson()).toList(),
      'createdAt': createdAt.toIso8601String(),
      'createdById': createdById,
    };
  }

  bool isUserAdmin(String userId) {
    return members.any((member) => member.id == userId && member.isAdmin);
  }

  GroupMember? getMemberById(String userId) {
    try {
      return members.firstWhere((member) => member.id == userId);
    } catch (e) {
      return null;
    }
  }

  @override
  List<Object?> get props => [
        id,
        name,
        description,
        imageUrl,
        members,
        expenses,
        createdAt,
        createdById,
      ];
}

class CreateGroupRequest extends Equatable {
  final String name;
  final String? description;
  final List<String> memberEmails;

  const CreateGroupRequest({
    required this.name,
    this.description,
    required this.memberEmails,
  });

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'description': description,
      'memberEmails': memberEmails,
    };
  }

  @override
  List<Object?> get props => [name, description, memberEmails];
}

class AddMemberRequest extends Equatable {
  final String email;

  const AddMemberRequest({
    required this.email,
  });

  Map<String, dynamic> toJson() {
    return {
      'email': email,
    };
  }

  @override
  List<Object?> get props => [email];
}

class CreateExpenseRequest extends Equatable {
  final String name;
  final String description;
  final double amount;
  final String currency;
  final String groupId;
  final List<String> splitAmongIds;

  const CreateExpenseRequest({
    required this.name,
    required this.description,
    required this.amount,
    required this.currency,
    required this.groupId,
    required this.splitAmongIds,
  });

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'description': description,
      'amount': amount,
      'currency': currency,
      'groupId': groupId,
      'splitAmongIds': splitAmongIds,
    };
  }

  @override
  List<Object?> get props => [name, description, amount, currency, groupId, splitAmongIds];
}
