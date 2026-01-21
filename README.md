# 📌 LibriUsers-MS

Microservice responsible for **user authentication, registration, and user data management** in the LibriBookshelf microservices ecosystem.  
It handles login, JWT token generation & validation, user lookup, and integrates with other services such as the API Gateway. :contentReference[oaicite:0]{index=0}

---

## 🧠 Overview

LibriUsers-MS manages all user-related functionality including:

- **User registration**
- **User login with JWT authentication**
- **JWT token generation and validation**
- **Token invalidation (logout/blacklist)**
- **User lookup by ID**
- **Integration with Spring Security**

This service is intended to be used as an **authentication provider** for other services via JWT. :contentReference[oaicite:1]{index=1}

---

## 🚀 Features

### 🔐 Authentication

- Login with email and password
- JWT token issuance
- Token validation on incoming requests
- Token blacklist support (logout implementation)

### 👤 User Management

- Register new users
- Persist users in database
- Lookup user by ID
- Integration with service clients (e.g., Gateway/Review)

### 🔒 Security

- Uses **Spring Security**
- Stateless session management
- Custom security filter for JWT validation
- BCrypt hashing for passwords

---

## 🏛️ Architecture

                                  +--------------------+
                                  |      API Gateway   |
                                  +--------------------+
                                            |
                               +----------------------------+
                               |      LibriUsers-MS         |
                               +----------------------------+
                               | - Login Controller         |
                               | - User Controller          |
                               | - JWT TokenService         |
                               | - SecurityFilter + Config  |
                               +----------------------------+
                                            |
               +------------------------------------------------+
               |                                                |
       +--------------+                             +------------------+
       | Database (User)|                          | Other Microservices |
       +--------------+                             +------------------+
        (User cache/DB)                                     (Gateway, etc.)


---

## 🛠️ Technologies Used

- **Spring Boot**
- **Spring Security**
- **JWT (Auth0)**
- **BCrypt Password Encoding**
- **Jakarta Transactions**
- **Spring Web**
- **Spring Data (Repository implementation)**
- **Maven Build System**

---

## 📦 Requirements

Make sure the following are available:

- Java 17+
- Maven 3+
- PostgreSQL or configured database
- API Gateway (if used)
- Other dependent services as needed (Eureka, etc.)

---

## 📥 Environment Configuration

Create an `application.yml` or define environment variables for:

```yaml
spring:
  application:
    name: user-service

  datasource:
    url: jdbc:postgresql://localhost:5432/libri-users
    username: user
    password: 123

jwt:
  secret: my-very-strong-secret-key-of-32-characters

spring:
  security:
    allowed-endpoints:
      - /auth/register
      - /auth/login
