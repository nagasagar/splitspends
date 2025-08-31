// ignore_for_file: deprecated_member_use

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

class ExpenseDetailScreen extends ConsumerWidget {
  final String expenseId;

  const ExpenseDetailScreen({
    super.key,
    required this.expenseId,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // Mock expense data
    final expense = _getMockExpense(expenseId);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Expense Details'),
        backgroundColor: Colors.purple[600],
        foregroundColor: Colors.white,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.pop(),
        ),
        actions: [
          PopupMenuButton<String>(
            onSelected: (value) => _handleMenuAction(context, value),
            itemBuilder: (context) => [
              const PopupMenuItem(
                value: 'edit',
                child: ListTile(
                  leading: Icon(Icons.edit),
                  title: Text('Edit Expense'),
                ),
              ),
              const PopupMenuItem(
                value: 'delete',
                child: ListTile(
                  leading: Icon(Icons.delete, color: Colors.red),
                  title: Text('Delete Expense', style: TextStyle(color: Colors.red)),
                ),
              ),
              const PopupMenuItem(
                value: 'share',
                child: ListTile(
                  leading: Icon(Icons.share),
                  title: Text('Share Details'),
                ),
              ),
            ],
          ),
        ],
      ),
      body: SingleChildScrollView(
        child: Column(
          children: [
            // Expense Header Card
            Container(
              width: double.infinity,
              margin: const EdgeInsets.all(16),
              child: Card(
                elevation: 4,
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.all(12),
                            decoration: BoxDecoration(
                              color: Colors.purple[100],
                              borderRadius: BorderRadius.circular(12),
                            ),
                            child: Icon(
                              _getCategoryIcon(expense['category']),
                              color: Colors.purple[600],
                              size: 24,
                            ),
                          ),
                          const SizedBox(width: 16),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  expense['description'],
                                  style: const TextStyle(
                                    fontSize: 20,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                                const SizedBox(height: 4),
                                Text(
                                  expense['category'],
                                  style: TextStyle(
                                    fontSize: 14,
                                    color: Colors.grey[600],
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 20),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                'Total Amount',
                                style: TextStyle(
                                  fontSize: 12,
                                  color: Colors.grey[600],
                                ),
                              ),
                              Text(
                                '₹${expense['amount'].toStringAsFixed(2)}',
                                style: const TextStyle(
                                  fontSize: 24,
                                  fontWeight: FontWeight.bold,
                                  color: Colors.black87,
                                ),
                              ),
                            ],
                          ),
                          Column(
                            crossAxisAlignment: CrossAxisAlignment.end,
                            children: [
                              Text(
                                'Your Share',
                                style: TextStyle(
                                  fontSize: 12,
                                  color: Colors.grey[600],
                                ),
                              ),
                              Text(
                                '₹${expense['userShare'].toStringAsFixed(2)}',
                                style: TextStyle(
                                  fontSize: 20,
                                  fontWeight: FontWeight.bold,
                                  color: expense['userShare'] > 0 ? Colors.red[600] : Colors.green[600],
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ),

            // Expense Info Card
            _buildInfoCard(context, expense),

            // Splits Card
            _buildSplitsCard(context, expense),

            // Recent Activity Card (if any)
            _buildActivityCard(context, expense),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => _showSettleUpDialog(context, expense),
        backgroundColor: Colors.green[600],
        icon: const Icon(Icons.payment, color: Colors.white),
        label: const Text(
          'Settle Up',
          style: TextStyle(color: Colors.white),
        ),
      ),
    );
  }

  Widget _buildInfoCard(BuildContext context, Map<String, dynamic> expense) {
    return Container(
      width: double.infinity,
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Expense Information',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 16),
              _buildInfoRow('Paid by', expense['paidBy'], Icons.person),
              _buildInfoRow('Date', expense['date'], Icons.calendar_today),
              _buildInfoRow('Group', expense['groupName'], Icons.group),
              if (expense['notes'] != null && expense['notes'].isNotEmpty)
                _buildInfoRow('Notes', expense['notes'], Icons.note),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildInfoRow(String label, String value, IconData icon) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        children: [
          Icon(icon, size: 20, color: Colors.grey[600]),
          const SizedBox(width: 12),
          Text(
            '$label: ',
            style: TextStyle(
              fontSize: 14,
              color: Colors.grey[600],
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: const TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSplitsCard(BuildContext context, Map<String, dynamic> expense) {
    final splits = expense['splits'] as List<Map<String, dynamic>>;
    final splitType = expense['splitType'] ?? 'equal';
    final totalAmount = expense['amount'] as double;
    
    return Container(
      width: double.infinity,
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'How it was split',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: _getSplitTypeColor(splitType).withOpacity(0.1),
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(
                        color: _getSplitTypeColor(splitType).withOpacity(0.3),
                      ),
                    ),
                    child: Text(
                      _getSplitTypeLabel(splitType),
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.w600,
                        color: _getSplitTypeColor(splitType),
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              ...splits.map((split) => _buildSplitItem(split, splitType, totalAmount)),
              
              // Total verification row
              const Divider(height: 24),
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  const Text(
                    'Total',
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.bold,
                      color: Colors.black87,
                    ),
                  ),
                  Text(
                    '₹${totalAmount.toStringAsFixed(2)}',
                    style: const TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.bold,
                      color: Colors.black87,
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

  Widget _buildSplitItem(Map<String, dynamic> split, String splitType, double totalAmount) {
    final isCurrentUser = split['isCurrentUser'] ?? false;
    final amount = split['amount'] as double;
    final percentage = split['percentage'] as double? ?? (amount / totalAmount * 100);
    
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: isCurrentUser ? Colors.blue[50] : Colors.grey[50],
        borderRadius: BorderRadius.circular(8),
        border: Border.all(
          color: isCurrentUser ? Colors.blue[200]! : Colors.grey[200]!,
        ),
      ),
      child: Row(
        children: [
          CircleAvatar(
            radius: 20,
            backgroundColor: isCurrentUser ? Colors.blue[100] : Colors.grey[300],
            child: Text(
              split['userName'][0].toUpperCase(),
              style: TextStyle(
                fontWeight: FontWeight.bold,
                color: isCurrentUser ? Colors.blue[800] : Colors.grey[700],
              ),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  split['userName'],
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                if (split['isPayer'])
                  Text(
                    'Paid the expense',
                    style: TextStyle(
                      fontSize: 12,
                      color: Colors.green[600],
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                // Show split details based on type
                if (splitType == 'percentage')
                  Text(
                    '${percentage.toStringAsFixed(1)}% of total',
                    style: TextStyle(
                      fontSize: 12,
                      color: Colors.grey[600],
                    ),
                  )
                else if (splitType == 'custom')
                  Text(
                    'Custom amount',
                    style: TextStyle(
                      fontSize: 12,
                      color: Colors.grey[600],
                    ),
                  )
                else
                  Text(
                    'Equal split',
                    style: TextStyle(
                      fontSize: 12,
                      color: Colors.grey[600],
                    ),
                  ),
              ],
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                '₹${amount.toStringAsFixed(2)}',
                style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                ),
              ),
              if (splitType == 'percentage')
                Text(
                  '${percentage.toStringAsFixed(1)}%',
                  style: TextStyle(
                    fontSize: 12,
                    color: Colors.grey[600],
                    fontWeight: FontWeight.w500,
                  ),
                ),
              if (split['owesAmount'] != null && split['owesAmount'] != 0)
                Text(
                  split['owesAmount'] > 0 ? 'owes' : 'gets back',
                  style: TextStyle(
                    fontSize: 12,
                    color: split['owesAmount'] > 0 ? Colors.red[600] : Colors.green[600],
                  ),
                ),
            ],
          ),
        ],
      ),
    );
  }

  Color _getSplitTypeColor(String splitType) {
    switch (splitType) {
      case 'equal':
        return Colors.blue;
      case 'percentage':
        return Colors.orange;
      case 'custom':
        return Colors.purple;
      default:
        return Colors.grey;
    }
  }

  String _getSplitTypeLabel(String splitType) {
    switch (splitType) {
      case 'equal':
        return 'Split Equally';
      case 'percentage':
        return 'Split by %';
      case 'custom':
        return 'Custom Split';
      default:
        return 'Split';
    }
  }

  Widget _buildActivityCard(BuildContext context, Map<String, dynamic> expense) {
    return Container(
      width: double.infinity,
      margin: const EdgeInsets.all(16),
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Recent Activity',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 16),
              _buildActivityItem(
                'Expense created',
                'by ${expense['paidBy']}',
                expense['date'],
                Icons.add_circle,
                Colors.green,
              ),
              _buildActivityItem(
                'Added to group',
                expense['groupName'],
                expense['date'],
                Icons.group_add,
                Colors.blue,
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildActivityItem(String title, String subtitle, String time, IconData icon, Color color) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: color.withOpacity(0.1),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Icon(icon, color: color, size: 16),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                Text(
                  subtitle,
                  style: TextStyle(
                    fontSize: 12,
                    color: Colors.grey[600],
                  ),
                ),
              ],
            ),
          ),
          Text(
            time,
            style: TextStyle(
              fontSize: 12,
              color: Colors.grey[600],
            ),
          ),
        ],
      ),
    );
  }

  void _handleMenuAction(BuildContext context, String action) {
    switch (action) {
      case 'edit':
        context.push('/expenses/$expenseId/edit');
        break;
      case 'delete':
        _showDeleteConfirmation(context);
        break;
      case 'share':
        _shareExpenseDetails(context);
        break;
    }
  }

  void _showDeleteConfirmation(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Expense'),
        content: const Text('Are you sure you want to delete this expense? This action cannot be undone.'),
        actions: [
          TextButton(
            onPressed: () => context.pop(),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () {
              context.pop();
              _deleteExpense(context);
            },
            style: TextButton.styleFrom(foregroundColor: Colors.red),
            child: const Text('Delete'),
          ),
        ],
      ),
    );
  }

  void _deleteExpense(BuildContext context) {
    // Show loading
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => const Center(child: CircularProgressIndicator()),
    );

    // Simulate API call
    Future.delayed(const Duration(seconds: 1), () {
      if (!context.mounted) return;
      
      Navigator.of(context).pop(); // Remove loading
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Expense deleted successfully'),
          backgroundColor: Colors.red,
        ),
      );
      context.go('/dashboard');
    });
  }

  void _shareExpenseDetails(BuildContext context) {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Sharing functionality will be implemented'),
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  void _showSettleUpDialog(BuildContext context, Map<String, dynamic> expense) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Settle Up'),
        content: const Text('Would you like to record a settlement for this expense?'),
        actions: [
          TextButton(
            onPressed: () => context.pop(),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              context.pop();
              context.push('/settle-up', extra: {'expenseId': expenseId});
            },
            child: const Text('Settle Up'),
          ),
        ],
      ),
    );
  }

  IconData _getCategoryIcon(String category) {
    switch (category.toLowerCase()) {
      case 'food':
        return Icons.restaurant;
      case 'transport':
        return Icons.directions_car;
      case 'entertainment':
        return Icons.movie;
      case 'shopping':
        return Icons.shopping_bag;
      case 'utilities':
        return Icons.home;
      case 'travel':
        return Icons.flight;
      default:
        return Icons.receipt;
    }
  }

  Map<String, dynamic> _getMockExpense(String expenseId) {
    // Mock data - in real app, this would come from provider/repository
    
    // Different expense scenarios based on ID
    if (expenseId == 'expense_1') {
      return {
        'id': expenseId,
        'description': 'Dinner at Restaurant',
        'category': 'Food',
        'amount': 1200.0,
        'userShare': 400.0,
        'paidBy': 'John Doe',
        'date': 'Today, 7:30 PM',
        'groupName': 'Trip to Goa',
        'notes': 'Great dinner with friends',
        'splitType': 'equal',
        'splits': [
          {
            'userName': 'John Doe',
            'amount': 400.0,
            'percentage': 33.33,
            'isPayer': true,
            'isCurrentUser': false,
            'owesAmount': 0.0,
          },
          {
            'userName': 'You',
            'amount': 400.0,
            'percentage': 33.33,
            'isPayer': false,
            'isCurrentUser': true,
            'owesAmount': 400.0,
          },
          {
            'userName': 'Jane Smith',
            'amount': 400.0,
            'percentage': 33.33,
            'isPayer': false,
            'isCurrentUser': false,
            'owesAmount': 400.0,
          },
        ],
      };
    } else if (expenseId == 'expense_2') {
      return {
        'id': expenseId,
        'description': 'Movie Tickets',
        'category': 'Entertainment',
        'amount': 900.0,
        'userShare': 200.0,
        'paidBy': 'You',
        'date': 'Yesterday, 9:00 PM',
        'groupName': 'Office Lunch',
        'notes': 'Watched Avengers',
        'splitType': 'custom',
        'splits': [
          {
            'userName': 'You',
            'amount': 200.0,
            'customAmount': 200.0,
            'isPayer': true,
            'isCurrentUser': true,
            'owesAmount': 0.0,
          },
          {
            'userName': 'Alice Johnson',
            'amount': 250.0,
            'customAmount': 250.0,
            'isPayer': false,
            'isCurrentUser': false,
            'owesAmount': 250.0,
          },
          {
            'userName': 'Bob Wilson',
            'amount': 300.0,
            'customAmount': 300.0,
            'isPayer': false,
            'isCurrentUser': false,
            'owesAmount': 300.0,
          },
          {
            'userName': 'Carol Davis',
            'amount': 150.0,
            'customAmount': 150.0,
            'isPayer': false,
            'isCurrentUser': false,
            'owesAmount': 150.0,
          },
        ],
      };
    } else if (expenseId == 'expense_3') {
      return {
        'id': expenseId,
        'description': 'Apartment Rent',
        'category': 'Utilities',
        'amount': 3000.0,
        'userShare': 900.0,
        'paidBy': 'Sarah Connor',
        'date': '2 days ago, 10:00 AM',
        'groupName': 'Roommates',
        'notes': 'Monthly rent payment',
        'splitType': 'percentage',
        'splits': [
          {
            'userName': 'Sarah Connor',
            'amount': 1500.0,
            'percentage': 50.0,
            'isPayer': true,
            'isCurrentUser': false,
            'owesAmount': 0.0,
          },
          {
            'userName': 'You',
            'amount': 900.0,
            'percentage': 30.0,
            'isPayer': false,
            'isCurrentUser': true,
            'owesAmount': 900.0,
          },
          {
            'userName': 'Mike Taylor',
            'amount': 600.0,
            'percentage': 20.0,
            'isPayer': false,
            'isCurrentUser': false,
            'owesAmount': 600.0,
          },
        ],
      };
    }
    
    // Default expense (equal split)
    return {
      'id': expenseId,
      'description': 'Dinner at Restaurant',
      'category': 'Food',
      'amount': 1200.0,
      'userShare': 400.0,
      'paidBy': 'John Doe',
      'date': 'Today, 7:30 PM',
      'groupName': 'Trip to Goa',
      'notes': 'Great dinner with friends',
      'splitType': 'equal',
      'splits': [
        {
          'userName': 'John Doe',
          'amount': 400.0,
          'percentage': 33.33,
          'isPayer': true,
          'isCurrentUser': false,
          'owesAmount': 0.0,
        },
        {
          'userName': 'You',
          'amount': 400.0,
          'percentage': 33.33,
          'isPayer': false,
          'isCurrentUser': true,
          'owesAmount': 400.0,
        },
        {
          'userName': 'Jane Smith',
          'amount': 400.0,
          'percentage': 33.33,
          'isPayer': false,
          'isCurrentUser': false,
          'owesAmount': 400.0,
        },
      ],
    };
  }
}
