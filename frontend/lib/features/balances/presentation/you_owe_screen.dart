import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

class YouOweScreen extends ConsumerWidget {
  const YouOweScreen({super.key});

  // Mock data for people you owe money to
  final List<Map<String, dynamic>> _oweData = const [
    {
      'userId': '1',
      'name': 'John Doe',
      'email': 'john@example.com',
      'amount': 2400.0,
      'expenseCount': 3,
      'lastExpense': 'Dinner at Restaurant',
      'groupName': 'Trip to Goa',
      'profileColor': Colors.blue,
    },
    {
      'userId': '2',
      'name': 'Jane Smith',
      'email': 'jane@example.com',
      'amount': 1500.0,
      'expenseCount': 2,
      'lastExpense': 'Hotel Booking',
      'groupName': 'Trip to Goa',
      'profileColor': Colors.purple,
    },
    {
      'userId': '3',
      'name': 'Bob Wilson',
      'email': 'bob@example.com',
      'amount': 300.0,
      'expenseCount': 1,
      'lastExpense': 'Movie Tickets',
      'groupName': null,
      'profileColor': Colors.green,
    },
    {
      'userId': '4',
      'name': 'Alice Cooper',
      'email': 'alice@example.com',
      'amount': 125.0,
      'expenseCount': 1,
      'lastExpense': 'Coffee Shop',
      'groupName': 'Office Lunch',
      'profileColor': Colors.orange,
    },
  ];

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final totalOwed = _oweData.fold<double>(0.0, (sum, item) => sum + item['amount']);

    return Scaffold(
      appBar: AppBar(
        title: const Text('You Owe'),
        backgroundColor: Colors.red[600],
        foregroundColor: Colors.white,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.pop(),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.payment),
            onPressed: () => _showSettleAllDialog(context),
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
                colors: [Colors.red[400]!, Colors.red[600]!],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              borderRadius: BorderRadius.circular(16),
              boxShadow: [
                BoxShadow(
                  color: Colors.red.withOpacity(0.3),
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
                      Icons.account_balance_wallet,
                      color: Colors.white,
                      size: 24,
                    ),
                    const SizedBox(width: 12),
                    const Text(
                      'Total Amount You Owe',
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
                  'to ${_oweData.length} people',
                  style: const TextStyle(
                    color: Colors.white70,
                    fontSize: 14,
                  ),
                ),
              ],
            ),
          ),

          // People List
          Expanded(
            child: ListView.builder(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              itemCount: _oweData.length,
              itemBuilder: (context, index) {
                final person = _oweData[index];
                return _buildPersonCard(context, person);
              },
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.push('/settle-up'),
        backgroundColor: Colors.green[600],
        icon: const Icon(Icons.payment, color: Colors.white),
        label: const Text(
          'Settle Up',
          style: TextStyle(color: Colors.white),
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
                        color: Colors.red[600],
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
                color: Colors.grey[50],
                borderRadius: BorderRadius.circular(8),
              ),
              child: Row(
                children: [
                  Icon(
                    Icons.receipt_long,
                    size: 16,
                    color: Colors.grey[600],
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      'Last: ${person['lastExpense']}',
                      style: TextStyle(
                        fontSize: 13,
                        color: Colors.grey[700],
                      ),
                      overflow: TextOverflow.ellipsis,
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
                    onPressed: () => _showPaymentDetails(context, person),
                    icon: const Icon(Icons.info_outline, size: 16),
                    label: const Text('Details'),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: Colors.blue[600],
                      side: BorderSide(color: Colors.blue[200]!),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: () => _settleWithPerson(context, person),
                    icon: const Icon(Icons.payment, size: 16),
                    label: const Text('Settle'),
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

  void _showPaymentDetails(BuildContext context, Map<String, dynamic> person) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text('Payment Details - ${person['name']}'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildDetailRow('Total Amount', '₹${person['amount'].toStringAsFixed(2)}'),
            _buildDetailRow('Number of Expenses', '${person['expenseCount']}'),
            if (person['groupName'] != null)
              _buildDetailRow('Group', person['groupName']),
            _buildDetailRow('Last Expense', person['lastExpense']),
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
              _settleWithPerson(context, person);
            },
            child: const Text('Settle Now'),
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

  void _settleWithPerson(BuildContext context, Map<String, dynamic> person) {
    context.push('/settle-up', extra: {
      'preselectedPerson': person,
      'amount': person['amount'],
    });
  }

  void _showSettleAllDialog(BuildContext context) {
    final totalAmount = _oweData.fold<double>(0.0, (sum, item) => sum + item['amount']);
    
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Settle All'),
        content: Text(
          'Are you sure you want to settle all debts totaling ₹${totalAmount.toStringAsFixed(2)}?',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Cancel'),
          ),
          ElevatedButton(
            onPressed: () {
              Navigator.of(context).pop();
              _settleAll(context);
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.green[600],
            ),
            child: const Text('Settle All'),
          ),
        ],
      ),
    );
  }

  void _settleAll(BuildContext context) {
    // Show loading
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => const Center(
        child: CircularProgressIndicator(),
      ),
    );

    // Simulate API call
    Future.delayed(const Duration(seconds: 2), () {
      if (!context.mounted) return;
      
      Navigator.of(context).pop(); // Remove loading
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: const Text('All settlements recorded successfully!'),
          backgroundColor: Colors.green[600],
          behavior: SnackBarBehavior.floating,
        ),
      );
      context.go('/dashboard');
    });
  }
}
