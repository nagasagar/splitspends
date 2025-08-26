# QA Testing Profile

This document describes how to use the QA profile for testing the SplitSpends application.

## Overview

The QA profile automatically creates demo data including:
- 5 demo users (including admin and super admin)
- 3 demo groups with different configurations
- 5 demo expenses with splits

## Getting Started

### Method 1: Using VS Code Launch Configuration
1. Open VS Code
2. Go to Run and Debug (Ctrl+Shift+D)
3. Select "SplitSpends Backend (QA)" from the dropdown
4. Click the play button

### Method 2: Using VS Code Task
1. Open Command Palette (Ctrl+Shift+P)
2. Type "Tasks: Run Task"
3. Select "Run Spring Boot Backend (QA)"

### Method 3: Using Maven Command Line
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=qa
```

### Method 4: Using Java Arguments
```bash
java -jar target/splitspends-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=qa
```

## Demo Users

The following users are automatically created:

| Email | Password | Role | Description |
|-------|----------|------|-------------|
| alice.qa@example.com | password123 | USER | Regular user, part of all groups |
| bob.qa@example.com | password123 | USER | Regular user, admin of work group |
| charlie.qa@example.com | password123 | USER | Regular user, admin of trip group |
| admin.qa@example.com | password123 | ADMIN | System administrator |
| superadmin.qa@example.com | password123 | SUPER_ADMIN | Super administrator |

## Demo Groups

1. **Friends Group**
   - Members: Alice, Bob, Charlie
   - Admin: Alice
   - Expenses: Dinner, Movie Tickets

2. **Work Lunch**
   - Members: Alice, Bob
   - Admin: Bob
   - Expenses: Office Lunch

3. **Weekend Trip**
   - Members: Alice, Bob, Charlie
   - Admins: Charlie, Alice
   - Expenses: Hotel, Gas

## Demo Expenses

1. **Dinner at Restaurant** - $120.00 (paid by Alice, split equally)
2. **Office Lunch** - $45.50 (paid by Bob, split equally)
3. **Hotel Booking** - $300.00 (paid by Charlie, split equally)
4. **Gas for Trip** - $80.25 (paid by Alice, split equally)
5. **Movie Tickets** - $36.00 (paid by Bob, split equally)

## Database Access

When running in QA mode, you can access the H2 database console at:
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:splitspendsqa
- Username: sa
- Password: (empty)

## API Testing

You can test the API endpoints with the created data:

### Authentication
```bash
POST /api/auth/login
{
  "email": "alice.qa@example.com",
  "password": "password123"
}
```

### Get User Groups
```bash
GET /api/groups/user/1
Authorization: Bearer <token>
```

### Get Group Expenses
```bash
GET /api/expenses/group/1
Authorization: Bearer <token>
```

## Features to Test

With the QA data, you can test:
- User authentication and authorization
- Group management (viewing, updating, member management)
- Expense creation and management
- Expense splitting functionality
- Settlement tracking
- Admin and super admin features
- Authorization rules and permissions

## Resetting Data

To reset the demo data:
1. Stop the application
2. Restart with the QA profile
3. All data will be recreated fresh

## Notes

- The QA profile uses an in-memory H2 database
- All data is lost when the application stops
- Email verification is disabled for QA users
- Some expense splits are randomly marked as settled for realistic testing
- Expenses have random dates within the last 30 days
