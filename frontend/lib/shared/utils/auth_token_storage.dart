import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

class AuthTokenStorage {
  static const FlutterSecureStorage _secureStorage = FlutterSecureStorage();

  /// Save token securely (platform-specific)
  static Future<void> saveToken(String token) async {
    if (kIsWeb) {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('jwt', token);
    } else {
      await _secureStorage.write(key: 'jwt', value: token);
    }
  }

  /// Read token securely (platform-specific)
  static Future<String?> readToken() async {
    if (kIsWeb) {
      final prefs = await SharedPreferences.getInstance();
      return prefs.getString('jwt');
    } else {
      return await _secureStorage.read(key: 'jwt');
    }
  }

  /// Delete token securely (platform-specific)
  static Future<void> deleteToken() async {
    if (kIsWeb) {
      final prefs = await SharedPreferences.getInstance();
      await prefs.remove('jwt');
    } else {
      await _secureStorage.delete(key: 'jwt');
    }
  }
}
