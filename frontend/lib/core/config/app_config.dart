import 'package:flutter/foundation.dart';

import '../logging/app_logger.dart';

class AppConfig {
  static late String _baseUrl;
  static late String _environment;

  // Default URLs for different environments
  static const String _devBaseUrl = 'http://localhost:8080';
  static const String _stagingBaseUrl = 'https://staging-api.splitspends.com';
  static const String _prodBaseUrl = 'https://api.splitspends.com';

  // Environment and URL keys for dart-define
  static const String _environmentKey = 'ENVIRONMENT';
  static const String _apiBaseUrlKey = 'API_BASE_URL';
  
  static Future<void> initialize() async {
    // Load environment variables, secrets, API endpoints, etc.
    
    // Get environment from dart-define or default to development
    _environment = const String.fromEnvironment(_environmentKey, defaultValue: 'development');

    // Check if API_BASE_URL is explicitly set via dart-define
    const explicitUrl = String.fromEnvironment(_apiBaseUrlKey);
    if (explicitUrl.isNotEmpty) {
      _baseUrl = explicitUrl;
    } else {
      // Otherwise, determine based on environment
      switch (_environment.toLowerCase()) {
        case 'production':
        case 'prod':
          _baseUrl = _prodBaseUrl;
          break;
        case 'staging':
        case 'stage':
          _baseUrl = _stagingBaseUrl;
          break;
        case 'development':
        case 'dev':
        default:
          _baseUrl = _devBaseUrl;
          break;
      }
    }

    // Print configuration in debug mode
    if (kDebugMode) {
      printConfig();
    }

    // If you want to use .env or some async config, perform that loading here
    // and then set _baseUrl accordingly.
  }

  // Getters for accessing configuration
  static String get environment => _environment;
  static String get baseUrl => _baseUrl;
  static String get apiBaseUrl => '$_baseUrl/api';

  // Environment checks
  static bool get isProduction => _environment.toLowerCase() == 'production';
  static bool get isDevelopment => _environment.toLowerCase() == 'development';
  static bool get isStaging => _environment.toLowerCase() == 'staging';

  // HTTP client configuration
  static Duration get connectTimeout {
    return isProduction ? const Duration(seconds: 10) : const Duration(seconds: 30);
  }

  static Duration get receiveTimeout {
    return isProduction ? const Duration(seconds: 30) : const Duration(seconds: 60);
  }

  static Duration get sendTimeout {
    return isProduction ? const Duration(seconds: 30) : const Duration(seconds: 60);
  }

  // Feature flags
  static bool get enableDebugLogging => !isProduction;
  static bool get enableAnalytics => isProduction || isStaging;
  static bool get enableCrashReporting => isProduction || isStaging;

  // Debug information
  static void printConfig() {
    if (kDebugMode) {
      AppLogger.configInfo('=== SplitSpends App Configuration ===');
      AppLogger.configInfo('Environment: $_environment');
      AppLogger.configInfo('Base URL: $_baseUrl');
      AppLogger.configInfo('API Base URL: $apiBaseUrl');
      AppLogger.configInfo('Is Production: $isProduction');
      AppLogger.configInfo('Connect Timeout: ${connectTimeout.inSeconds}s');
      AppLogger.configInfo('Receive Timeout: ${receiveTimeout.inSeconds}s');
      AppLogger.configInfo('Debug Logging: $enableDebugLogging');
      AppLogger.configInfo('=====================================');
    }
  }
}
