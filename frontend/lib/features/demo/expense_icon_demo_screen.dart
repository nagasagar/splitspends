import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/utils/expense_icons.dart';

/// Demo screen showing how expense icons work automatically with real data
class ExpenseIconDemoScreen extends ConsumerWidget {
  const ExpenseIconDemoScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // Example of real expense data that might come from your backend API
    final List<Map<String, dynamic>> realExpenseData = [
      {
        'id': '1',
        'name': 'Dinner at Pizza Hut',
        'description': 'Team dinner after project completion',
        'amount': 45.50,
        'category': null, // No category provided by user
      },
      {
        'id': '2', 
        'name': 'Uber to Airport',
        'description': 'Trip to catch morning flight',
        'amount': 25.00,
        'category': null,
      },
      {
        'id': '3',
        'name': 'Hotel Booking',
        'description': 'Marriott Downtown - 2 nights',
        'amount': 320.00,
        'category': null,
      },
      {
        'id': '4',
        'name': 'Weekly Groceries',
        'description': 'Walmart grocery shopping',
        'amount': 85.75,
        'category': null,
      },
      {
        'id': '5',
        'name': 'Movie Tickets',
        'description': 'Avengers screening',
        'amount': 24.00,
        'category': null,
      },
      {
        'id': '6',
        'name': 'Electric Bill',
        'description': 'Monthly utility payment',
        'amount': 120.00,
        'category': null,
      },
    ];

    return Scaffold(
      appBar: AppBar(
        title: const Text('How Expense Icons Work'),
        backgroundColor: Colors.indigo[600],
        foregroundColor: Colors.white,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Explanation Card
            Card(
              color: Colors.blue[50],
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(Icons.lightbulb, color: Colors.blue[600]),
                        const SizedBox(width: 8),
                        Text(
                          'How Icons Are Automatically Selected',
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                            color: Colors.blue[800],
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    const Text(
                      '• The system analyzes expense names and descriptions\n'
                      '• It looks for keywords like "pizza", "uber", "hotel", etc.\n'
                      '• Icons are automatically assigned without user input\n'
                      '• Users can override the automatic selection if needed',
                      style: TextStyle(fontSize: 14, height: 1.5),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 24),

            // Section 1: Automatic Icon Selection
            Text(
              '1. Automatic Icon Selection',
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'These expenses get icons automatically based on their names:',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: Colors.grey[600],
              ),
            ),
            const SizedBox(height: 16),

            // Show automatic icon selection
            ...realExpenseData.map((expense) {
              final iconData = ExpenseIcons.getExpenseIcon(
                expense['name'],
                description: expense['description'],
              );
              
              return Card(
                margin: const EdgeInsets.only(bottom: 8),
                child: ListTile(
                  leading: Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: iconData.color.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Icon(
                      iconData.icon,
                      color: iconData.color,
                      size: 24,
                    ),
                  ),
                  title: Text(
                    expense['name'],
                    style: const TextStyle(fontWeight: FontWeight.w600),
                  ),
                  subtitle: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(expense['description']),
                      const SizedBox(height: 4),
                      Text(
                        'Auto-detected: ${_getDetectedKeywords(expense['name'], expense['description'])}',
                        style: TextStyle(
                          fontSize: 12,
                          color: iconData.color,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ],
                  ),
                  trailing: Text(
                    '\$${expense['amount'].toStringAsFixed(2)}',
                    style: const TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 16,
                    ),
                  ),
                ),
              );
            }).toList(),

            const SizedBox(height: 32),

            // Section 2: Category Override
            Text(
              '2. User Can Override (Optional)',
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'When creating expenses, users can optionally select a category:',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: Colors.grey[600],
              ),
            ),
            const SizedBox(height: 16),

            // Show category selection options
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Available Categories:',
                      style: TextStyle(
                        fontWeight: FontWeight.w600,
                        fontSize: 16,
                      ),
                    ),
                    const SizedBox(height: 16),
                    Wrap(
                      spacing: 12,
                      runSpacing: 12,
                      children: ExpenseIcons.getExpenseCategories().map((category) {
                        return Container(
                          padding: const EdgeInsets.all(12),
                          decoration: BoxDecoration(
                            color: category.iconData.color.withOpacity(0.1),
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(
                              color: category.iconData.color.withOpacity(0.3),
                            ),
                          ),
                          child: Column(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Icon(
                                category.iconData.icon,
                                color: category.iconData.color,
                                size: 20,
                              ),
                              const SizedBox(height: 4),
                              Text(
                                category.name,
                                style: TextStyle(
                                  fontSize: 10,
                                  color: category.iconData.color,
                                  fontWeight: FontWeight.w500,
                                ),
                                textAlign: TextAlign.center,
                              ),
                            ],
                          ),
                        );
                      }).toList(),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 32),

            // Section 3: Smart Suggestions
            Text(
              '3. Smart Category Suggestions',
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              'The system can suggest categories as the user types:',
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                color: Colors.grey[600],
              ),
            ),
            const SizedBox(height: 16),

            // Show smart suggestions
            Card(
              color: Colors.green[50],
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(Icons.auto_awesome, color: Colors.green[600]),
                        const SizedBox(width: 8),
                        Text(
                          'Smart Suggestions Examples',
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            color: Colors.green[800],
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    ...[
                      _buildSuggestionExample('User types: "Starbucks"', 'System suggests: Food & Dining'),
                      _buildSuggestionExample('User types: "Gas station"', 'System suggests: Transportation'),
                      _buildSuggestionExample('User types: "Netflix"', 'System suggests: Entertainment'),
                      _buildSuggestionExample('User types: "Doctor visit"', 'System suggests: Healthcare'),
                    ],
                  ],
                ),
              ),
            ),

            const SizedBox(height: 32),

            // Summary
            Card(
              color: Colors.orange[50],
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Icon(Icons.summarize, color: Colors.orange[600]),
                        const SizedBox(width: 8),
                        Text(
                          'Summary: How It Works in Practice',
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            color: Colors.orange[800],
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    const Text(
                      '1. User creates expense: "Dinner at McDonald\'s"\n'
                      '2. System automatically assigns food icon 🍽️\n'
                      '3. User can accept it OR choose different category\n'
                      '4. Icon is saved and displayed in all expense lists\n\n'
                      '✨ Result: Most expenses get perfect icons automatically,\n'
                      'with the option to customize when needed!',
                      style: TextStyle(fontSize: 14, height: 1.5),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSuggestionExample(String input, String suggestion) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          Expanded(
            child: Text(
              input,
              style: const TextStyle(fontSize: 14),
            ),
          ),
          const SizedBox(width: 8),
          Icon(Icons.arrow_forward, size: 16, color: Colors.green[600]),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              suggestion,
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.w500,
                color: Colors.green[700],
              ),
            ),
          ),
        ],
      ),
    );
  }

  String _getDetectedKeywords(String name, String description) {
    final searchText = '$name $description'.toLowerCase();
    
    if (searchText.contains('pizza') || searchText.contains('dinner')) return 'Food keywords detected';
    if (searchText.contains('uber') || searchText.contains('airport')) return 'Transport keywords detected';
    if (searchText.contains('hotel') || searchText.contains('booking')) return 'Accommodation keywords detected';
    if (searchText.contains('grocery') || searchText.contains('walmart')) return 'Shopping keywords detected';
    if (searchText.contains('movie') || searchText.contains('tickets')) return 'Entertainment keywords detected';
    if (searchText.contains('bill') || searchText.contains('electric')) return 'Bills keywords detected';
    
    return 'General expense';
  }
}
