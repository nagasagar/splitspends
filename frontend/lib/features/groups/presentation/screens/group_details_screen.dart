import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../auth/models/auth_models.dart';
import '../../../auth/provider/auth_provider.dart';
import '../providers/groups_provider.dart';
import '../widgets/expense_card.dart';
import '../widgets/group_actions_bottom_sheet.dart';
import '../widgets/member_card.dart';

class GroupDetailsScreen extends ConsumerStatefulWidget {
  final String groupId;

  const GroupDetailsScreen({
    super.key,
    required this.groupId,
  });

  @override
  ConsumerState<GroupDetailsScreen> createState() => _GroupDetailsScreenState();
}

class _GroupDetailsScreenState extends ConsumerState<GroupDetailsScreen> {
  @override
  void initState() {
    super.initState();
    // Load group details when screen initializes
    Future.microtask(() {
      ref.read(groupDetailsProvider.notifier).loadGroupDetails(widget.groupId);
    });
  }

  void _showGroupActions() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => GroupActionsBottomSheet(
        groupId: widget.groupId,
        onAddMember: () => _navigateToAddMember(),
        onAddExpense: () => _navigateToAddExpense(),
        onEditGroup: () => _navigateToEditGroup(),
        onLeaveGroup: () => _showLeaveGroupDialog(),
      ),
    );
  }

  void _navigateToAddMember() {
    context.push('/groups/${widget.groupId}/add-member');
  }

  void _navigateToAddExpense() {
    context.push('/groups/${widget.groupId}/add-expense');
  }

  void _navigateToEditGroup() {
    context.push('/groups/${widget.groupId}/edit');
  }

  void _showLeaveGroupDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Leave Group'),
        content: const Text(
          'Are you sure you want to leave this group? '
          'You won\'t be able to see group expenses or participate in new ones.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () {
              Navigator.of(context).pop();
              _leaveGroup();
            },
            style: FilledButton.styleFrom(
              backgroundColor: Theme.of(context).colorScheme.error,
            ),
            child: const Text('Leave'),
          ),
        ],
      ),
    );
  }

  void _leaveGroup() async {
    final currentUser = ref.read(authProvider).user;
    if (currentUser == null) return;

    final scaffoldMessenger = ScaffoldMessenger.of(context);

    try {
      await ref.read(groupDetailsProvider.notifier).leaveGroup(
        widget.groupId,
        currentUser.id.toString(),
      );
      
      if (mounted) {
        scaffoldMessenger.showSnackBar(
          const SnackBar(content: Text('Left group successfully')),
        );
        if (mounted) {
          context.go('/dashboard');
        }
      }
    } catch (e) {
      if (mounted) {
        scaffoldMessenger.showSnackBar(
          SnackBar(content: Text('Failed to leave group: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final groupDetailsState = ref.watch(groupDetailsProvider);
    final currentUser = ref.watch(authProvider).user;

    return Scaffold(
      body: _buildBody(groupDetailsState, currentUser),
      floatingActionButton: groupDetailsState.groupDetails != null
          ? FloatingActionButton.extended(
              onPressed: _navigateToAddExpense,
              icon: const Icon(Icons.add),
              label: const Text('Add Expense'),
            )
          : null,
    );
  }

  Widget _buildBody(GroupDetailsState groupDetailsState, User? currentUser) {
    if (groupDetailsState.isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (groupDetailsState.error != null) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.error_outline,
              size: 64,
              color: Theme.of(context).colorScheme.error,
            ),
            const SizedBox(height: 16),
            Text(
              'Failed to load group details',
              style: Theme.of(context).textTheme.headlineSmall,
            ),
            const SizedBox(height: 8),
            Text(
              groupDetailsState.error!,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: Theme.of(context).colorScheme.onSurfaceVariant,
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 24),
            FilledButton.icon(
              onPressed: () {
                ref.read(groupDetailsProvider.notifier).loadGroupDetails(widget.groupId);
              },
              icon: const Icon(Icons.refresh),
              label: const Text('Retry'),
            ),
          ],
        ),
      );
    }

    final group = groupDetailsState.groupDetails;
    if (group == null) {
      return const Center(
        child: Text('Group not found'),
      );
    }

    return RefreshIndicator(
      onRefresh: () async {
        await ref.read(groupDetailsProvider.notifier).loadGroupDetails(widget.groupId);
      },
      child: CustomScrollView(
        slivers: [
          // App Bar with Group Info
          SliverAppBar(
            expandedHeight: 200,
            pinned: true,
            flexibleSpace: FlexibleSpaceBar(
              title: Text(
                group.name,
                style: const TextStyle(
                  fontWeight: FontWeight.bold,
                  shadows: [
                    Shadow(
                      offset: Offset(0, 1),
                      blurRadius: 3,
                      color: Colors.black26,
                    ),
                  ],
                ),
              ),
              background: Container(
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [
                      Theme.of(context).colorScheme.primaryContainer,
                      Theme.of(context).colorScheme.secondaryContainer,
                    ],
                  ),
                ),
                child: group.imageUrl != null
                    ? Image.network(
                        group.imageUrl!,
                        fit: BoxFit.cover,
                      )
                    : const SizedBox.shrink(),
              ),
            ),
            actions: [
              IconButton(
                onPressed: _showGroupActions,
                icon: const Icon(Icons.more_vert),
                tooltip: 'Group actions',
              ),
            ],
          ),

          // Group Description
          if (group.description?.isNotEmpty == true)
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Icon(
                              Icons.info_outline,
                              size: 20,
                              color: Theme.of(context).colorScheme.primary,
                            ),
                            const SizedBox(width: 8),
                            Text(
                              'Description',
                              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 8),
                        Text(
                          group.description!,
                          style: Theme.of(context).textTheme.bodyMedium,
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),

          // Members Section
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Icon(
                    Icons.people,
                    color: Theme.of(context).colorScheme.primary,
                  ),
                  const SizedBox(width: 8),
                  Text(
                    'Members (${group.members.length})',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const Spacer(),
                  FilledButton.tonalIcon(
                    onPressed: _navigateToAddMember,
                    icon: const Icon(Icons.person_add, size: 18),
                    label: const Text('Add'),
                    style: FilledButton.styleFrom(
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      minimumSize: const Size(0, 36),
                    ),
                  ),
                ],
              ),
            ),
          ),

          // Members List
          SliverList(
            delegate: SliverChildBuilderDelegate(
              (context, index) {
                final member = group.members[index];
                final isCurrentUser = currentUser?.id.toString() == member.id;
                
                return Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
                  child: MemberCard(
                    member: member,
                    isCurrentUser: isCurrentUser,
                    canManage: currentUser != null && 
                              group.members.any((m) => m.id == currentUser.id.toString() && m.isAdmin) &&
                              !isCurrentUser,
                    onMakeAdmin: () async {
                      final scaffoldMessenger = ScaffoldMessenger.of(context);
                      try {
                        await ref.read(groupDetailsProvider.notifier)
                            .makeAdmin(widget.groupId, member.id);
                        if (mounted) {
                          scaffoldMessenger.showSnackBar(
                            SnackBar(content: Text('${member.name} is now an admin')),
                          );
                        }
                      } catch (e) {
                        if (mounted) {
                          scaffoldMessenger.showSnackBar(
                            SnackBar(content: Text('Failed to make admin: $e')),
                          );
                        }
                      }
                    },
                    onRemove: () async {
                      final scaffoldMessenger = ScaffoldMessenger.of(context);
                      final confirmed = await showDialog<bool>(
                        context: context,
                        builder: (context) => AlertDialog(
                          title: const Text('Remove Member'),
                          content: Text('Remove ${member.name} from this group?'),
                          actions: [
                            TextButton(
                              onPressed: () => Navigator.of(context).pop(false),
                              child: const Text('Cancel'),
                            ),
                            FilledButton(
                              onPressed: () => Navigator.of(context).pop(true),
                              style: FilledButton.styleFrom(
                                backgroundColor: Theme.of(context).colorScheme.error,
                              ),
                              child: const Text('Remove'),
                            ),
                          ],
                        ),
                      );

                      if (confirmed == true && mounted) {
                        try {
                          await ref.read(groupDetailsProvider.notifier)
                              .removeMember(widget.groupId, member.id);
                          if (mounted) {
                            scaffoldMessenger.showSnackBar(
                              SnackBar(content: Text('${member.name} removed from group')),
                            );
                          }
                        } catch (e) {
                          if (mounted) {
                            scaffoldMessenger.showSnackBar(
                              SnackBar(content: Text('Failed to remove member: $e')),
                            );
                          }
                        }
                      }
                    },
                  ),
                );
              },
              childCount: group.members.length,
            ),
          ),

          // Expenses Section
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Icon(
                    Icons.receipt_long,
                    color: Theme.of(context).colorScheme.primary,
                  ),
                  const SizedBox(width: 8),
                  Text(
                    'Expenses (${group.expenses.length})',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const Spacer(),
                  FilledButton.tonalIcon(
                    onPressed: _navigateToAddExpense,
                    icon: const Icon(Icons.add, size: 18),
                    label: const Text('Add'),
                    style: FilledButton.styleFrom(
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                      minimumSize: const Size(0, 36),
                    ),
                  ),
                ],
              ),
            ),
          ),

          // Expenses List
          if (group.expenses.isEmpty)
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.all(32),
                child: Column(
                  children: [
                    Icon(
                      Icons.receipt_outlined,
                      size: 64,
                      color: Theme.of(context).colorScheme.onSurfaceVariant,
                    ),
                    const SizedBox(height: 16),
                    Text(
                      'No expenses yet',
                      style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                        color: Theme.of(context).colorScheme.onSurfaceVariant,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      'Add the first expense to get started',
                      style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                        color: Theme.of(context).colorScheme.onSurfaceVariant,
                      ),
                    ),
                    const SizedBox(height: 16),
                    FilledButton.icon(
                      onPressed: _navigateToAddExpense,
                      icon: const Icon(Icons.add),
                      label: const Text('Add Expense'),
                    ),
                  ],
                ),
              ),
            )
          else
            SliverList(
              delegate: SliverChildBuilderDelegate(
                (context, index) {
                  final expense = group.expenses[index];
                  return Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
                    child: ExpenseCard(
                      expense: expense,
                      groupMembers: group.members,
                      onTap: () {
                        // Navigate to expense details (to be implemented)
                        // context.push('/expenses/${expense.id}');
                      },
                    ),
                  );
                },
                childCount: group.expenses.length,
              ),
            ),

          // Bottom spacing
          const SliverToBoxAdapter(
            child: SizedBox(height: 32),
          ),
        ],
      ),
    );
  }
}
