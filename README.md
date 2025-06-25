# Bank Cards Management API

A secure and robust backend system for managing bank cards, built with Java and Spring Boot. The application supports user authentication, role-based authorization, card operations (CRUD), balance transfers, pagination, filtering, and comprehensive error handling.

---

## 🚀 Features

### 👤 Authentication & Authorization
- JWT-based authentication
- Role-based access control: `ADMIN`, `USER`
- Data encryption

### 👨‍💼 Admin Capabilities
- Create, activate, block, and delete cards
- Manage users
- View all cards in the system

### 👤 User Capabilities
- View own cards (with pagination and filtering)
- Request card blocking
- Transfer funds between own cards
- View total balance

### 💾 Database
- PostgreSQL
- Managed via Liquibase migrations

### 🧪 Testing
- Unit tests for key business logic
- Security and role access tests
- REST controller tests with `MockMvc`

### 📄 Logging
- Console logging for development
- File-based logging with daily and size-based rotation
- Separate log files for all events (app.log) and for errors (error.log)
- Environment-configurable log level and file path

---

## 🔧 Tech Stack
- Java 17+
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Liquibase
- JWT
- Swagger / OpenAPI
- Docker / Docker Compose
- JUnit + Mockito

---

## 🐳 Local Development Setup

### ✅ Prerequisites
- Java 17+
- Docker & Docker Compose
- Maven or your preferred build tool

### ⚙️ 1. Clone the repository

```bash
git clone https://github.com/anatoliydrake/bank-card-management-system.git
cd bank-card-management-system
```

### ⚙️ 2. Configure environment variables
Create a .env file in the project root with the required environment variables.
Then edit .env to set your own values for:
```env
POSTGRES_DB=bank_db
POSTGRES_USER=admin
POSTGRES_PASSWORD=admin
JWT_SECRET=your_jwt_secret_key
ENCRYPTION_SECRET=1234567890abcdef
```
Or you can use the provided .env.example file as a template.

**Note**: Do not commit the .env file to version control.

### ⚙️ 3. Run the app with Docker Compose

```bash
docker-compose up --build
```
This will start the Spring Boot application and the PostgreSQL database with the configured environment.

### 🛡 Authentication
Obtain JWT Token

1. Use the /auth/login endpoint to authenticate and receive a JWT token:

POST http://localhost:8080/auth/login

Request body:
```json
{
  "username": "admin",
  "password": "admin"
}
```
2. Authorize API Requests

Include the token in the Authorization header for all secured endpoints:

```makefile
Authorization: Bearer <your_token_here>
```

In Postman, go to the Authorization tab and choose Bearer Token.

---

### 🧪 Running Tests
```bash
./mvnw test
```

#### Tests include:

- Controller tests using MockMvc
- Security role tests with @WithMockUser
- Business logic unit tests with Mockito

---

## 🔐 Security Highlights
- JWT-based authentication for stateless sessions
- Enforced route-level and method-level access control
- BCrypt password hashing
- Card number encrypting with AES and masking (e.g., **** **** **** 1234)
- Exception handling via @RestControllerAdvice

---
## 📘 API Documentation
Once the app is running, access Swagger UI at:
```
http://localhost:8080/swagger-ui/index.html
```
Or see the OpenAPI definition at:
```
http://localhost:8080/v3/api-docs
```
and in docs/openapi.yaml

---

## 📝 License
This project is for educational and demonstration purposes.