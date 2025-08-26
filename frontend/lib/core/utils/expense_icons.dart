import 'package:flutter/material.dart';

/// Utility class to determine expense icons and colors based on expense data
class ExpenseIcons {
  // Define expense categories and their corresponding icons and colors
  static const Map<String, ExpenseIconData> _categoryMap = {
    // Food & Dining
    'food': ExpenseIconData(Icons.restaurant, Colors.orange),
    'restaurant': ExpenseIconData(Icons.restaurant, Colors.orange),
    'dining': ExpenseIconData(Icons.restaurant, Colors.orange),
    'dinner': ExpenseIconData(Icons.restaurant, Colors.orange),
    'lunch': ExpenseIconData(Icons.restaurant, Colors.orange),
    'breakfast': ExpenseIconData(Icons.restaurant, Colors.orange),
    'coffee': ExpenseIconData(Icons.local_cafe, Colors.brown),
    'drinks': ExpenseIconData(Icons.local_bar, Colors.purple),
    'groceries': ExpenseIconData(Icons.shopping_cart, Colors.green),
    'grocery': ExpenseIconData(Icons.shopping_cart, Colors.green),
    'pizza': ExpenseIconData(Icons.local_pizza, Colors.red),
    'snacks': ExpenseIconData(Icons.fastfood, Colors.orange),
    
    // Transportation
    'transport': ExpenseIconData(Icons.directions_car, Colors.blue),
    'transportation': ExpenseIconData(Icons.directions_car, Colors.blue),
    'uber': ExpenseIconData(Icons.local_taxi, Colors.black),
    'taxi': ExpenseIconData(Icons.local_taxi, Colors.yellow),
    'bus': ExpenseIconData(Icons.directions_bus, Colors.blue),
    'train': ExpenseIconData(Icons.train, Colors.green),
    'metro': ExpenseIconData(Icons.subway, Colors.blue),
    'flight': ExpenseIconData(Icons.flight, Colors.blue),
    'airline': ExpenseIconData(Icons.flight, Colors.blue),
    'gas': ExpenseIconData(Icons.local_gas_station, Colors.red),
    'fuel': ExpenseIconData(Icons.local_gas_station, Colors.red),
    'parking': ExpenseIconData(Icons.local_parking, Colors.grey),
    'car': ExpenseIconData(Icons.directions_car, Colors.blue),
    'bike': ExpenseIconData(Icons.directions_bike, Colors.green),
    
    // Accommodation
    'accommodation': ExpenseIconData(Icons.hotel, Colors.indigo),
    'hotel': ExpenseIconData(Icons.hotel, Colors.indigo),
    'airbnb': ExpenseIconData(Icons.home, Colors.red),
    'lodging': ExpenseIconData(Icons.hotel, Colors.indigo),
    'rent': ExpenseIconData(Icons.home, Colors.brown),
    
    // Entertainment
    'entertainment': ExpenseIconData(Icons.movie, Colors.purple),
    'movie': ExpenseIconData(Icons.movie, Colors.purple),
    'cinema': ExpenseIconData(Icons.movie, Colors.purple),
    'theatre': ExpenseIconData(Icons.theater_comedy, Colors.purple),
    'concert': ExpenseIconData(Icons.music_note, Colors.purple),
    'music': ExpenseIconData(Icons.music_note, Colors.purple),
    'game': ExpenseIconData(Icons.sports_esports, Colors.green),
    'gaming': ExpenseIconData(Icons.sports_esports, Colors.green),
    'sports': ExpenseIconData(Icons.sports_basketball, Colors.orange),
    'gym': ExpenseIconData(Icons.fitness_center, Colors.red),
    'fitness': ExpenseIconData(Icons.fitness_center, Colors.red),
    
    // Shopping
    'shopping': ExpenseIconData(Icons.shopping_bag, Colors.pink),
    'clothes': ExpenseIconData(Icons.checkroom, Colors.pink),
    'clothing': ExpenseIconData(Icons.checkroom, Colors.pink),
    'shoes': ExpenseIconData(Icons.checkroom, Colors.pink),
    'electronics': ExpenseIconData(Icons.devices, Colors.blue),
    'books': ExpenseIconData(Icons.menu_book, Colors.brown),
    'gifts': ExpenseIconData(Icons.card_giftcard, Colors.red),
    'gift': ExpenseIconData(Icons.card_giftcard, Colors.red),
    
    // Healthcare
    'healthcare': ExpenseIconData(Icons.local_hospital, Colors.red),
    'health': ExpenseIconData(Icons.local_hospital, Colors.red),
    'medical': ExpenseIconData(Icons.local_hospital, Colors.red),
    'doctor': ExpenseIconData(Icons.local_hospital, Colors.red),
    'pharmacy': ExpenseIconData(Icons.local_pharmacy, Colors.green),
    'medicine': ExpenseIconData(Icons.local_pharmacy, Colors.green),
    'dental': ExpenseIconData(Icons.local_hospital, Colors.blue),
    
    // Bills & Utilities
    'bills': ExpenseIconData(Icons.receipt_long, Colors.blue),
    'bill': ExpenseIconData(Icons.receipt_long, Colors.blue),
    'utilities': ExpenseIconData(Icons.electrical_services, Colors.yellow),
    'electricity': ExpenseIconData(Icons.electrical_services, Colors.yellow),
    'water': ExpenseIconData(Icons.water_drop, Colors.blue),
    'internet': ExpenseIconData(Icons.wifi, Colors.blue),
    'phone': ExpenseIconData(Icons.phone, Colors.green),
    'mobile': ExpenseIconData(Icons.phone_android, Colors.green),
    
    // Travel
    'travel': ExpenseIconData(Icons.luggage, Colors.orange),
    'vacation': ExpenseIconData(Icons.beach_access, Colors.blue),
    'holiday': ExpenseIconData(Icons.beach_access, Colors.blue),
    'trip': ExpenseIconData(Icons.luggage, Colors.orange),
    'tourism': ExpenseIconData(Icons.luggage, Colors.orange),
    
    // Education
    'education': ExpenseIconData(Icons.school, Colors.blue),
    'course': ExpenseIconData(Icons.school, Colors.blue),
    'training': ExpenseIconData(Icons.school, Colors.blue),
    'tuition': ExpenseIconData(Icons.school, Colors.blue),
    
    // Personal Care
    'personal': ExpenseIconData(Icons.person, Colors.teal),
    'haircut': ExpenseIconData(Icons.content_cut, Colors.pink),
    'beauty': ExpenseIconData(Icons.face, Colors.pink),
    'spa': ExpenseIconData(Icons.spa, Colors.purple),
    
    // Miscellaneous
    'misc': ExpenseIconData(Icons.more_horiz, Colors.grey),
    'miscellaneous': ExpenseIconData(Icons.more_horiz, Colors.grey),
    'other': ExpenseIconData(Icons.more_horiz, Colors.grey),
    'general': ExpenseIconData(Icons.receipt, Colors.grey),
  };

  /// Get icon data for an expense based on its name and description
  static ExpenseIconData getExpenseIcon(String expenseName, {String? description, String? category}) {
    final searchText = '${expenseName.toLowerCase()} ${description?.toLowerCase() ?? ''} ${category?.toLowerCase() ?? ''}';
    
    // First, try to match by category if provided
    if (category != null && category.isNotEmpty) {
      final categoryIcon = _categoryMap[category.toLowerCase()];
      if (categoryIcon != null) {
        return categoryIcon;
      }
    }
    
    // Then try to match keywords in expense name and description
    for (final entry in _categoryMap.entries) {
      if (searchText.contains(entry.key)) {
        return entry.value;
      }
    }
    
    // Try partial matches for common expense patterns
    if (searchText.contains('eat') || searchText.contains('meal')) {
      return _categoryMap['food']!;
    }
    if (searchText.contains('drive') || searchText.contains('ride')) {
      return _categoryMap['transport']!;
    }
    if (searchText.contains('stay') || searchText.contains('room')) {
      return _categoryMap['accommodation']!;
    }
    if (searchText.contains('buy') || searchText.contains('purchase')) {
      return _categoryMap['shopping']!;
    }
    if (searchText.contains('pay') || searchText.contains('bill')) {
      return _categoryMap['bills']!;
    }
    
    // Default fallback
    return const ExpenseIconData(Icons.receipt, Colors.grey);
  }

  /// Get available categories for expense creation/editing
  static List<ExpenseCategoryOption> getExpenseCategories() {
    return [
      ExpenseCategoryOption('food', 'Food & Dining', _categoryMap['food']!),
      ExpenseCategoryOption('transport', 'Transportation', _categoryMap['transport']!),
      ExpenseCategoryOption('accommodation', 'Accommodation', _categoryMap['accommodation']!),
      ExpenseCategoryOption('entertainment', 'Entertainment', _categoryMap['entertainment']!),
      ExpenseCategoryOption('shopping', 'Shopping', _categoryMap['shopping']!),
      ExpenseCategoryOption('healthcare', 'Healthcare', _categoryMap['healthcare']!),
      ExpenseCategoryOption('bills', 'Bills & Utilities', _categoryMap['bills']!),
      ExpenseCategoryOption('travel', 'Travel', _categoryMap['travel']!),
      ExpenseCategoryOption('education', 'Education', _categoryMap['education']!),
      ExpenseCategoryOption('personal', 'Personal Care', _categoryMap['personal']!),
      ExpenseCategoryOption('other', 'Other', _categoryMap['other']!),
    ];
  }

  /// Analyze expense name to suggest a category
  static String suggestCategory(String expenseName, {String? description}) {
    final searchText = '${expenseName.toLowerCase()} ${description?.toLowerCase() ?? ''}';
    
    final categoryScores = <String, int>{};
    
    for (final entry in _categoryMap.entries) {
      if (searchText.contains(entry.key)) {
        final category = _getCategoryForKeyword(entry.key);
        categoryScores[category] = (categoryScores[category] ?? 0) + 1;
      }
    }
    
    if (categoryScores.isNotEmpty) {
      return categoryScores.entries
          .reduce((a, b) => a.value > b.value ? a : b)
          .key;
    }
    
    return 'other';
  }

  static String _getCategoryForKeyword(String keyword) {
    const categoryGroups = {
      'food': ['food', 'restaurant', 'dining', 'dinner', 'lunch', 'breakfast', 'coffee', 'drinks', 'groceries', 'grocery', 'pizza', 'snacks'],
      'transport': ['transport', 'transportation', 'uber', 'taxi', 'bus', 'train', 'metro', 'flight', 'airline', 'gas', 'fuel', 'parking', 'car', 'bike'],
      'accommodation': ['accommodation', 'hotel', 'airbnb', 'lodging', 'rent'],
      'entertainment': ['entertainment', 'movie', 'cinema', 'theatre', 'concert', 'music', 'game', 'gaming', 'sports', 'gym', 'fitness'],
      'shopping': ['shopping', 'clothes', 'clothing', 'shoes', 'electronics', 'books', 'gifts', 'gift'],
      'healthcare': ['healthcare', 'health', 'medical', 'doctor', 'pharmacy', 'medicine', 'dental'],
      'bills': ['bills', 'bill', 'utilities', 'electricity', 'water', 'internet', 'phone', 'mobile'],
      'travel': ['travel', 'vacation', 'holiday', 'trip', 'tourism'],
      'education': ['education', 'course', 'training', 'tuition'],
      'personal': ['personal', 'haircut', 'beauty', 'spa'],
    };
    
    for (final entry in categoryGroups.entries) {
      if (entry.value.contains(keyword)) {
        return entry.key;
      }
    }
    
    return 'other';
  }
}

/// Data class for expense icon information
class ExpenseIconData {
  final IconData icon;
  final Color color;

  const ExpenseIconData(this.icon, this.color);
}

/// Data class for expense category options
class ExpenseCategoryOption {
  final String id;
  final String name;
  final ExpenseIconData iconData;

  const ExpenseCategoryOption(this.id, this.name, this.iconData);
}
