import 'dart:developer' as developer;

import 'package:flutter/foundation.dart';

import '../config/app_config.dart';

enum LogLevel {
  debug,
  info,
  warning,
  error,
}

class AppLogger {
  static const String _tag = 'SplitSpends';
  
  // Singleton pattern
  static final AppLogger _instance = AppLogger._internal();
  factory AppLogger() => _instance;
  AppLogger._internal();
  
  // Static convenience methods
  static void debug(String message, {String? tag, Object? error, StackTrace? stackTrace}) {
    _instance._log(LogLevel.debug, message, tag: tag, error: error, stackTrace: stackTrace);
  }
  
  static void info(String message, {String? tag, Object? error, StackTrace? stackTrace}) {
    _instance._log(LogLevel.info, message, tag: tag, error: error, stackTrace: stackTrace);
  }
  
  static void warning(String message, {String? tag, Object? error, StackTrace? stackTrace}) {
    _instance._log(LogLevel.warning, message, tag: tag, error: error, stackTrace: stackTrace);
  }
  
  static void error(String message, {String? tag, Object? error, StackTrace? stackTrace}) {
    _instance._log(LogLevel.error, message, tag: tag, error: error, stackTrace: stackTrace);
  }
  
  // HTTP-specific logging methods
  static void httpRequest(String method, String url, {Map<String, dynamic>? data}) {
    if (!AppConfig.enableDebugLogging) return;
    debug('🌐 HTTP $method: $url${data != null ? '\nData: $data' : ''}', tag: 'HTTP');
  }
  
  static void httpResponse(String method, String url, int statusCode, {dynamic data}) {
    if (!AppConfig.enableDebugLogging) return;
    final emoji = statusCode >= 200 && statusCode < 300 ? '✅' : '❌';
    debug('$emoji HTTP $method: $url ($statusCode)${data != null ? '\nResponse: $data' : ''}', tag: 'HTTP');
  }
  
  static void httpError(String method, String url, Object error, {StackTrace? stackTrace}) {
    AppLogger.error('🚨 HTTP $method: $url - Error: $error', tag: 'HTTP', error: error, stackTrace: stackTrace);
  }
  
  // Repository-specific logging
  static void repositoryError(String repository, String operation, Object error, {StackTrace? stackTrace}) {
    AppLogger.error('📦 Repository Error in $repository.$operation: $error', tag: 'Repository', error: error, stackTrace: stackTrace);
  }
  
  static void repositoryInfo(String repository, String operation, {String? details}) {
    if (!AppConfig.enableDebugLogging) return;
    info('📦 Repository: $repository.$operation${details != null ? ' - $details' : ''}', tag: 'Repository');
  }
  
  // Config logging
  static void configInfo(String message) {
    info('⚙️ Config: $message', tag: 'Config');
  }
  
  void _log(LogLevel level, String message, {String? tag, Object? error, StackTrace? stackTrace}) {
    // Don't log debug messages in production unless explicitly enabled
    if (level == LogLevel.debug && AppConfig.isProduction && !AppConfig.enableDebugLogging) {
      return;
    }
    
    final logTag = tag != null ? '$_tag:$tag' : _tag;
    final timestamp = DateTime.now().toIso8601String();
    final levelStr = level.name.toUpperCase();
    
    // Format the message
    final formattedMessage = '[$timestamp] [$levelStr] $message';
    
    if (kDebugMode) {
      // In debug mode, use developer.log for better formatting in IDEs
      developer.log(
        formattedMessage,
        name: logTag,
        level: _mapLogLevel(level),
        error: error,
        stackTrace: stackTrace,
      );
    } else {
      // In release mode, still log errors and warnings
      if (level == LogLevel.error || level == LogLevel.warning) {
        developer.log(
          formattedMessage,
          name: logTag,
          level: _mapLogLevel(level),
          error: error,
          stackTrace: stackTrace,
        );
      }
    }
  }
  
  int _mapLogLevel(LogLevel level) {
    switch (level) {
      case LogLevel.debug:
        return 300;
      case LogLevel.info:
        return 800;
      case LogLevel.warning:
        return 900;
      case LogLevel.error:
        return 1000;
    }
  }
}
