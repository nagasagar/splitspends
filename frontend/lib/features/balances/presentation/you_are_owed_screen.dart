import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

class YouAreOwedScreen extends ConsumerWidget {
  const YouAreOwedScreen({super.key});

  // Mock data for people who owe you money
  final List<Map<String, dynamic>> _owedData = const [
    {
      'userId': '1',
      'name': 'Sarah Wilson',
      'email': 'sarah@example.com',
      'amount': 850.0,
      'expenseCount': 2,
      'lastExpense': 'Uber to Airport',
      'groupName': 'Trip to Goa',
      'profileColor': Colors.pink,
      'lastActivity': '2 days ago',
    },
    {
      'userId': '2',
      'name': 'Mike Johnson',
      'email': 'mike@example.com',
      'amount': 425.0,
      'expenseCount': 1,
      'lastExpense': 'Birthday Party',
      'groupName': 'Birthday Party',
      'profileColor': Colors.indigo,
      'lastActivity': '1 week ago',
    },
    {
      'userId': '3',
      'name': 'Emma Davis',
      'email': 'emma@example.com',
      'amount': 150.0,
      'expenseCount': 3,
      'lastExpense': 'Coffee Shop',
      'groupName': 'Office Lunch',
      'profileColor': Colors.teal,
      'lastActivity': 'Yesterday',
    },
    {
      'userId': '4',
      'name': 'David Brown',
      'email': 'david@example.com',
      'amount': 75.0,
      'expenseCount': 1,
      'lastExpense': 'Grocery Shopping',
      'groupName': null,
      'profileColor': Colors.deepOrange,
      'lastActivity': '3 days ago',
    },
  ];

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final totalOwed = _owedData.fold<double>(0.0, (sum, item) => sum + item['amount']);

    return Scaffold(
      appBar: AppBar(
        title: const Text('You Are Owed'),
        backgroundColor: Colors.green[600],
        foregroundColor: Colors.white,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.pop(),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.send),
            onPressed: () => _showRemindAllDialog(context),
          ),
        ],
      ),
      body: Column(
        children: [
          // Total Owed Card
          Container(
            width: double.infinity,
            margin: const EdgeInsets.all(16),
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [Colors.green[400]!, Colors.green[600]!],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              borderRadius: BorderRadius.circular(16),
              boxShadow: [
                BoxShadow(
                  color: Colors.green.withOpacity(0.3),
                  spreadRadius: 2,
                  blurRadius: 8,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: Column(
              children: [
                Row(
                  children: [
                    const Icon(
                      Icons.trending_up,
                      color: Colors.white,
                      size: 24,
                    ),
                    const SizedBox(width: 12),
                    const Text(
                      'Total Amount You Are Owed',
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 16,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                Text(
                  '₹${totalOwed.toStringAsFixed(2)}',
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 36,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  'from ${_owedData.length} people',
                  style: const TextStyle(
                    color: Colors.white70,
                    fontSize: 14,
                  ),
                ),
              ],
            ),
          ),

          // Quick Actions
          Container(
            margin: const EdgeInsets.symmetric(horizontal: 16),
            child: Row(
              children: [
                Expanded(
                  child: _buildQuickAction(
                    'Remind All',
                    Icons.notifications,
                    Colors.orange[600]!,
                    () => _showRemindAllDialog(context),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: _buildQuickAction(
                    'Export List',
                    Icons.download,
                    Colors.blue[600]!,
                    () => _exportList(context),
                  ),
                ),
              ],
            ),
          ),

          const SizedBox(height: 16),

          // People List
          Expanded(
            child: ListView.builder(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              itemCount: _owedData.length,
              itemBuilder: (context, index) {
                final person = _owedData[index];
                return _buildPersonCard(context, person);
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildQuickAction(String title, IconData icon, Color color, VoidCallback onTap) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(8),
      child: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: color.withOpacity(0.1),
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: color.withOpacity(0.3)),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, color: color, size: 18),
            const SizedBox(width: 8),
            Text(
              title,
              style: TextStyle(
                color: color,
                fontSize: 14,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildPersonCard(BuildContext context, Map<String, dynamic> person) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 3,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            Row(
              children: [
                // Profile Avatar
                CircleAvatar(
                  radius: 28,
                  backgroundColor: person['profileColor'],
                  child: Text(
                    person['name'][0].toUpperCase(),
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
                const SizedBox(width: 16),

                // Person Info
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        person['name'],
                        style: const TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        person['email'],
                        style: TextStyle(
                          fontSize: 14,
                          color: Colors.grey[600],
                        ),
                      ),
                      if (person['groupName'] != null) ...[
                        const SizedBox(height: 4),
                        Row(
                          children: [
                            Icon(
                              Icons.group,
                              size: 14,
                              color: Colors.grey[500],
                            ),
                            const SizedBox(width: 4),
                            Text(
                              person['groupName'],
                              style: TextStyle(
                                fontSize: 12,
                                color: Colors.grey[500],
                              ),
                            ),
                          ],
                        ),
                      ],
                    ],
                  ),
                ),

                // Amount Owed
                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      '₹${person['amount'].toStringAsFixed(2)}',
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                        color: Colors.green[600],
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      '${person['expenseCount']} expense${person['expenseCount'] > 1 ? 's' : ''}',
                      style: TextStyle(
                        fontSize: 12,
                        color: Colors.grey[500],
                      ),
                    ),
                  ],
                ),
              ],
            ),

            const SizedBox(height: 16),

            // Last Expense Info
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.green[50],
                borderRadius: BorderRadius.circular(8),
              ),
              child: Row(
                children: [
                  Icon(
                    Icons.receipt_long,
                    size: 16,
                    color: Colors.green[600],
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Last: ${person['lastExpense']}',
                          style: TextStyle(
                            fontSize: 13,
                            color: Colors.grey[700],
                            fontWeight: FontWeight.w500,
                          ),
                          overflow: TextOverflow.ellipsis,
                        ),
                        Text(
                          'Activity: ${person['lastActivity']}',
                          style: TextStyle(
                            fontSize: 11,
                            color: Colors.grey[500],
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 12),

            // Action Buttons
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    onPressed: () => _showPersonDetails(context, person),
                    icon: const Icon(Icons.info_outline, size: 16),
                    label: const Text('Details'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: Colors.blue[600],
                      side: BorderSide(color: Colors.blue[200]!),
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: () => _sendReminder(context, person),
                    icon: const Icon(Icons.send, size: 16),
                    label: const Text('Remind'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.orange[600],
                      foregroundColor: Colors.white,
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: () => _markAsReceived(context, person),
                    icon: const Icon(Icons.check, size: 16),
                    label: const Text('Received'),
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.green[600],
                      foregroundColor: Colors.white,
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  void _showPersonDetails(BuildContext context, Map<String, dynamic> person) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Details - ${person['name']}'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildDetailRow('Total Amount', '₹${person['amount'].toStringAsFixed(2)}'),
            _buildDetailRow('Number of Expenses', '${person['expenseCount']}'),
            if (person['groupName'] != null)
              _buildDetailRow('Group', person['groupName']),
            _buildDetailRow('Last Expense', person['lastExpense']),
            _buildDetailRow('Last Activity', person['lastActivity']),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Close'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.of(context).pop();
              _sendReminder(context, person);
            },
            child: const Text('Send Reminder'),
          ),
        ],
      ),
    );
  }

  Widget _buildDetailRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 120,
            child: Text(
              '$label:',
              style: const TextStyle(fontWeight: FontWeight.w500),
            ),
          ),
          Expanded(
            child: Text(value),
          ),
        ],
      ),
    );
  }

  void _sendReminder(BuildContext context, Map<String, dynamic> person) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Reminder sent to ${person['name']}'),
        backgroundColor: Colors.orange[600],
        behavior: SnackBarBehavior.floating,
        action: SnackBarAction(
          label: 'Undo',
          textColor: Colors.white,
          onPressed: () {},
        ),
      ),
    );
  }

  void _markAsReceived(BuildContext context, Map<String, dynamic> person) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Mark as Received'),
        content: Text(
          'Mark ₹${person['amount'].toStringAsFixed(2)} from ${person['name']} as received?',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.of(context).pop();
              _confirmReceived(context, person);
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.green[600],
            ),
            child: const Text('Mark Received'),
          ),
        ],
      ),
    );
  }

  void _confirmReceived(BuildContext context, Map<String, dynamic> person) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('₹${person['amount'].toStringAsFixed(2)} marked as received from ${person['name']}'),
        backgroundColor: Colors.green[600],
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  void _showRemindAllDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Send Reminder to All'),
        content: Text(
          'Send payment reminders to all ${_owedData.length} people who owe you money?',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.of(context).pop();
              _remindAll(context);
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.orange[600],
            ),
            child: const Text('Send All'),
          ),
        ],
      ),
    );
  }

  void _remindAll(BuildContext context) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text('Reminders sent to ${_owedData.length} people'),
        backgroundColor: Colors.orange[600],
        behavior: SnackBarBehavior.floating,
      ),
    );
  }

  void _exportList(BuildContext context) {
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text('Exporting list... Feature coming soon!'),
        behavior: SnackBarBehavior.floating,
      ),
    );
  }
}
