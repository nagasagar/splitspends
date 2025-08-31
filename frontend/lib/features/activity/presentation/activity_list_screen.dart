// ignore_for_file: deprecated_member_use

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../auth/provider/auth_provider.dart';
import '../data/repositories/activity_repository.dart';

class ActivityListScreen extends ConsumerStatefulWidget {
  const ActivityListScreen({super.key});

  @override
  ConsumerState<ActivityListScreen> createState() => _ActivityListScreenState();
}

class _ActivityListScreenState extends ConsumerState<ActivityListScreen> {
  String _selectedFilter = 'all';
  List<Map<String, dynamic>> _activities = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadActivities();
  }

  Future<void> _loadActivities() async {
    setState(() {
      _isLoading = true;
    });

    try {
      final authState = ref.read(authProvider);
      if (authState.isAuthenticated) {
        final activityRepository = ref.read(activityRepositoryProvider);
        final activities = await activityRepository.getUserActivities(
          authState.user!.id.toString(),
          limit: 50,
        );

        setState(() {
          _activities = activities;
          _isLoading = false;
        });
      }
    } catch (e) {
      setState(() {
        _activities = [];
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final filteredActivities = _getFilteredActivities();

    return Scaffold(
      appBar: AppBar(
        title: const Text('All Activity'),
        backgroundColor: Colors.indigo[600],
        foregroundColor: Colors.white,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.pop(),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.filter_list),
            onPressed: () => _showFilterDialog(context),
          ),
        ],
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _loadActivities,
              child: Column(
                children: [
                  // Filter Chips
                  Container(
                    padding: const EdgeInsets.all(16),
                    child: SingleChildScrollView(
                      scrollDirection: Axis.horizontal,
                      child: Row(
                        children: [
                          _buildFilterChip('All', 'all'),
                          const SizedBox(width: 8),
                          _buildFilterChip('My Actions', 'my_actions'),
                          const SizedBox(width: 8),
                          _buildFilterChip('Expenses', 'expenses'),
                          const SizedBox(width: 8),
                          _buildFilterChip('Settlements', 'settlements'),
                          const SizedBox(width: 8),
                          _buildFilterChip('Groups', 'groups'),
                        ],
                      ),
                    ),
                  ),

                  // Activity Stats Card
                  Container(
                    margin: const EdgeInsets.symmetric(horizontal: 16),
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        colors: [Colors.indigo[400]!, Colors.indigo[600]!],
                        begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Total Activities',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 14,
                        ),
                      ),
                      Text(
                        '${_activities.length}',
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 24,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ],
                  ),
                ),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      const Text(
                        'Your Actions',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 14,
                        ),
                      ),
                      Text(
                        '${_activities.where((a) => a['isUserAction']).length}',
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 24,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ],
                  ),
                ),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      const Text(
                        'This Week',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 14,
                        ),
                      ),
                      Text(
                        '${_getThisWeekCount()}',
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 24,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),

          const SizedBox(height: 16),

                  // Activities List
                  Expanded(
                    child: filteredActivities.isEmpty
                        ? const Center(
                            child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Icon(Icons.history, size: 64, color: Colors.grey),
                                SizedBox(height: 16),
                                Text(
                                  'No activity found',
                                  style: TextStyle(fontSize: 18, color: Colors.grey),
                                ),
                                SizedBox(height: 8),
                                Text(
                                  'Start adding expenses to see activity',
                                  style: TextStyle(color: Colors.grey),
                                ),
                              ],
                            ),
                          )
                        : ListView.builder(
                            padding: const EdgeInsets.symmetric(horizontal: 16),
                            itemCount: filteredActivities.length,
                            itemBuilder: (context, index) {
                              final activity = filteredActivities[index];
                              final isToday = _isToday(activity['timestamp']);
                              final isYesterday = _isYesterday(activity['timestamp']);

                              // Show date header if needed
                              bool showDateHeader = false;
                              if (index == 0) {
                                showDateHeader = true;
                              } else {
                                final prevActivity = filteredActivities[index - 1];
                                final prevDate = _getDateString(prevActivity['timestamp']);
                                final currentDate = _getDateString(activity['timestamp']);
                                showDateHeader = prevDate != currentDate;
                              }

                              return Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  if (showDateHeader) ...[
                                    Padding(
                                      padding: const EdgeInsets.only(left: 8, top: 16, bottom: 8),
                                      child: Text(
                                        isToday
                                            ? 'Today'
                                            : isYesterday
                                                ? 'Yesterday'
                                                : _getDateString(activity['timestamp']),
                                        style: TextStyle(
                                          fontSize: 16,
                                          fontWeight: FontWeight.bold,
                                          color: Colors.grey[700],
                                        ),
                                      ),
                                    ),
                                  ],
                                  _buildActivityItem(activity),
                                ],
                              );
                            },
                          ),
                  ),
                ],
              ),
            ),
    );
  }

  Widget _buildFilterChip(String label, String value) {
    final isSelected = _selectedFilter == value;
    return FilterChip(
      label: Text(label),
      selected: isSelected,
      onSelected: (selected) {
        setState(() {
          _selectedFilter = value;
        });
      },
      selectedColor: Colors.indigo[100],
      checkmarkColor: Colors.indigo[600],
    );
  }

  Widget _buildActivityItem(Map<String, dynamic> activity) {
    final isUserAction = activity['isUserAction'] as bool;
    final activityType = activity['type'] as String;
    
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: InkWell(
        onTap: () => _onActivityTap(activity),
        borderRadius: BorderRadius.circular(8),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              // Activity Icon
              Container(
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: _getActivityColor(activityType).withOpacity(0.1),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Icon(
                  _getActivityIcon(activityType),
                  color: _getActivityColor(activityType),
                  size: 20,
                ),
              ),
              const SizedBox(width: 16),

              // Activity Details
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      activity['title'],
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      activity['description'],
                      style: TextStyle(
                        fontSize: 14,
                        color: Colors.grey[600],
                      ),
                    ),
                    const SizedBox(height: 4),
                    Row(
                      children: [
                        if (activity['groupName'] != null) ...[
                          Icon(
                            Icons.group,
                            size: 12,
                            color: Colors.grey[500],
                          ),
                          const SizedBox(width: 4),
                          Text(
                            activity['groupName'],
                            style: TextStyle(
                              fontSize: 12,
                              color: Colors.grey[500],
                            ),
                          ),
                          Text(
                            ' • ',
                            style: TextStyle(
                              fontSize: 12,
                              color: Colors.grey[500],
                            ),
                          ),
                        ],
                        Text(
                          _getTimeAgo(activity['timestamp']),
                          style: TextStyle(
                            fontSize: 12,
                            color: Colors.grey[500],
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),

              // Amount and User Badge
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  if (activity['amount'] != null)
                    Text(
                      '₹${activity['amount'].toStringAsFixed(0)}',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.bold,
                        color: activityType == 'settlement_completed' 
                            ? Colors.green[600] 
                            : Colors.black87,
                      ),
                    ),
                  const SizedBox(height: 4),
                  if (isUserAction)
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                      decoration: BoxDecoration(
                        color: Colors.blue[100],
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Text(
                        'You',
                        style: TextStyle(
                          fontSize: 10,
                          color: Colors.blue[700],
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  List<Map<String, dynamic>> _getFilteredActivities() {
    switch (_selectedFilter) {
      case 'my_actions':
        return _activities.where((activity) => activity['isUserAction'] == true).toList();
      case 'expenses':
        return _activities.where((activity) => 
          activity['type'] == 'expense_added' || 
          activity['type'] == 'expense_edited' || 
          activity['type'] == 'expense_deleted').toList();
      case 'settlements':
        return _activities.where((activity) => activity['type'] == 'settlement_completed').toList();
      case 'groups':
        return _activities.where((activity) => 
          activity['type'] == 'group_created' || 
          activity['type'] == 'member_added').toList();
      default:
        return _activities;
    }
  }

  IconData _getActivityIcon(String type) {
    switch (type) {
      case 'expense_added':
        return Icons.add_circle_outline;
      case 'expense_edited':
        return Icons.edit_outlined;
      case 'expense_deleted':
        return Icons.delete_outline;
      case 'settlement_completed':
        return Icons.check_circle_outline;
      case 'group_created':
        return Icons.group_add;
      case 'member_added':
        return Icons.person_add;
      default:
        return Icons.info_outline;
    }
  }

  Color _getActivityColor(String type) {
    switch (type) {
      case 'expense_added':
        return Colors.blue[600]!;
      case 'expense_edited':
        return Colors.orange[600]!;
      case 'expense_deleted':
        return Colors.red[600]!;
      case 'settlement_completed':
        return Colors.green[600]!;
      case 'group_created':
        return Colors.purple[600]!;
      case 'member_added':
        return Colors.teal[600]!;
      default:
        return Colors.grey[600]!;
    }
  }

  bool _isToday(DateTime date) {
    final now = DateTime.now();
    return date.year == now.year && date.month == now.month && date.day == now.day;
  }

  bool _isYesterday(DateTime date) {
    final yesterday = DateTime.now().subtract(const Duration(days: 1));
    return date.year == yesterday.year && date.month == yesterday.month && date.day == yesterday.day;
  }

  String _getDateString(DateTime date) {
    final now = DateTime.now();
    final difference = now.difference(date).inDays;
    
    if (difference == 0) {
      return 'Today';
    } else if (difference == 1) {
      return 'Yesterday';
    } else if (difference < 7) {
      return '$difference days ago';
    } else {
      return '${date.day}/${date.month}/${date.year}';
    }
  }

  String _getTimeAgo(DateTime timestamp) {
    final now = DateTime.now();
    final difference = now.difference(timestamp);

    if (difference.inMinutes < 1) {
      return 'Just now';
    } else if (difference.inHours < 1) {
      return '${difference.inMinutes}m ago';
    } else if (difference.inDays < 1) {
      return '${difference.inHours}h ago';
    } else {
      return '${difference.inDays}d ago';
    }
  }

  int _getThisWeekCount() {
    final now = DateTime.now();
    final startOfWeek = now.subtract(Duration(days: now.weekday - 1));
    return _activities.where((activity) {
      final activityDate = activity['timestamp'] as DateTime;
      return activityDate.isAfter(startOfWeek);
    }).length;
  }

  void _onActivityTap(Map<String, dynamic> activity) {
    final type = activity['type'] as String;
    final id = activity['id'] as String;
    
    // Navigate based on activity type
    switch (type) {
      case 'expense_added':
      case 'expense_edited':
        context.push('/expenses/$id');
        break;
      case 'group_created':
      case 'member_added':
        if (activity['groupName'] != null) {
          context.push('/groups/1'); // In real app, use actual group ID
        }
        break;
      case 'settlement_completed':
        // Could navigate to settlement details or balances
        context.push('/balances/you-owe');
        break;
      default:
        // Show details in a dialog
        _showActivityDetails(context, activity);
    }
  }

  void _showActivityDetails(BuildContext context, Map<String, dynamic> activity) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(activity['title']),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(activity['description']),
            const SizedBox(height: 16),
            if (activity['amount'] != null)
              Text('Amount: ₹${activity['amount'].toStringAsFixed(2)}'),
            if (activity['groupName'] != null)
              Text('Group: ${activity['groupName']}'),
            Text('User: ${activity['userName']}'),
            Text('Time: ${activity['timestamp']}'),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Close'),
          ),
        ],
      ),
    );
  }

  void _showFilterDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Filter Activities'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              title: const Text('All Activities'),
              leading: Radio<String>(
                value: 'all',
                groupValue: _selectedFilter,
                onChanged: (value) {
                  setState(() {
                    _selectedFilter = value!;
                  });
                  Navigator.of(context).pop();
                },
              ),
            ),
            ListTile(
              title: const Text('My Actions'),
              leading: Radio<String>(
                value: 'my_actions',
                groupValue: _selectedFilter,
                onChanged: (value) {
                  setState(() {
                    _selectedFilter = value!;
                  });
                  Navigator.of(context).pop();
                },
              ),
            ),
            ListTile(
              title: const Text('Expenses Only'),
              leading: Radio<String>(
                value: 'expenses',
                groupValue: _selectedFilter,
                onChanged: (value) {
                  setState(() {
                    _selectedFilter = value!;
                  });
                  Navigator.of(context).pop();
                },
              ),
            ),
            ListTile(
              title: const Text('Settlements Only'),
              leading: Radio<String>(
                value: 'settlements',
                groupValue: _selectedFilter,
                onChanged: (value) {
                  setState(() {
                    _selectedFilter = value!;
                  });
                  Navigator.of(context).pop();
                },
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Close'),
          ),
        ],
      ),
    );
  }
}
