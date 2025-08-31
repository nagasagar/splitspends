// ignore_for_file: deprecated_member_use

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../auth/provider/auth_provider.dart';
import '../data/repositories/balance_repository.dart';

class YouAreOwedScreen extends ConsumerStatefulWidget {
  const YouAreOwedScreen({super.key});

  @override
  ConsumerState<YouAreOwedScreen> createState() => _YouAreOwedScreenState();
}

class _YouAreOwedScreenState extends ConsumerState<YouAreOwedScreen> {
  List<Map<String, dynamic>> _owedData = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadBalanceData();
  }

  Future<void> _loadBalanceData() async {
    setState(() {
      _isLoading = true;
    });

    try {
      final authState = ref.read(authProvider);
      if (authState.isAuthenticated) {
        final balanceRepository = ref.read(balanceRepositoryProvider);
        final oweYouData = await balanceRepository.getWhoOwesYou(authState.user!.id.toString());

        setState(() {
          _owedData = oweYouData
              .map((balance) => {
                    'userId': balance['userId'],
                    'name': balance['userName'],
                    'email': balance['userEmail'],
                    'amount': balance['amount'],
                    'expenseCount': balance['expenseCount'] ?? 1,
                    'lastExpense': balance['lastExpense'] ?? 'Recent expense',
                    'groupName': balance['groupName'],
                    'profileColor': _getRandomColor(),
                  })
              .toList();
          _isLoading = false;
        });
      }
    } catch (e) {
      setState(() {
        _owedData = [];
        _isLoading = false;
      });
    }
  }

  Color _getRandomColor() {
    final colors = [Colors.blue, Colors.purple, Colors.green, Colors.orange, Colors.red, Colors.indigo];
    return colors[DateTime.now().millisecond % colors.length];
  }

  @override
  Widget build(BuildContext context) {
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
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _loadBalanceData,
              child: Column(
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
                        const Row(
                          children: [
                            Icon(
                              Icons.trending_up,
                              color: Colors.white,
                              size: 24,
                            ),
                            SizedBox(width: 12),
                            Text(
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

                  // People List
                  Expanded(
                    child: _owedData.isEmpty
                        ? const Center(
                            child: Column(
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Icon(Icons.monetization_on_outlined, size: 64, color: Colors.grey),
                                SizedBox(height: 16),
                                Text(
                                  'Nobody owes you money',
                                  style: TextStyle(fontSize: 18, color: Colors.grey),
                                ),
                                SizedBox(height: 8),
                                Text(
                                  'When people owe you money, it will appear here',
                                  style: TextStyle(color: Colors.grey),
                                ),
                              ],
                            ),
                          )
                        : ListView.builder(
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
            ),
    );
  }

  Widget _buildPersonCard(BuildContext context, Map<String, dynamic> person) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 3,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
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
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(
                  content: Text('Reminders sent to ${_owedData.length} people'),
                  backgroundColor: Colors.orange[600],
                  behavior: SnackBarBehavior.floating,
                ),
              );
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
}
