// Theme configuration for the app
import 'package:flutter/material.dart';

class ThemeConfig {
  static ThemeData get lightTheme => ThemeData(
    brightness: Brightness.light,
    // ...add your theme settings here...
  );

  static ThemeData get darkTheme => ThemeData(
    brightness: Brightness.dark,
    // ...add your theme settings here...
  );
}
