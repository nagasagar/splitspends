// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

User _$UserFromJson(Map<String, dynamic> json) => User(
      id: (json['id'] as num).toInt(),
      name: json['name'] as String,
      email: json['email'] as String,
      profilePictureUrl: json['profilePictureUrl'] as String?,
      emailVerified: json['emailVerified'] as bool,
      preferredCurrency: json['preferredCurrency'] as String,
    );

Map<String, dynamic> _$UserToJson(User instance) => <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'email': instance.email,
      'profilePictureUrl': instance.profilePictureUrl,
      'emailVerified': instance.emailVerified,
      'preferredCurrency': instance.preferredCurrency,
    };
