import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

class SettleUpScreen extends ConsumerStatefulWidget {
  final String? groupId;

  const SettleUpScreen({
    super.key,
    this.groupId,
  });

  @override
  ConsumerState<SettleUpScreen> createState() => _SettleUpScreenState();
}

class _SettleUpScreenState extends ConsumerState<SettleUpScreen> {
  final _formKey = GlobalKey<FormState>();
  final _amountController = TextEditingController();
  
  String? _selectedFromUser;
  String? _selectedToUser;
  String? _selectedGroup;
  String _settlementMethod = 'cash';

  // Mock data for demo
  final List<Map<String, String>> _users = [
    {'id': '1', 'name': 'John Doe', 'email': 'john@example.com'},
    {'id': '2', 'name': 'Jane Smith', 'email': 'jane@example.com'},
    {'id': '3', 'name': 'Bob Wilson', 'email': 'bob@example.com'},
  ];

  final List<Map<String, String>> _groups = [
    {'id': '1', 'name': 'Trip to Goa'},
    {'id': '2', 'name': 'Office Lunch'},
    {'id': '3', 'name': 'Apartment Rent'},
  ];

  @override
  void initState() {
    super.initState();
    if (widget.groupId != null) {
      _selectedGroup = widget.groupId;
    }
  }

  @override
  void dispose() {
    _amountController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Settle Up'),
        backgroundColor: Colors.green[600],
        foregroundColor: Colors.white,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => context.pop(),
        ),
      ),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // Settlement Info Card
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(Icons.account_balance_wallet, color: Colors.green[600]),
                        const SizedBox(width: 8),
                        const Text(
                          'Settlement Details',
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    
                    // Amount Field
                    TextFormField(
                      controller: _amountController,
                      keyboardType: const TextInputType.numberWithOptions(decimal: true),
                      decoration: InputDecoration(
                        labelText: 'Amount *',
                        prefixIcon: const Icon(Icons.currency_rupee),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                        ),
                        hintText: 'Enter settlement amount',
                      ),
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please enter an amount';
                        }
                        final amount = double.tryParse(value);
                        if (amount == null || amount <= 0) {
                          return 'Please enter a valid amount';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),

                    // From User Dropdown
                    DropdownButtonFormField<String>(
                      value: _selectedFromUser,
                      decoration: InputDecoration(
                        labelText: 'From (Payer) *',
                        prefixIcon: const Icon(Icons.person_remove),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                        ),
                      ),
                      items: _users.map((user) {
                        return DropdownMenuItem<String>(
                          value: user['id'],
                          child: Text(user['name']!),
                        );
                      }).toList(),
                      onChanged: (value) {
                        setState(() {
                          _selectedFromUser = value;
                        });
                      },
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please select who is paying';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),

                    // To User Dropdown
                    DropdownButtonFormField<String>(
                      value: _selectedToUser,
                      decoration: InputDecoration(
                        labelText: 'To (Receiver) *',
                        prefixIcon: const Icon(Icons.person_add),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                        ),
                      ),
                      items: _users.where((user) => user['id'] != _selectedFromUser).map((user) {
                        return DropdownMenuItem<String>(
                          value: user['id'],
                          child: Text(user['name']!),
                        );
                      }).toList(),
                      onChanged: (value) {
                        setState(() {
                          _selectedToUser = value;
                        });
                      },
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Please select who is receiving';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),

                    // Group Dropdown (Optional)
                    DropdownButtonFormField<String>(
                      value: _selectedGroup,
                      decoration: InputDecoration(
                        labelText: 'Group (Optional)',
                        prefixIcon: const Icon(Icons.group),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                        ),
                      ),
                      items: [
                        const DropdownMenuItem<String>(
                          value: null,
                          child: Text('No Group'),
                        ),
                        ..._groups.map((group) {
                          return DropdownMenuItem<String>(
                            value: group['id'],
                            child: Text(group['name']!),
                          );
                        }),
                      ],
                      onChanged: (value) {
                        setState(() {
                          _selectedGroup = value;
                        });
                      },
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Settlement Method Card
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(Icons.payment, color: Colors.blue[600]),
                        const SizedBox(width: 8),
                        const Text(
                          'Settlement Method',
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    
                    // Payment Method Options
                    Column(
                      children: [
                        RadioListTile<String>(
                          title: const Text('Cash'),
                          subtitle: const Text('Paid in cash'),
                          value: 'cash',
                          groupValue: _settlementMethod,
                          onChanged: (value) {
                            setState(() {
                              _settlementMethod = value!;
                            });
                          },
                        ),
                        RadioListTile<String>(
                          title: const Text('UPI / Digital Payment'),
                          subtitle: const Text('Paid via UPI, PayTM, etc.'),
                          value: 'upi',
                          groupValue: _settlementMethod,
                          onChanged: (value) {
                            setState(() {
                              _settlementMethod = value!;
                            });
                          },
                        ),
                        RadioListTile<String>(
                          title: const Text('Bank Transfer'),
                          subtitle: const Text('Direct bank transfer'),
                          value: 'bank_transfer',
                          groupValue: _settlementMethod,
                          onChanged: (value) {
                            setState(() {
                              _settlementMethod = value!;
                            });
                          },
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),

            // Submit Button
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(
                onPressed: _submitSettlement,
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.green[600],
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 16),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                ),
                child: const Text(
                  'Record Settlement',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _submitSettlement() {
    if (!_formKey.currentState!.validate()) {
      return;
    }

    // Show loading indicator
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => const Center(
        child: CircularProgressIndicator(),
      ),
    );

    // Simulate API call
    Future.delayed(const Duration(seconds: 2), () {
      if (!mounted) return;
      
      Navigator.of(context).pop(); // Remove loading indicator
      
      // Show success message
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: const Text('Settlement recorded successfully!'),
          backgroundColor: Colors.green[600],
          behavior: SnackBarBehavior.floating,
        ),
      );

      // Navigate back to dashboard
      context.go('/dashboard');
    });
  }
}
