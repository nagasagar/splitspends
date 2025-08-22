// Custom exceptions for error handling
class AppException implements Exception {
  final String message;
  AppException(this.message);
  @override
  String toString() => message;
}
