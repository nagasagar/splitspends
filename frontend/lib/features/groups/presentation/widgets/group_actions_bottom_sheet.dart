// ignore_for_file: deprecated_member_use

import 'package:flutter/material.dart';

class GroupActionsBottomSheet extends StatelessWidget {
  final String groupId;
  final VoidCallback onAddMember;
  final VoidCallback onAddExpense;
  final VoidCallback onEditGroup;
  final VoidCallback onLeaveGroup;

  const GroupActionsBottomSheet({
    super.key,
    required this.groupId,
    required this.onAddMember,
    required this.onAddExpense,
    required this.onEditGroup,
    required this.onLeaveGroup,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Container(
      decoration: BoxDecoration(
        color: theme.colorScheme.surface,
        borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
      ),
      child: SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // Handle
            Container(
              margin: const EdgeInsets.symmetric(vertical: 12),
              width: 32,
              height: 4,
              decoration: BoxDecoration(
                color: theme.colorScheme.onSurfaceVariant.withOpacity(0.4),
                borderRadius: BorderRadius.circular(2),
              ),
            ),

            // Title
            Padding(
              padding: const EdgeInsets.fromLTRB(24, 8, 24, 16),
              child: Text(
                'Group Actions',
                style: theme.textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),

            // Actions
            _ActionTile(
              icon: Icons.person_add,
              title: 'Add Member',
              subtitle: 'Invite someone to join this group',
              onTap: () {
                Navigator.of(context).pop();
                onAddMember();
              },
            ),

            _ActionTile(
              icon: Icons.add_circle_outline,
              title: 'Add Expense',
              subtitle: 'Record a new group expense',
              onTap: () {
                Navigator.of(context).pop();
                onAddExpense();
              },
            ),

            _ActionTile(
              icon: Icons.edit,
              title: 'Edit Group',
              subtitle: 'Change group name or description',
              onTap: () {
                Navigator.of(context).pop();
                onEditGroup();
              },
            ),

            const Divider(height: 32),

            _ActionTile(
              icon: Icons.exit_to_app,
              title: 'Leave Group',
              subtitle: 'Remove yourself from this group',
              isDestructive: true,
              onTap: () {
                Navigator.of(context).pop();
                onLeaveGroup();
              },
            ),

            const SizedBox(height: 16),
          ],
        ),
      ),
    );
  }
}

class _ActionTile extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;
  final bool isDestructive;

  const _ActionTile({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.onTap,
    this.isDestructive = false,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final titleColor = isDestructive 
        ? theme.colorScheme.error 
        : theme.colorScheme.onSurface;

    return ListTile(
      leading: Container(
        padding: const EdgeInsets.all(8),
        decoration: BoxDecoration(
          color: isDestructive 
              ? theme.colorScheme.errorContainer 
              : theme.colorScheme.primaryContainer,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Icon(
          icon,
          color: isDestructive 
              ? theme.colorScheme.onErrorContainer 
              : theme.colorScheme.onPrimaryContainer,
          size: 20,
        ),
      ),
      title: Text(
        title,
        style: theme.textTheme.titleMedium?.copyWith(
          fontWeight: FontWeight.w500,
          color: titleColor,
        ),
      ),
      subtitle: Text(
        subtitle,
        style: theme.textTheme.bodySmall?.copyWith(
          color: theme.colorScheme.onSurfaceVariant,
        ),
      ),
      onTap: onTap,
      contentPadding: const EdgeInsets.symmetric(horizontal: 24, vertical: 4),
      trailing: Icon(
        Icons.chevron_right,
        color: theme.colorScheme.onSurfaceVariant,
      ),
    );
  }
}
