import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'core/config/theme_config.dart';
import 'navigation/app_router.dart';
import 'shared/providers/auth_provider.dart';

class SplitSpendsApp extends ConsumerWidget {
  const SplitSpendsApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(appRouterProvider);
    return MaterialApp.router(
      title: 'SplitSpends',
      theme: ThemeConfig.lightTheme,
      darkTheme: ThemeConfig.darkTheme,
      routerConfig: router,
      debugShowCheckedModeBanner: false,
    );
  }
}
