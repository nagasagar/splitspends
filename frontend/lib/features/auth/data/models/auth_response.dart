import 'package:equatable/equatable.dart';
import 'package:json_annotation/json_annotation.dart';

import 'user.dart';

part 'auth_response.g.dart';

@JsonSerializable()
class AuthResponse extends Equatable {
  final String token;
  final String? refreshToken;
  final String email;
  final User? user;
  final String? error;

  const AuthResponse({
    required this.token,
    this.refreshToken,
    required this.email,
    this.user,
    this.error,
  });
  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    return AuthResponse(
      token: json['token'] as String,
      refreshToken: json['refreshToken'] as String?,
      email: json['email'] as String,
      user: json['user'] != null ? User.fromJson(json['user']) : null,
      error: json['error'],
    );
  }
  bool get isSuccess => error == null || error!.isEmpty;
  Map<String, dynamic> toJson() => _$AuthResponseToJson(this);
  @override
  List<Object?> get props => [token, refreshToken, email, user];
}
