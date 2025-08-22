import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:splitspends_flutter/core/config/app_config.dart';

import 'app.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  // Initialize Hive
  await Hive.initFlutter();
  // Initialize app configuration
  await AppConfig.initialize();
  runApp(
    const ProviderScope(
      child: SplitSpendsApp(),
    ),
  );
}
