import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

class GroupsListScreen extends ConsumerStatefulWidget {
  const GroupsListScreen({super.key});

  @override
  ConsumerState<GroupsListScreen> createState() => _GroupsListScreenState();
}

class _GroupsListScreenState extends ConsumerState<GroupsListScreen> {
  String _selectedFilter = 'all';

  // Mock groups data
  final List<Map<String, dynamic>> _groups = [
    {
      'id': '1',
      'name': 'Trip to Goa',
      'description': 'Beach vacation with friends',
      'memberCount': 4,
      'totalExpenses': 12500.0,
      'userBalance': -2400.0, // User owes money
      'lastActivity': 'Today',
      'isActive': true,
      'imageUrl': null,
      'category': 'Travel',
    },
    {
      'id': '2',
      'name': 'Office Lunch',
      'description': 'Daily office lunch expenses',
      'memberCount': 8,
      'totalExpenses': 3200.0,
      'userBalance': 150.0, // User is owed money
      'lastActivity': 'Yesterday',
      'isActive': true,
      'imageUrl': null,
      'category': 'Food',
    },
    {
      'id': '3',
      'name': 'Apartment Rent',
      'description': 'Monthly apartment expenses',
      'memberCount': 3,
      'totalExpenses': 45000.0,
      'userBalance': 0.0, // All settled
      'lastActivity': '1 week ago',
      'isActive': true,
      'imageUrl': null,
      'category': 'Housing',
    },
    {
      'id': '4',
      'name': 'Movie Night',
      'description': 'Weekend movie and snacks',
      'memberCount': 6,
      'totalExpenses': 1800.0,
      'userBalance': -300.0,
      'lastActivity': '2 weeks ago',
      'isActive': false,
      'imageUrl': null,
      'category': 'Entertainment',
    },
    {
      'id': '5',
      'name': 'Birthday Party',
      'description': 'Sarah\'s birthday celebration',
      'memberCount': 12,
      'totalExpenses': 8500.0,
      'userBalance': 425.0,
      'lastActivity': '1 month ago',
      'isActive': false,
      'imageUrl': null,
      'category': 'Celebration',
    },
  ];

  @override
  Widget build(BuildContext context) {
    final filteredGroups = _getFilteredGroups();

    return Scaffold(
      appBar: AppBar(
        title: const Text('All Groups'),
        backgroundColor: Colors.teal[600],
        foregroundColor: Colors.white,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.pop(),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.search),
            onPressed: () => _showSearchDialog(context),
          ),
        ],
      ),
      body: Column(
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
                  _buildFilterChip('Active', 'active'),
                  const SizedBox(width: 8),
                  _buildFilterChip('Settled', 'settled'),
                  const SizedBox(width: 8),
                  _buildFilterChip('I owe', 'i_owe'),
                  const SizedBox(width: 8),
                  _buildFilterChip('I\'m owed', 'im_owed'),
                ],
              ),
            ),
          ),

          // Groups Stats Card
          Container(
            margin: const EdgeInsets.symmetric(horizontal: 16),
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [Colors.teal[400]!, Colors.teal[600]!],
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
                        'Total Groups',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 14,
                        ),
                      ),
                      Text(
                        '${_groups.length}',
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
                        'Active Groups',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 14,
                        ),
                      ),
                      Text(
                        '${_groups.where((g) => g['isActive']).length}',
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
                        'Net Balance',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 14,
                        ),
                      ),
                      Text(
                        _getNetBalanceText(),
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 20,
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

          // Groups List
          Expanded(
            child: ListView.builder(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              itemCount: filteredGroups.length,
              itemBuilder: (context, index) {
                final group = filteredGroups[index];
                return _buildGroupItem(group);
              },
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => context.push('/groups/create'),
        backgroundColor: Colors.teal[600],
        child: const Icon(Icons.add, color: Colors.white),
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
      selectedColor: Colors.teal[100],
      checkmarkColor: Colors.teal[600],
    );
  }

  Widget _buildGroupItem(Map<String, dynamic> group) {
    final balance = group['userBalance'] as double;
    final isOwed = balance > 0;
    final isSettled = balance == 0;
    final balanceColor = isSettled
        ? Colors.grey[600]!
        : isOwed
            ? Colors.green[600]!
            : Colors.red[600]!;

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: () => context.push('/groups/${group['id']}'),
        borderRadius: BorderRadius.circular(8),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Group Header
              Row(
                children: [
                  // Group Avatar
                  CircleAvatar(
                    radius: 24,
                    backgroundColor: _getCategoryColor(group['category']),
                    child: Icon(
                      _getCategoryIcon(group['category']),
                      color: Colors.white,
                      size: 20,
                    ),
                  ),
                  const SizedBox(width: 16),

                  // Group Info
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Expanded(
                              child: Text(
                                group['name'],
                                style: const TextStyle(
                                  fontSize: 16,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ),
                            if (!group['isActive'])
                              Container(
                                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                                decoration: BoxDecoration(
                                  color: Colors.grey[200],
                                  borderRadius: BorderRadius.circular(12),
                                ),
                                child: Text(
                                  'Inactive',
                                  style: TextStyle(
                                    fontSize: 10,
                                    color: Colors.grey[600],
                                    fontWeight: FontWeight.w500,
                                  ),
                                ),
                              ),
                          ],
                        ),
                        const SizedBox(height: 4),
                        Text(
                          group['description'],
                          style: TextStyle(
                            fontSize: 14,
                            color: Colors.grey[600],
                          ),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ],
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 16),

              // Group Stats
              Row(
                children: [
                  Expanded(
                    child: _buildStatItem(
                      Icons.people,
                      '${group['memberCount']} members',
                      Colors.blue[600]!,
                    ),
                  ),
                  Expanded(
                    child: _buildStatItem(
                      Icons.receipt,
                      '₹${group['totalExpenses'].toStringAsFixed(0)}',
                      Colors.orange[600]!,
                    ),
                  ),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.end,
                      children: [
                        Text(
                          isSettled
                              ? 'Settled'
                              : isOwed
                                  ? 'You get'
                                  : 'You owe',
                          style: TextStyle(
                            fontSize: 12,
                            color: Colors.grey[600],
                          ),
                        ),
                        if (!isSettled)
                          Text(
                            '₹${balance.abs().toStringAsFixed(0)}',
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                              color: balanceColor,
                            ),
                          )
                        else
                          Icon(
                            Icons.check_circle,
                            color: Colors.green[600],
                            size: 20,
                          ),
                      ],
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 12),

              // Last Activity
              Text(
                'Last activity: ${group['lastActivity']}',
                style: TextStyle(
                  fontSize: 12,
                  color: Colors.grey[500],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildStatItem(IconData icon, String text, Color color) {
    return Row(
      children: [
        Icon(icon, size: 16, color: color),
        const SizedBox(width: 4),
        Expanded(
          child: Text(
            text,
            style: TextStyle(
              fontSize: 12,
              color: Colors.grey[700],
              fontWeight: FontWeight.w500,
            ),
            overflow: TextOverflow.ellipsis,
          ),
        ),
      ],
    );
  }

  List<Map<String, dynamic>> _getFilteredGroups() {
    switch (_selectedFilter) {
      case 'active':
        return _groups.where((group) => group['isActive'] == true).toList();
      case 'settled':
        return _groups.where((group) => group['userBalance'] == 0).toList();
      case 'i_owe':
        return _groups.where((group) => (group['userBalance'] as double) < 0).toList();
      case 'im_owed':
        return _groups.where((group) => (group['userBalance'] as double) > 0).toList();
      default:
        return _groups;
    }
  }

  String _getNetBalanceText() {
    final totalBalance = _groups.fold<double>(
      0.0,
      (sum, group) => sum + (group['userBalance'] as double),
    );

    if (totalBalance == 0) return '₹0';
    return totalBalance > 0 ? '+₹${totalBalance.toStringAsFixed(0)}' : '₹${totalBalance.toStringAsFixed(0)}';
  }

  IconData _getCategoryIcon(String category) {
    switch (category.toLowerCase()) {
      case 'travel':
        return Icons.flight;
      case 'food':
        return Icons.restaurant;
      case 'housing':
        return Icons.home;
      case 'entertainment':
        return Icons.movie;
      case 'celebration':
        return Icons.celebration;
      default:
        return Icons.group;
    }
  }

  Color _getCategoryColor(String category) {
    switch (category.toLowerCase()) {
      case 'travel':
        return Colors.blue;
      case 'food':
        return Colors.orange;
      case 'housing':
        return Colors.green;
      case 'entertainment':
        return Colors.purple;
      case 'celebration':
        return Colors.pink;
      default:
        return Colors.teal;
    }
  }

  void _showSearchDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Search Groups'),
        content: const TextField(
          decoration: InputDecoration(
            hintText: 'Enter group name...',
            prefixIcon: Icon(Icons.search),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Search'),
          ),
        ],
      ),
    );
  }
}
