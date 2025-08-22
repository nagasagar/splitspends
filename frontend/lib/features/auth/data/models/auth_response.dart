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
  const AuthResponse({
    required this.token,
    this.refreshToken,
    required this.email,
    this.user,
  });
  factory AuthResponse.fromJson(Map<String, dynamic> json) => _$AuthResponseFromJson(json);
  Map<String, dynamic> toJson() => _$AuthResponseToJson(this);
  @override
  List<Object?> get props => [token, refreshToken, email, user];
}
