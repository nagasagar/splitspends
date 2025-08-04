import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'features/auth/presentation/login_screen.dart';
import 'features/dashboard/presentation/dashboard_screen.dart';

void main() {
  runApp(const ProviderScope(child: SplitSpendsApp()));
}

class SplitSpendsApp extends StatelessWidget {
  const SplitSpendsApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'SplitSpends',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const LoginScreen(),
      // Define routes for navigation as your app grows
      routes: {
        '/login': (context) => const LoginScreen(),
        '/dashboard': (context) => const DashboardScreen(),
        // '/signup': (context) => SignupScreen(),       // TODO: Implement
      },
    );
  }
}
