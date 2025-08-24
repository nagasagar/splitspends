import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:splitspends_flutter/features/auth/data/auth_repository.dart';
import 'package:splitspends_flutter/features/auth/provider/auth_provider.dart';
import 'package:splitspends_flutter/features/expenses/data/expenses_repository.dart';
import 'package:splitspends_flutter/features/groups/data/groups_repository.dart';
import 'package:splitspends_flutter/features/users/data/users_repository.dart';
import 'package:splitspends_flutter/shared/utils/auth_token_storage.dart';

class DashboardScreen extends ConsumerStatefulWidget {
  const DashboardScreen({super.key});

  @override
  ConsumerState<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends ConsumerState<DashboardScreen> {
  String userName = '';
  int groupCount = 0;
  double totalOwed = 0;
  double totalToReceive = 0;
  List groups = [];
  bool loading = true;
  String? userId;

  final usersRepo = UsersRepository();
  final groupsRepo = GroupsRepository();
  final expensesRepo = ExpensesRepository();

  @override
  void initState() {
    super.initState();
    loadDashboard();
  }

  Future<void> loadDashboard() async {
    setState(() => loading = true);
    final authRepo = ref.read(authRepositoryProvider);

    // 1. Fetch token using your token storage utility
    final token = await AuthTokenStorage.readToken();
    if (token == null) {
      setState(() => loading = false);
      return;
    }

    // 2. Get the userId from the token
    userId = authRepo.getUserIdFromToken(token);
    if (userId == null) {
      setState(() => loading = false);
      return;
    }

    // 3. Fetch user details
    final user = await usersRepo.getUserDetails(userId!);
    userName = user['name'] ?? '';

    // 4. Fetch all groups for this user
    groups = await groupsRepo.getGroupsByUserId(userId!);
    groupCount = groups.length;

    // 5. Fetch expense stats
    final stats = await expensesRepo.getUserExpenseStats(userId!);
    totalOwed = (stats['owed'] ?? 0).toDouble();
    totalToReceive = (stats['toReceive'] ?? 0).toDouble();

    setState(() => loading = false);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Dashboard"),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () async {
              await ref.read(authProvider.notifier).logout();
            },
          ),
        ],
      ),
      body: loading
          ? const Center(child: CircularProgressIndicator())
          : SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Welcome${userName.isNotEmpty ? ', $userName' : ''}!',
                    style: Theme.of(context).textTheme.headlineSmall,
                  ),
                  const SizedBox(height: 18),
                  Card(
                    child: ListTile(
                      title: const Text("Groups"),
                      subtitle: Text("You are in $groupCount group${groupCount == 1 ? '' : 's'}"),
                      trailing: const Icon(Icons.group),
                      onTap: () {
                        // TODO: Implement group list navigation
                      },
                    ),
                  ),
                  const SizedBox(height: 10),
                  Row(
                    children: [
                      Expanded(
                        child: Card(
                          color: Colors.orange[50],
                          child: ListTile(
                            title: const Text("You Owe"),
                            subtitle: Text('₹${totalOwed.toStringAsFixed(2)}'),
                          ),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Card(
                          color: Colors.green[50],
                          child: ListTile(
                            title: const Text("Owed To You"),
                            subtitle: Text('₹${totalToReceive.toStringAsFixed(2)}'),
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 18),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text("Your Groups", style: Theme.of(context).textTheme.titleMedium),
                      IconButton(
                        icon: const Icon(Icons.refresh),
                        onPressed: loadDashboard,
                        tooltip: "Reload",
                      ),
                    ],
                  ),
                  ...groups.map((group) => Card(
                        child: ListTile(
                          title: Text(group['name'] ?? ''),
                          subtitle: Text("Members: ${(group['members'] as List?)?.length ?? 0}"),
                          onTap: () {
                            // TODO: View this group's detail page
                          },
                        ),
                      )),
                ],
              ),
            ),
      floatingActionButton: FloatingActionButton.extended(
        icon: const Icon(Icons.add),
        label: const Text("Create Group"),
        onPressed: () {
          // TODO: Navigate to group create screen
        },
      ),
    );
  }
}
