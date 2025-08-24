class AppConfig {
  static late String apiBaseUrl;
  static Future<void> initialize() async {
    // You can load .env, remote config, platform-specific config, etc. here.
    // For now, fallback to a hardcoded default, but allow override via environment if needed.
    const defaultBaseUrl = 'http://localhost:8080';
    // Example: if using top-level Dart env, use:
    apiBaseUrl = const String.fromEnvironment('API_BASE_URL', defaultValue: defaultBaseUrl);

    // If you want to use .env or some async config, perform that loading here
    // and then set apiBaseUrl accordingly.
  }
}
