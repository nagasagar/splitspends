import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'SplitSpends',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const LoginScreen(),
    );
  }
}

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();
  final String backendUrl = 'http://localhost:8080/api/test';
  String _message = '';
  bool _isLoading = false;

  Future<void> testBackendWithAuth() async {
    setState(() {
      _isLoading = true;
      _message = '';
    });

    try {
      // Create basic auth header
      String username = _usernameController.text;
      String password = _passwordController.text;
      String basicAuth = 'Basic ${base64Encode(utf8.encode('$username:$password'))}';

      final response = await http.get(
        Uri.parse(backendUrl),
        headers: {
          'Authorization': basicAuth,
          'Content-Type': 'application/json',
        },
      );

      if (response.statusCode == 200) {
        setState(() {
          _message = '✅ Success: ${response.body}';
        });
      } else {
        setState(() {
          _message = '❌ Error: Status ${response.statusCode}\n${response.body}';
        });
      }
    } catch (e) {
      setState(() {
        _message = '❌ Connection Error: $e';
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('SplitSpends - Backend Test'),
        backgroundColor: Colors.blue,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Text(
              'Login to Test Backend',
              style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 32),
            TextField(
              controller: _usernameController,
              decoration: const InputDecoration(
                labelText: 'Username',
                border: OutlineInputBorder(),
                prefixIcon: Icon(Icons.person),
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _passwordController,
              obscureText: true,
              decoration: const InputDecoration(
                labelText: 'Password',
                border: OutlineInputBorder(),
                prefixIcon: Icon(Icons.lock),
              ),
            ),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: _isLoading ? null : testBackendWithAuth,
              style: ElevatedButton.styleFrom(
                minimumSize: const Size.fromHeight(50),
              ),
              child: _isLoading
                  ? const CircularProgressIndicator()
                  : const Text('Test Backend Connection'),
            ),
            const SizedBox(height: 16),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Colors.grey[100],
                border: Border.all(color: Colors.grey),
                borderRadius: BorderRadius.circular(4),
              ),
              child: Text(
                _message.isEmpty ? 'Enter credentials and test connection' : _message,
                style: TextStyle(
                  color: _message.startsWith('✅') ? Colors.green : Colors.red,
                ),
              ),
            ),
            const SizedBox(height: 16),
            // TODO: Remove this card when moving to production
            const Card(
              color: Colors.yellow,
              child: Padding(
                padding: EdgeInsets.all(12.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(Icons.warning, color: Colors.orange),
                        SizedBox(width: 8),
                        Text('Development Only', style: TextStyle(fontWeight: FontWeight.bold)),
                      ],
                    ),
                    SizedBox(height: 8),
                    Text('Test Credentials:', style: TextStyle(fontWeight: FontWeight.bold)),
                    Text('Username: admin, Password: admin'),
                    Text('Username: testuser, Password: testpassword'),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  void dispose() {
    _usernameController.dispose();
    _passwordController.dispose();
    super.dispose();
  }
}
