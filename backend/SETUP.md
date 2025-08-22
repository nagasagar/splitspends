# SplitSpends Backend Setup Guide

This guide will help you set up the SplitSpends backend (Spring Boot) for local development.

## Prerequisites

- **Java 17** (JDK 17 or higher)
- **Maven** (3.8+ recommended)
- **PostgreSQL** (or compatible database)
- (Optional) **Git** for version control

## 1. Clone the Repository

If you haven't already, clone the monorepo:
```bash
git clone https://github.com/yourusername/splitspends.git
cd splitspends/backend
```

## 2. Configure Secrets

Before running the backend, create a file named `application-secrets.properties` in the `backend/` directory. This file should contain sensitive configuration such as database credentials, JWT secret, and email credentials.

**Example `backend/application-secrets.properties`:**
```
spring.datasource.username=your_db_user
spring.datasource.password=your_db_password
jwt.secret=your_strong_jwt_secret
spring.mail.username=your_email_username
spring.mail.password=your_email_app_password
```
- **Never commit this file to git!** It is already included in `.gitignore`.
- Each developer should create their own copy with local values.

> Note: The `jwt.expiration` value is not a secret and should be set in `application.properties`.

## 3. Set Up the Database

- Ensure PostgreSQL is running.
- Create a database named `splitspends` (or update the DB name in `application.properties`).
- (Optional) Create the user and grant privileges as needed.

## 4. Install Dependencies

```bash
mvn clean install
```

## 5. Run the Backend

```bash
mvn spring-boot:run --spring.config.additional-location=application-secrets.properties
```

The backend will start on port 8080 by default. You can test the API or access Swagger UI at `http://localhost:8080/swagger-ui.html` (if enabled).

---

For more details, see the main project `SETUP.md` or `README.md`.