# SplitSpends – Smart Expense Sharing App

**Effortless group expense tracking and bill splitting made easy for trips, roommates, and beyond.**

A full-featured expense-sharing app with a Spring Boot backend and Flutter frontend—intuitive for users, powerful for power users!

---
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Flutter](https://img.shields.io/badge/Flutter-Latest-blue.svg)](https://flutter.dev/)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
---
# Requirements

- **User authentication:** Sign up/in, Google SSO
- **Email verification on signup** (pending)
- **Groups:** Create/manage groups (e.g., trips, roommates)
- **Expenses:** Add, edit, delete shared expenses in groups
- **Splitting logic:** Even/uneven splits, settle-up calculations
- **Activity feed:** Who paid what, notifications
- **Balances:** Who owes whom and how much
- **Security:** JWT for APIs, encrypted sensitive data
- **Attachments:** Image upload for receipts (Cloudinary, S3, etc.)
- **Real-time updates:** WebSocket for instant notifications





## 🚀 Key Features

### 👥 **User Management**
- User registration and authentication
- Google SSO integration
- Profile management

### 🏠 **Group Management**
- Create and manage expense groups
- Invite members via email
- Admin and member roles
- Group activity feeds

### 💰 **Expense Tracking**
- Add, edit, and delete expenses
- Multiple splitting methods (even/uneven splits)
- Receipt attachment support
- Expense categories and descriptions

### 🔄 **Settlement System**
- Smart settlement calculations
- Settlement requests and confirmations
- Payment tracking
- Balance optimization

### 📱 **Real-time Features**
- Live notifications
- Activity feed updates
- Group member updates
- Settlement status changes

### 📎 **File Management**
- Receipt image uploads
- Document attachments
- File validation and security
- Storage management

### **Activity Feed:**
- Track expense activity
- Track Group activity
- Track Settlement activity
  
## 🏗️ Architecture

### Backend (Spring Boot)
```
src/main/java/com/dasa/splitspends/
├── entity/          # JPA entities
├── repository/      # Data access layer
├── service/         # Business logic
├── controller/      # REST endpoints
└── config/          # Configuration classes
```

### Frontend (Flutter)
```
lib/
├── models/          # Data models
├── services/        # API services
├── screens/         # UI screens
├── widgets/         # Reusable widgets
└── utils/           # Utility functions
```

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.5.4
- **Language**: Java 17
- **Database**: PostgreSQL
- **Security**: Spring Security + JWT
- **Documentation**: Swagger/OpenAPI
- **Build Tool**: Maven

### Frontend
- **Framework**: Flutter
- **Language**: Dart
- **State Management**: Provider/Bloc
- **HTTP Client**: Dio
- **Local Storage**: SharedPreferences/Hive

### DevOps & Tools
- **Version Control**: Git
- **IDE**: VS Code, IntelliJ IDEA
- **API Testing**: Postman
- **Database**: PostgreSQL
- **Deployment**: Docker, Railway/Heroku

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Flutter SDK
- PostgreSQL
- Maven
- Git

### Backend Setup
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
The backend will be available at `http://localhost:8080`

### Frontend Setup
```bash
cd frontend
flutter pub get
flutter run
```

### Database Setup
1. Install PostgreSQL
2. Create database: `splitspends_db`
3. Update `application.properties` with your database credentials

## 📚 Documentation

- [API Documentation](docs/API.md)
- [Database Schema](docs/DATABASE.md)
- [Architecture Guide](docs/ARCHITECTURE.md)
- [Deployment Guide](docs/DEPLOYMENT.md)
- [Development Setup](docs/SETUP.md)

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
flutter test
```

## 📱 Screenshots

| Home Screen | Groups | Expenses | Settlements |
|-------------|--------|----------|-------------|
| ![Home](docs/images/home.png) | ![Groups](docs/images/groups.png) | ![Expenses](docs/images/expenses.png) | ![Settlements](docs/images/settlements.png) |

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Nagasagar Dasa**
- GitHub: [@nagasagar](https://github.com/nagasagar)
- Email: your.email@example.com

## 🙏 Acknowledgments

- Spring Boot community for excellent documentation
- Flutter team for the amazing framework
- All contributors who help make this project better


## 📊 Project Status

- ✅ Backend API development
- ✅ Database design and implementation
- ✅ Authentication system
- ✅ Group management
- ✅ Expense tracking
- ✅ Settlement system
- 🚧 Frontend development (in progress)
- 🚧 Real-time notifications
- 🚧 Mobile app testing
- 🚧 Deployment setup
- 🛠️ Email verification on signup (pending)

---

**Made with ❤️ for better expense sharing**
