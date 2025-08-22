import 'package:equatable/equatable.dart';
import 'package:json_annotation/json_annotation.dart';

part 'auth_request.g.dart';

@JsonSerializable()
class AuthRequest extends Equatable {
  final String? name;
  final String email;
  final String password;
  const AuthRequest({
    this.name,
    required this.email,
    required this.password,
  });
  factory AuthRequest.fromJson(Map<String, dynamic> json) => _$AuthRequestFromJson(json);
  Map<String, dynamic> toJson() => _$AuthRequestToJson(this);
  @override
  List<Object?> get props => [name, email, password];
}
