import 'package:equatable/equatable.dart';
import 'package:json_annotation/json_annotation.dart';

part 'user.g.dart';

@JsonSerializable()
class User extends Equatable {
  final int id;
  final String name;
  final String email;
  final String? profilePictureUrl;
  final bool emailVerified;
  final String preferredCurrency;
  const User({
    required this.id,
    required this.name,
    required this.email,
    this.profilePictureUrl,
    required this.emailVerified,
    required this.preferredCurrency,
  });

  factory User.fromJson(Map<String, dynamic> json) => User(
        id: (json['id'] as num).toInt(),
        name: json['name'] as String? ?? '',
        email: json['email'] as String? ?? '',
        profilePictureUrl: json['profilePictureUrl'] as String?,
        emailVerified: (json['emailVerified'] as bool?) ?? false,
        preferredCurrency: json['preferredCurrency'] as String? ?? '',
      );

  Map<String, dynamic> toJson() => _$UserToJson(this);

  @override
  List<Object?> get props => [
        id,
        name,
        email,
        profilePictureUrl,
        emailVerified,
        preferredCurrency,
      ];
}
