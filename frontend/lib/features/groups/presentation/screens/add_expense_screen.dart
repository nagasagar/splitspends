import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../auth/provider/auth_provider.dart';
import '../../data/models/group_models.dart';
import '../providers/groups_provider.dart';

class AddExpenseScreen extends ConsumerStatefulWidget {
  final String groupId;

  const AddExpenseScreen({
    super.key,
    required this.groupId,
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
  final Set<String> _splitAmongIds = <String>{};
  bool _isLoading = false;

  GroupDetails? _groupDetails;

  @override
  void initState() {
    super.initState();
    _loadGroupDetails();
  }

  @override
  void dispose() {
    _nameController.dispose();
    _descriptionController.dispose();
    _amountController.dispose();
    super.dispose();
  }

  void _loadGroupDetails() {
    final groupDetailsState = ref.read(groupDetailsProvider);
    _groupDetails = groupDetailsState.groupDetails;
    
    if (_groupDetails != null) {
      // Pre-select current user as payer if they're in the group
      final currentUser = ref.read(authProvider).user;
      if (currentUser != null) {
        final currentUserMember = _groupDetails!.members.cast<GroupMember?>().firstWhere(
          (member) => member?.id == currentUser.id.toString(),
          orElse: () => null,
        );
        if (currentUserMember != null) {
          _selectedPayerId = currentUserMember.id;
          _splitAmongIds.addAll(_groupDetails!.members.map((m) => m.id));
        }
      }
    }
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
        groupId: widget.groupId,
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

                  const SizedBox(height: 20),

                  // Amount and Currency
                  Row(
                    children: [
                      Expanded(
                        flex: 3,
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
                            TextFormField(
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
                                ),
                                filled: true,
                                fillColor: theme.colorScheme.surfaceContainerHighest,
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
                            DropdownButtonFormField<String>(
                              value: _currency,
                              decoration: InputDecoration(
                                border: OutlineInputBorder(
                                  borderRadius: BorderRadius.circular(12),
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
                  Text(
                    'Split Among *',
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
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
                                    } else {
                                      _splitAmongIds.remove(member.id);
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
                        child: Row(
                          children: [
                            Icon(
                              Icons.info_outline,
                              color: theme.colorScheme.primary,
                              size: 16,
                            ),
                            const SizedBox(width: 8),
                            Expanded(
                              child: Text(
                                'Split equally among ${_splitAmongIds.length} people',
                                style: theme.textTheme.bodySmall?.copyWith(
                                  color: theme.colorScheme.onSurfaceVariant,
                                ),
                              ),
                            ),
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
