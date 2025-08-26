import 'package:equatable/equatable.dart';

class DashboardSummary extends Equatable {
  final double totalOwed;
  final double totalToReceive;
  final int groupCount;
  final int expenseCount;
  final double monthlySpending;

  const DashboardSummary({
    required this.totalOwed,
    required this.totalToReceive,
    required this.groupCount,
    required this.expenseCount,
    required this.monthlySpending,
  });

  factory DashboardSummary.fromJson(Map<String, dynamic> json) {
    return DashboardSummary(
      totalOwed: (json['totalOwed'] as num?)?.toDouble() ?? 0.0,
      totalToReceive: (json['totalToReceive'] as num?)?.toDouble() ?? 0.0,
      groupCount: json['groupCount'] as int? ?? 0,
      expenseCount: json['expenseCount'] as int? ?? 0,
      monthlySpending: (json['monthlySpending'] as num?)?.toDouble() ?? 0.0,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'totalOwed': totalOwed,
      'totalToReceive': totalToReceive,
      'groupCount': groupCount,
      'expenseCount': expenseCount,
      'monthlySpending': monthlySpending,
    };
  }

  @override
  List<Object?> get props => [
        totalOwed,
        totalToReceive,
        groupCount,
        expenseCount,
        monthlySpending,
      ];
}

class RecentActivity extends Equatable {
  final String id;
  final String type; // 'expense_added', 'settlement_completed', etc.
  final String description;
  final DateTime timestamp;
  final double? amount;
  final String? groupName;
  final String? currency;

  const RecentActivity({
    required this.id,
    required this.type,
    required this.description,
    required this.timestamp,
    this.amount,
    this.groupName,
    this.currency,
  });

  factory RecentActivity.fromJson(Map<String, dynamic> json) {
    return RecentActivity(
      id: json['id'] as String,
      type: json['type'] as String,
      description: json['description'] as String,
      timestamp: DateTime.parse(json['timestamp'] as String),
      amount: (json['amount'] as num?)?.toDouble(),
      groupName: json['groupName'] as String?,
      currency: json['currency'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'type': type,
      'description': description,
      'timestamp': timestamp.toIso8601String(),
      'amount': amount,
      'groupName': groupName,
      'currency': currency,
    };
  }

  @override
  List<Object?> get props => [
        id,
        type,
        description,
        timestamp,
        amount,
        groupName,
        currency,
      ];
}

class GroupSummary extends Equatable {
  final String id;
  final String name;
  final String? description;
  final int memberCount;
  final double? userBalance; // What user owes/is owed in this group
  final String? groupImageUrl;
  final DateTime lastActivity;

  const GroupSummary({
    required this.id,
    required this.name,
    this.description,
    required this.memberCount,
    this.userBalance,
    this.groupImageUrl,
    required this.lastActivity,
  });

  factory GroupSummary.fromJson(Map<String, dynamic> json) {
    return GroupSummary(
      id: json['id'] as String,
      name: json['name'] as String,
      description: json['description'] as String?,
      memberCount: json['memberCount'] as int,
      userBalance: (json['userBalance'] as num?)?.toDouble(),
      groupImageUrl: json['groupImageUrl'] as String?,
      lastActivity: DateTime.parse(json['lastActivity'] as String),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'description': description,
      'memberCount': memberCount,
      'userBalance': userBalance,
      'groupImageUrl': groupImageUrl,
      'lastActivity': lastActivity.toIso8601String(),
    };
  }

  @override
  List<Object?> get props => [
        id,
        name,
        description,
        memberCount,
        userBalance,
        groupImageUrl,
        lastActivity,
      ];
}

class DashboardData extends Equatable {
  final DashboardSummary summary;
  final List<RecentActivity> recentActivities;
  final List<GroupSummary> groups;

  const DashboardData({
    required this.summary,
    required this.recentActivities,
    required this.groups,
  });

  factory DashboardData.fromJson(Map<String, dynamic> json) {
    return DashboardData(
      summary: DashboardSummary.fromJson(json['summary'] as Map<String, dynamic>),
      recentActivities: (json['recentActivities'] as List<dynamic>)
          .map((item) => RecentActivity.fromJson(item as Map<String, dynamic>))
          .toList(),
      groups: (json['groups'] as List<dynamic>)
          .map((item) => GroupSummary.fromJson(item as Map<String, dynamic>))
          .toList(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'summary': summary.toJson(),
      'recentActivities': recentActivities.map((item) => item.toJson()).toList(),
      'groups': groups.map((item) => item.toJson()).toList(),
    };
  }

  @override
  List<Object?> get props => [summary, recentActivities, groups];
}
