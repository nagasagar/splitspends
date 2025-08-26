import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/utils/expense_icons.dart';
import '../../data/models/group_models.dart';
import '../providers/groups_provider.dart';

class AddExpenseScreen extends ConsumerStatefulWidget {
  final String? groupId;

  const AddExpenseScreen({
    super.key,
    this.groupId,
  });

  @override
  ConsumerState<AddExpenseScreen> createState() => _AddExpenseScreenState();
}

class _AddExpenseScreenState extends ConsumerState<AddExpenseScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _amountController = TextEditingController();
  String _currency = 'USD';
  String? _selectedPayerId;
  String? _selectedCategory;
  String _splitType = 'equal'; // 'equal', 'percentage', 'custom'
  final Set<String> _splitAmongIds = <String>{};
  final Map<String, double> _customSplits = {}; // For custom amounts
  final Map<String, double> _percentageSplits = {}; // For percentage splits
  bool _isLoading = false;

  GroupDetails? _groupDetails;
  List<GroupDetails> _availableGroups = [];

  @override
  void initState() {
    super.initState();
    _loadGroupDetails();
    _amountController.addListener(() {
      setState(() {}); // Refresh UI when amount changes
    });
  }

  @override
  void dispose() {
    _nameController.dispose();
    _descriptionController.dispose();
    _amountController.dispose();
    super.dispose();
  }

  void _loadGroupDetails() {
    // Create mock groups for demonstration
    _availableGroups = [
      GroupDetails(
        id: '0',
        name: 'Personal Expenses',
        description: 'Personal expenses not tied to a specific group',
        createdAt: DateTime.now(),
        createdById: '1',
        expenses: [],
        members: [
          GroupMember(
            id: '1',
            name: 'You (John Doe)',
            email: 'john.doe@example.com',
            isAdmin: true,
            joinedAt: DateTime.now(),
          ),
        ],
      ),
      GroupDetails(
        id: '1',
        name: 'Trip to Goa',
        description: 'Beach vacation with friends',
        createdAt: DateTime.now().subtract(const Duration(days: 30)),
        createdById: '1',
        expenses: [],
        members: [
          GroupMember(
            id: '1',
            name: 'You (John Doe)',
            email: 'john.doe@example.com',
            isAdmin: true,
            joinedAt: DateTime.now().subtract(const Duration(days: 30)),
          ),
          GroupMember(
            id: '2',
            name: 'Jane Smith',
            email: 'jane.smith@example.com',
            isAdmin: false,
            joinedAt: DateTime.now().subtract(const Duration(days: 25)),
          ),
          GroupMember(
            id: '3',
            name: 'Mike Johnson',
            email: 'mike.johnson@example.com',
            isAdmin: false,
            joinedAt: DateTime.now().subtract(const Duration(days: 20)),
          ),
          GroupMember(
            id: '4',
            name: 'Sarah Wilson',
            email: 'sarah.wilson@example.com',
            isAdmin: false,
            joinedAt: DateTime.now().subtract(const Duration(days: 15)),
          ),
        ],
      ),
      GroupDetails(
        id: '2',
        name: 'Office Lunch',
        description: 'Weekly office lunch group',
        createdAt: DateTime.now().subtract(const Duration(days: 60)),
        createdById: '1',
        expenses: [],
        members: [
          GroupMember(
            id: '1',
            name: 'You (John Doe)',
            email: 'john.doe@example.com',
            isAdmin: true,
            joinedAt: DateTime.now().subtract(const Duration(days: 60)),
          ),
          GroupMember(
            id: '5',
            name: 'Alex Brown',
            email: 'alex.brown@example.com',
            isAdmin: false,
            joinedAt: DateTime.now().subtract(const Duration(days: 55)),
          ),
          GroupMember(
            id: '6',
            name: 'Lisa Garcia',
            email: 'lisa.garcia@example.com',
            isAdmin: false,
            joinedAt: DateTime.now().subtract(const Duration(days: 50)),
          ),
          GroupMember(
            id: '7',
            name: 'Tom Davis',
            email: 'tom.davis@example.com',
            isAdmin: false,
            joinedAt: DateTime.now().subtract(const Duration(days: 45)),
          ),
        ],
      ),
      GroupDetails(
        id: '3',
        name: 'Roommates',
        description: 'Shared apartment expenses',
        createdAt: DateTime.now().subtract(const Duration(days: 90)),
        createdById: '1',
        expenses: [],
        members: [
          GroupMember(
            id: '1',
            name: 'You (John Doe)',
            email: 'john.doe@example.com',
            isAdmin: true,
            joinedAt: DateTime.now().subtract(const Duration(days: 90)),
          ),
          GroupMember(
            id: '8',
            name: 'Chris Lee',
            email: 'chris.lee@example.com',
            isAdmin: false,
            joinedAt: DateTime.now().subtract(const Duration(days: 85)),
          ),
          GroupMember(
            id: '9',
            name: 'Sam Taylor',
            email: 'sam.taylor@example.com',
            isAdmin: false,
            joinedAt: DateTime.now().subtract(const Duration(days: 80)),
          ),
        ],
      ),
    ];

    if (widget.groupId != null && widget.groupId!.isNotEmpty) {
      // Load specific group
      _groupDetails = _availableGroups.firstWhere(
        (group) => group.id == widget.groupId,
        orElse: () => _availableGroups.first,
      );
    } else {
      // Default to personal expenses
      _groupDetails = _availableGroups.first;
    }
    
    if (_groupDetails != null) {
      // Pre-select current user as payer
      _selectedPayerId = _groupDetails!.members.first.id;
      _splitAmongIds.addAll(_groupDetails!.members.map((m) => m.id));
    }
  }

  void _onExpenseNameChanged() {
    // Automatically suggest category based on expense name
    if (_nameController.text.isNotEmpty && _selectedCategory == null) {
      final suggestedCategory = ExpenseIcons.suggestCategory(
        _nameController.text,
        description: _descriptionController.text,
      );
      setState(() {
        _selectedCategory = suggestedCategory;
      });
    }
  }

  ExpenseIconData _getCurrentExpenseIcon() {
    return ExpenseIcons.getExpenseIcon(
      _nameController.text.isEmpty ? 'General Expense' : _nameController.text,
      description: _descriptionController.text,
      category: _selectedCategory,
    );
  }

  void _showCategorySelector(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Choose Category'),
        content: SizedBox(
          width: double.maxFinite,
          child: Wrap(
            spacing: 8,
            runSpacing: 8,
            children: ExpenseIcons.getExpenseCategories().map((category) {
              final isSelected = _selectedCategory == category.id;

              return GestureDetector(
                onTap: () {
                  setState(() {
                    _selectedCategory = category.id;
                  });
                  Navigator.of(context).pop();
                },
                child: Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: isSelected ? category.iconData.color.withOpacity(0.2) : Colors.grey.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(
                      color: isSelected ? category.iconData.color : Colors.grey.withOpacity(0.3),
                      width: isSelected ? 2 : 1,
                    ),
                  ),
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(
                        category.iconData.icon,
                        color: category.iconData.color,
                        size: 24,
                      ),
                      const SizedBox(height: 4),
                      Text(
                        category.name,
                        style: TextStyle(
                          fontSize: 10,
                          fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                          color: isSelected ? category.iconData.color : Colors.black87,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ],
                  ),
                ),
              );
            }).toList(),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () {
              setState(() {
                _selectedCategory = null; // Reset to automatic
              });
              Navigator.of(context).pop();
            },
            child: const Text('Auto Select'),
          ),
        ],
      ),
    );
  }

  void _initializeSplits() {
    final amount = double.tryParse(_amountController.text) ?? 0.0;
    final participantCount = _splitAmongIds.length;

    if (participantCount == 0) return;

    switch (_splitType) {
      case 'equal':
        final equalAmount = amount / participantCount;
        _customSplits.clear();
        _percentageSplits.clear();
        for (final id in _splitAmongIds) {
          _customSplits[id] = equalAmount;
          _percentageSplits[id] = 100.0 / participantCount;
        }
        break;
      case 'custom':
        if (_customSplits.isEmpty) {
          final equalAmount = amount / participantCount;
          for (final id in _splitAmongIds) {
            _customSplits[id] = equalAmount;
          }
        }
        break;
      case 'percentage':
        if (_percentageSplits.isEmpty) {
          final equalPercentage = 100.0 / participantCount;
          for (final id in _splitAmongIds) {
            _percentageSplits[id] = equalPercentage;
          }
        }
        break;
    }
  }

  void _initializeSplitsForNewParticipant(String memberId) {
    final amount = double.tryParse(_amountController.text) ?? 0.0;

    if (_splitType == 'equal') {
      // Equal split is calculated automatically, no initialization needed
    } else if (_splitType == 'custom') {
      // Initialize with equal share of remaining amount
      final currentTotal = _customSplits.values.fold(0.0, (sum, value) => sum + value);
      final remainingAmount = amount - currentTotal;
      final newParticipants = _splitAmongIds.where((id) => !_customSplits.containsKey(id)).length;

      if (newParticipants > 0) {
        _customSplits[memberId] = remainingAmount / newParticipants;
      } else {
        _customSplits[memberId] = amount / _splitAmongIds.length;
      }
    } else if (_splitType == 'percentage') {
      // Initialize with equal percentage of remaining percentage
      final currentTotalPercentage = _percentageSplits.values.fold(0.0, (sum, value) => sum + value);
      final remainingPercentage = 100.0 - currentTotalPercentage;
      final newParticipants = _splitAmongIds.where((id) => !_percentageSplits.containsKey(id)).length;

      if (newParticipants > 0 && remainingPercentage > 0) {
        _percentageSplits[memberId] = remainingPercentage / newParticipants;
      } else {
        _percentageSplits[memberId] = 100.0 / _splitAmongIds.length;
      }
    }
  }

  void _redistributeSplits() {
    final amount = double.tryParse(_amountController.text) ?? 0.0;

    if (_splitType == 'equal') {
      // Equal split is calculated automatically, no redistribution needed
    } else if (_splitType == 'custom') {
      // Redistribute only if current total doesn't match the bill amount
      final currentTotal = _customSplits.values.fold(0.0, (sum, value) => sum + value);
      if ((currentTotal - amount).abs() > 0.01 && _splitAmongIds.isNotEmpty) {
        // Redistribute equally among remaining participants
        final equalShare = amount / _splitAmongIds.length;
        for (String id in _splitAmongIds) {
          _customSplits[id] = equalShare;
        }
      }
    } else if (_splitType == 'percentage') {
      // Redistribute percentages equally among remaining participants
      if (_splitAmongIds.isNotEmpty) {
        final equalPercentage = 100.0 / _splitAmongIds.length;
        for (String id in _splitAmongIds) {
          _percentageSplits[id] = equalPercentage;
        }
      }
    }
  }

  List<Widget> _buildSplitBreakdown() {
    final amount = double.tryParse(_amountController.text) ?? 0.0;
    if (amount <= 0 || _splitAmongIds.isEmpty) {
      return [
        Text(
          'Enter an amount to see split breakdown',
          style: TextStyle(
            fontSize: 12,
            color: Colors.grey[600],
            fontStyle: FontStyle.italic,
          ),
        ),
      ];
    }

    List<Widget> breakdownItems = [];

    for (String memberId in _splitAmongIds) {
      final member = _groupDetails!.members.firstWhere((m) => m.id == memberId);
      double memberAmount = 0.0;
      String amountDisplay = '';

      if (_splitType == 'equal') {
        memberAmount = amount / _splitAmongIds.length;
        amountDisplay = '₹${memberAmount.toStringAsFixed(2)}';
      } else if (_splitType == 'custom') {
        memberAmount = _customSplits[memberId] ?? 0.0;
        amountDisplay = '₹${memberAmount.toStringAsFixed(2)}';
      } else if (_splitType == 'percentage') {
        final percentage = _percentageSplits[memberId] ?? 0.0;
        memberAmount = amount * percentage / 100;
        amountDisplay = '₹${memberAmount.toStringAsFixed(2)} (${percentage.toStringAsFixed(1)}%)';
      }

      breakdownItems.add(
        Padding(
          padding: const EdgeInsets.symmetric(vertical: 2),
          child: Row(
            children: [
              CircleAvatar(
                radius: 12,
                backgroundColor: Colors.blue[100],
                child: Text(
                  member.name[0].toUpperCase(),
                  style: TextStyle(
                    fontSize: 10,
                    fontWeight: FontWeight.bold,
                    color: Colors.blue[800],
                  ),
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  member.name,
                  style: const TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ),
              Text(
                amountDisplay,
                style: const TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
        ),
      );
    }

    // Add total verification
    if (_splitType == 'custom' || _splitType == 'percentage') {
      double totalSplit = 0.0;
      if (_splitType == 'custom') {
        totalSplit = _customSplits.values.fold(0.0, (sum, value) => sum + value);
      } else if (_splitType == 'percentage') {
        final totalPercentage = _percentageSplits.values.fold(0.0, (sum, value) => sum + value);
        totalSplit = amount * totalPercentage / 100;
      }

      final difference = (totalSplit - amount).abs();
      final isValid = difference < 0.01;

      breakdownItems.add(
        Padding(
          padding: const EdgeInsets.only(top: 8),
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            decoration: BoxDecoration(
              color: isValid ? Colors.green[50] : Colors.red[50],
              borderRadius: BorderRadius.circular(8),
              border: Border.all(
                color: isValid ? Colors.green[300]! : Colors.red[300]!,
                width: isValid ? 1 : 2,
              ),
            ),
            child: Row(
              children: [
                Icon(
                  isValid ? Icons.check_circle : Icons.error,
                  size: 18,
                  color: isValid ? Colors.green[600] : Colors.red[600],
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        isValid ? 'Split is balanced ✓' : 'Split doesn\'t add up!',
                        style: TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.bold,
                          color: isValid ? Colors.green[700] : Colors.red[700],
                        ),
                      ),
                      Text(
                        isValid
                            ? 'Total: ₹${totalSplit.toStringAsFixed(2)}'
                            : 'Total: ₹${totalSplit.toStringAsFixed(2)} (Expected: ₹${amount.toStringAsFixed(2)})',
                        style: TextStyle(
                          fontSize: 11,
                          color: isValid ? Colors.green[600] : Colors.red[600],
                        ),
                      ),
                    ],
                  ),
                ),
                if (!isValid)
                  Icon(
                    Icons.warning_amber,
                    size: 16,
                    color: Colors.red[600],
                  ),
              ],
            ),
          ),
        ),
      );
    } else {
      // For equal split, show simple total
      breakdownItems.add(
        Padding(
          padding: const EdgeInsets.only(top: 8),
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
            decoration: BoxDecoration(
              color: Colors.blue[50],
              borderRadius: BorderRadius.circular(8),
              border: Border.all(
                color: Colors.blue[200]!,
              ),
            ),
            child: Row(
              children: [
                Icon(
                  Icons.check_circle,
                  size: 16,
                  color: Colors.blue[600],
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    'Total: ₹${amount.toStringAsFixed(2)} (Split equally)',
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                      color: Colors.blue[700],
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      );
    }

    return breakdownItems;
  }

  void _onGroupChanged(String? groupId) {
    if (groupId == null) return;

    setState(() {
      _groupDetails = _availableGroups.firstWhere((group) => group.id == groupId);
      // Reset selections
      _selectedPayerId = _groupDetails!.members.first.id;
      _splitAmongIds.clear();
      _splitAmongIds.addAll(_groupDetails!.members.map((m) => m.id));
      _customSplits.clear();
      _percentageSplits.clear();
    });
  }

  void _showSplitDetailsDialog() {
    final amount = double.tryParse(_amountController.text) ?? 0.0;
    if (amount <= 0) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please enter a valid amount first')),
      );
      return;
    }
    
    _initializeSplits();

    showDialog(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          title: Text('Split ${_amountController.text} ${_currency}'),
          content: SizedBox(
            width: double.maxFinite,
            height: 400,
            child: Column(
              children: [
                // Split Type Selector
                Row(
                  children: [
                    Expanded(
                      child: RadioListTile<String>(
                        title: const Text('Equal'),
                        value: 'equal',
                        groupValue: _splitType,
                        onChanged: (value) {
                          setDialogState(() {
                            _splitType = value!;
                            _initializeSplits();
                          });
                        },
                      ),
                    ),
                  ],
                ),
                Row(
                  children: [
                    Expanded(
                      child: RadioListTile<String>(
                        title: const Text('Custom'),
                        value: 'custom',
                        groupValue: _splitType,
                        onChanged: (value) {
                          setDialogState(() {
                            _splitType = value!;
                            _initializeSplits();
                          });
                        },
                      ),
                    ),
                    Expanded(
                      child: RadioListTile<String>(
                        title: const Text('Percentage'),
                        value: 'percentage',
                        groupValue: _splitType,
                        onChanged: (value) {
                          setDialogState(() {
                            _splitType = value!;
                            _initializeSplits();
                          });
                        },
                      ),
                    ),
                  ],
                ),

                const Divider(),

                // Split Details
                Expanded(
                  child: ListView.builder(
                    itemCount: _splitAmongIds.length,
                    itemBuilder: (context, index) {
                      final memberId = _splitAmongIds.elementAt(index);
                      final member = _groupDetails!.members.firstWhere((m) => m.id == memberId);

                      return ListTile(
                        leading: CircleAvatar(
                          child: Text(member.name[0].toUpperCase()),
                        ),
                        title: Text(member.name),
                        trailing: SizedBox(
                          width: 100,
                          child: _splitType == 'equal'
                              ? Text(
                                  '${(amount / _splitAmongIds.length).toStringAsFixed(2)} $_currency',
                                  style: const TextStyle(fontWeight: FontWeight.bold),
                                )
                              : _splitType == 'custom'
                                  ? TextFormField(
                                      initialValue: _customSplits[memberId]?.toStringAsFixed(2) ?? '0.00',
                                      keyboardType: TextInputType.number,
                                      decoration: InputDecoration(
                                        suffixText: _currency,
                                        isDense: true,
                                      ),
                                      onChanged: (value) {
                                        final customAmount = double.tryParse(value) ?? 0.0;
                                        setDialogState(() {
                                          _customSplits[memberId] = customAmount;
                                        });
                                      },
                                    )
                                  : TextFormField(
                                      initialValue: _percentageSplits[memberId]?.toStringAsFixed(1) ?? '0.0',
                                      keyboardType: TextInputType.number,
                                      decoration: const InputDecoration(
                                        suffixText: '%',
                                        isDense: true,
                                      ),
                                      onChanged: (value) {
                                        final percentage = double.tryParse(value) ?? 0.0;
                                        setDialogState(() {
                                          _percentageSplits[memberId] = percentage;
                                          _customSplits[memberId] = (amount * percentage / 100);
                                        });
                                      },
                                    ),
                        ),
                      );
                    },
                  ),
                ),

                // Summary
                if (_splitType != 'equal') ...[
                  const Divider(),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      const Text('Total:', style: TextStyle(fontWeight: FontWeight.bold)),
                      Text(
                        _splitType == 'custom'
                            ? '${_customSplits.values.fold(0.0, (sum, amount) => sum + amount).toStringAsFixed(2)} $_currency'
                            : '${_percentageSplits.values.fold(0.0, (sum, percentage) => sum + percentage).toStringAsFixed(1)}%',
                        style: TextStyle(
                          fontWeight: FontWeight.bold,
                          color: _splitType == 'custom'
                              ? (_customSplits.values.fold(0.0, (sum, amount) => sum + amount) == amount
                                  ? Colors.green
                                  : Colors.red)
                              : (_percentageSplits.values.fold(0.0, (sum, percentage) => sum + percentage) == 100.0
                                  ? Colors.green
                                  : Colors.red),
                        ),
                      ),
                    ],
                  ),
                ],
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () {
                setState(() {
                  // Update the main state with split details
                });
                Navigator.of(context).pop();
              },
              child: const Text('Apply Split'),
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _addExpense() async {
    if (!_formKey.currentState!.validate()) return;
    
    final scaffoldMessenger = ScaffoldMessenger.of(context);
    
    if (_selectedPayerId == null) {
      scaffoldMessenger.showSnackBar(
        const SnackBar(content: Text('Please select who paid for this expense')),
      );
      return;
    }
    if (_splitAmongIds.isEmpty) {
      scaffoldMessenger.showSnackBar(
        const SnackBar(content: Text('Please select who to split this expense among')),
      );
      return;
    }

    setState(() {
      _isLoading = true;
    });

    try {
      final request = CreateExpenseRequest(
        groupId: widget.groupId ?? _groupDetails?.id ?? '0',
        name: _nameController.text.trim(),
        description: _descriptionController.text.trim(),
        amount: double.parse(_amountController.text),
        currency: _currency,
        splitAmongIds: _splitAmongIds.toList(),
      );

      await ref.read(groupDetailsProvider.notifier).createExpense(request);

      if (mounted) {
        scaffoldMessenger.showSnackBar(
          SnackBar(
            content: Text('Expense "${_nameController.text.trim()}" added successfully'),
            backgroundColor: Theme.of(context).colorScheme.primary,
          ),
        );
        if (mounted) {
          context.pop();
        }
      }
    } catch (e) {
      if (mounted) {
        scaffoldMessenger.showSnackBar(
          SnackBar(
            content: Text('Failed to add expense: $e'),
            backgroundColor: Theme.of(context).colorScheme.error,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  String? _validateName(String? value) {
    if (value == null || value.trim().isEmpty) {
      return 'Please enter an expense name';
    }
    if (value.trim().length < 2) {
      return 'Expense name must be at least 2 characters';
    }
    return null;
  }

  String? _validateAmount(String? value) {
    if (value == null || value.trim().isEmpty) {
      return 'Please enter an amount';
    }
    final amount = double.tryParse(value.trim());
    if (amount == null) {
      return 'Please enter a valid amount';
    }
    if (amount <= 0) {
      return 'Amount must be greater than 0';
    }
    if (amount > 1000000) {
      return 'Amount is too large';
    }
    return null;
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Add Expense'),
        centerTitle: true,
        actions: [
          if (_isLoading)
            const Padding(
              padding: EdgeInsets.all(16),
              child: SizedBox(
                width: 20,
                height: 20,
                child: CircularProgressIndicator(strokeWidth: 2),
              ),
            ),
        ],
      ),
      body: _groupDetails == null
          ? const Center(child: CircularProgressIndicator())
          : Form(
              key: _formKey,
              child: ListView(
                padding: const EdgeInsets.all(16),
                children: [
                  // Header
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16),
                      child: Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.all(12),
                            decoration: BoxDecoration(
                              color: theme.colorScheme.primaryContainer,
                              borderRadius: BorderRadius.circular(12),
                            ),
                            child: Icon(
                              Icons.receipt_long,
                              color: theme.colorScheme.onPrimaryContainer,
                              size: 24,
                            ),
                          ),
                          const SizedBox(width: 16),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  'New Expense',
                                  style: theme.textTheme.titleMedium?.copyWith(
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                                const SizedBox(height: 4),
                                Text(
                                  'Add a shared expense for ${_groupDetails!.name}',
                                  style: theme.textTheme.bodySmall?.copyWith(
                                    color: theme.colorScheme.onSurfaceVariant,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),

                  const SizedBox(height: 16),

                  // Group Selector
                  Text(
                    'Select Group *',
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Card(
                    child: DropdownButtonFormField<String>(
                      value: _groupDetails?.id,
                      decoration: InputDecoration(
                        prefixIcon: const Icon(Icons.group),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide.none,
                        ),
                        filled: true,
                        fillColor: theme.colorScheme.surfaceContainerHighest,
                      ),
                      items: _availableGroups.map((group) {
                        return DropdownMenuItem<String>(
                          value: group.id,
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                group.name,
                                style: const TextStyle(fontWeight: FontWeight.w600),
                              ),
                              Text(
                                '${group.members.length} members',
                                style: TextStyle(
                                  fontSize: 12,
                                  color: Colors.grey[600],
                                ),
                              ),
                            ],
                          ),
                        );
                      }).toList(),
                      onChanged: _isLoading ? null : _onGroupChanged,
                    ),
                  ),

                  const SizedBox(height: 24),

                  // Expense Name
                  Text(
                    'Expense Name *',
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: 8),
                  TextFormField(
                    controller: _nameController,
                    textCapitalization: TextCapitalization.words,
                    textInputAction: TextInputAction.next,
                    enabled: !_isLoading,
                    validator: _validateName,
                    onChanged: (value) => _onExpenseNameChanged(),
                    decoration: InputDecoration(
                      hintText: 'e.g., Dinner, Gas, Groceries',
                      prefixIcon: const Icon(Icons.receipt_outlined),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                      filled: true,
                      fillColor: theme.colorScheme.surfaceContainerHighest,
                    ),
                  ),

                  const SizedBox(height: 16),

                  // Live Icon Preview - Shows how icons work automatically
                  if (_nameController.text.isNotEmpty) ...[
                    Card(
                      color: Colors.blue[50],
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Row(
                          children: [
                            Container(
                              padding: const EdgeInsets.all(12),
                              decoration: BoxDecoration(
                                color: _getCurrentExpenseIcon().color.withOpacity(0.1),
                                borderRadius: BorderRadius.circular(12),
                              ),
                              child: Icon(
                                _getCurrentExpenseIcon().icon,
                                color: _getCurrentExpenseIcon().color,
                                size: 24,
                              ),
                            ),
                            const SizedBox(width: 16),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    'Auto-Selected Icon',
                                    style: TextStyle(
                                      fontWeight: FontWeight.w600,
                                      color: Colors.blue[800],
                                    ),
                                  ),
                                  Text(
                                    'This icon was automatically chosen based on your expense name',
                                    style: TextStyle(
                                      fontSize: 12,
                                      color: Colors.blue[600],
                                    ),
                                  ),
                                ],
                              ),
                            ),
                            IconButton(
                              icon: Icon(Icons.edit, color: Colors.blue[600]),
                              onPressed: () => _showCategorySelector(context),
                              tooltip: 'Choose different category',
                            ),
                          ],
                        ),
                      ),
                    ),
                    const SizedBox(height: 4),
                  ],

                  const SizedBox(height: 20),

                  // Amount and Currency Row
                  Row(
                    children: [
                      Expanded(
                        flex: 2,
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'Amount *',
                              style: theme.textTheme.titleMedium?.copyWith(
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                            const SizedBox(height: 8),
                            Card(
                              child: TextFormField(
                                controller: _amountController,
                                keyboardType: const TextInputType.numberWithOptions(
                                  decimal: true,
                                ),
                                textInputAction: TextInputAction.next,
                                enabled: !_isLoading,
                                validator: _validateAmount,
                                inputFormatters: [
                                  FilteringTextInputFormatter.allow(
                                    RegExp(r'^\d*\.?\d{0,2}'),
                                  ),
                                ],
                                decoration: InputDecoration(
                                  hintText: '0.00',
                                  prefixIcon: const Icon(Icons.attach_money),
                                  border: OutlineInputBorder(
                                    borderRadius: BorderRadius.circular(12),
                                    borderSide: BorderSide.none,
                                  ),
                                  filled: true,
                                  fillColor: theme.colorScheme.surfaceContainerHighest,
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        flex: 1,
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'Currency',
                              style: theme.textTheme.titleMedium?.copyWith(
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                            const SizedBox(height: 8),
                            Card(
                              child: DropdownButtonFormField<String>(
                                value: _currency,
                                decoration: InputDecoration(
                                  border: OutlineInputBorder(
                                    borderRadius: BorderRadius.circular(12),
                                    borderSide: BorderSide.none,
                                  ),
                                  filled: true,
                                  fillColor: theme.colorScheme.surfaceContainerHighest,
                                ),
                                items: const [
                                  DropdownMenuItem(value: 'USD', child: Text('USD')),
                                  DropdownMenuItem(value: 'EUR', child: Text('EUR')),
                                  DropdownMenuItem(value: 'GBP', child: Text('GBP')),
                                  DropdownMenuItem(value: 'CAD', child: Text('CAD')),
                                ],
                                onChanged: (value) {
                                  if (value != null) {
                                    setState(() {
                                      _currency = value;
                                    });
                                  }
                                },
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 20),

                  // Description
                  Text(
                    'Description (Optional)',
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: 8),
                  TextFormField(
                    controller: _descriptionController,
                    textCapitalization: TextCapitalization.sentences,
                    textInputAction: TextInputAction.done,
                    enabled: !_isLoading,
                    maxLines: 2,
                    decoration: InputDecoration(
                      hintText: 'Additional details (optional)',
                      prefixIcon: const Padding(
                        padding: EdgeInsets.only(bottom: 20),
                        child: Icon(Icons.description_outlined),
                      ),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                      filled: true,
                      fillColor: theme.colorScheme.surfaceContainerHighest,
                    ),
                  ),

                  const SizedBox(height: 24),

                  // Who Paid
                  Text(
                    'Who Paid? *',
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Card(
                    child: Column(
                      children: _groupDetails!.members.map((member) {
                        return RadioListTile<String>(
                          title: Text(member.name),
                          subtitle: Text(member.email),
                          value: member.id,
                          groupValue: _selectedPayerId,
                          onChanged: _isLoading
                              ? null
                              : (value) {
                                  setState(() {
                                    _selectedPayerId = value;
                                  });
                                },
                        );
                      }).toList(),
                    ),
                  ),

                  const SizedBox(height: 24),

                  // Split Among
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          'Split Among *',
                          style: theme.textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                      if (_splitAmongIds.isNotEmpty)
                        TextButton.icon(
                          onPressed: () => _showSplitDetailsDialog(),
                          icon: Icon(Icons.tune, size: 18),
                          label: Text('Split Options'),
                          style: TextButton.styleFrom(
                            foregroundColor: theme.colorScheme.primary,
                          ),
                        ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Card(
                    child: Column(
                      children: _groupDetails!.members.map((member) {
                        return CheckboxListTile(
                          title: Text(member.name),
                          subtitle: Text(member.email),
                          value: _splitAmongIds.contains(member.id),
                          onChanged: _isLoading
                              ? null
                              : (checked) {
                                  setState(() {
                                    if (checked == true) {
                                      _splitAmongIds.add(member.id);
                                      // Initialize splits for new participant
                                      _initializeSplitsForNewParticipant(member.id);
                                    } else {
                                      _splitAmongIds.remove(member.id);
                                      // Remove from custom and percentage splits
                                      _customSplits.remove(member.id);
                                      _percentageSplits.remove(member.id);
                                      // Recalculate splits for remaining participants
                                      _redistributeSplits();
                                    }
                                  });
                                },
                        );
                      }).toList(),
                    ),
                  ),

                  if (_splitAmongIds.isNotEmpty) ...[
                    const SizedBox(height: 8),
                    Card(
                      color: theme.colorScheme.surfaceContainerHighest,
                      child: Padding(
                        padding: const EdgeInsets.all(12),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              children: [
                                Icon(
                                  Icons.pie_chart,
                                  color: theme.colorScheme.primary,
                                  size: 16,
                                ),
                                const SizedBox(width: 8),
                                Expanded(
                                  child: Text(
                                    _splitType == 'equal'
                                        ? 'Split equally among ${_splitAmongIds.length} people'
                                        : _splitType == 'custom'
                                            ? 'Custom split amounts among ${_splitAmongIds.length} people'
                                            : 'Percentage split among ${_splitAmongIds.length} people',
                                    style: theme.textTheme.bodySmall?.copyWith(
                                      color: theme.colorScheme.onSurfaceVariant,
                                      fontWeight: FontWeight.w600,
                                    ),
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 12),
                            // Show split breakdown
                            ...(_buildSplitBreakdown()),
                          ],
                        ),
                      ),
                    ),
                  ],

                  const SizedBox(height: 32),

                  // Buttons
                  Row(
                    children: [
                      Expanded(
                        child: OutlinedButton(
                          onPressed: _isLoading ? null : () => context.pop(),
                          child: const Text('Cancel'),
                        ),
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        flex: 2,
                        child: FilledButton.icon(
                          onPressed: _isLoading ? null : _addExpense,
                          icon: _isLoading
                              ? const SizedBox(
                                  width: 16,
                                  height: 16,
                                  child: CircularProgressIndicator(
                                    strokeWidth: 2,
                                    color: Colors.white,
                                  ),
                                )
                              : const Icon(Icons.add),
                          label: Text(_isLoading ? 'Adding...' : 'Add Expense'),
                        ),
                      ),
                    ],
                  ),

                  const SizedBox(height: 16),
                ],
              ),
            ),
    );
  }
}
