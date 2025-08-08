# Health First Provider Registration Backend

A Spring Boot application for healthcare provider registration with secure authentication and comprehensive validation.

## Features

- **Secure Provider Registration**: Complete registration flow with validation
- **Password Security**: BCrypt hashing with configurable salt rounds
- **Email Verification**: Automated email notifications
- **Comprehensive Validation**: Input sanitization and business rule validation
- **Database Flexibility**: Supports MySQL, PostgreSQL, and MongoDB
- **RESTful API**: Clean, documented REST endpoints
- **Comprehensive Testing**: Unit and integration tests

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: MySQL/PostgreSQL (JPA/Hibernate)
- **Security**: Spring Security with BCrypt
- **Validation**: Bean Validation (JSR-303)
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Build Tool**: Maven

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+ or PostgreSQL 12+
- SMTP server for email functionality

## Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd health-first-server
```

### 2. Configure Database
Update `src/main/resources/application.yml` with your database credentials:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/health_first
    username: your_username
    password: your_password
```

### 3. Configure Email Settings
Update the email configuration in `application.yml`:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
```

### 4. Build and Run
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Provider Registration
- **POST** `/api/v1/provider/register`
- **Description**: Register a new healthcare provider
- **Content-Type**: `application/json`

#### Request Body
```json
{
  "first_name": "John",
  "last_name": "Doe",
  "email": "john.doe@clinic.com",
  "phone_number": "+1234567890",
  "password": "SecurePassword123!",
  "confirm_password": "SecurePassword123!",
  "specialization": "Cardiology",
  "license_number": "MD123456789",
  "years_of_experience": 10,
  "clinic_address": {
    "street": "123 Medical Center Dr",
    "city": "New York",
    "state": "NY",
    "zip": "10001"
  }
}
```

#### Success Response (201)
```json
{
  "success": true,
  "message": "Provider registered successfully. Verification email sent.",
  "data": {
    "provider_id": "uuid-here",
    "email": "john.doe@clinic.com",
    "verification_status": "PENDING"
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

### Get Specializations
- **GET** `/api/v1/provider/specializations`
- **Description**: Get list of valid medical specializations

### Health Check
- **GET** `/api/v1/provider/health`
- **Description**: Check service health status

## Validation Rules

### Email
- Required and unique
- Must be in valid email format

### Phone Number
- Required and unique
- Must be in international format (e.g., +1234567890)

### Password
- Minimum 8 characters
- Must contain uppercase, lowercase, number, and special character
- Confirmation password must match

### License Number
- Required and unique
- Must be alphanumeric only

### Specialization
- Must be from predefined list
- 3-100 characters

### Clinic Address
- All fields required
- Street: max 200 characters
- City: max 100 characters
- State: max 50 characters
- ZIP: 5-10 characters

## Security Features

- **Password Hashing**: BCrypt with configurable salt rounds (default: 12)
- **Input Sanitization**: All inputs are validated and sanitized
- **CORS Configuration**: Configurable cross-origin requests
- **Error Handling**: Secure error messages without exposing sensitive data

## Database Schema

### Provider Table
```sql
CREATE TABLE providers (
    id UUID PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) UNIQUE NOT NULL,
    years_of_experience INTEGER,
    clinic_street VARCHAR(200) NOT NULL,
    clinic_city VARCHAR(100) NOT NULL,
    clinic_state VARCHAR(50) NOT NULL,
    clinic_zip VARCHAR(10) NOT NULL,
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Categories
```bash
# Unit tests only
mvn test -Dtest=*Test

# Integration tests only
mvn test -Dtest=*IntegrationTest
```

### Test Coverage
The application includes comprehensive tests for:
- Service layer business logic
- Password hashing and validation
- Controller endpoints
- Validation rules
- Error handling

## Configuration

### Environment Variables
- `MAIL_USERNAME`: Email username
- `MAIL_PASSWORD`: Email password
- `JWT_SECRET`: JWT secret key
- `DB_URL`: Database connection URL
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password

### Application Properties
Key configuration options in `application.yml`:
- Database connection settings
- Email server configuration
- Security settings (BCrypt rounds, JWT secret)
- Logging levels

## Error Handling

The application provides detailed error responses with appropriate HTTP status codes:

- **400 Bad Request**: Validation errors
- **409 Conflict**: Duplicate email/phone/license
- **422 Unprocessable Entity**: Business rule violations
- **500 Internal Server Error**: Unexpected errors

## Development

### Project Structure
```
src/
├── main/
│   ├── java/com/healthfirst/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/       # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA entities
│   │   ├── exception/       # Exception handlers
│   │   ├── repository/      # Data access layer
│   │   └── service/         # Business logic
│   └── resources/
│       └── application.yml  # Configuration
└── test/
    └── java/com/healthfirst/
        ├── controller/       # Integration tests
        └── service/         # Unit tests
```

### Adding New Features
1. Create entity classes in `entity/` package
2. Add repository interfaces in `repository/` package
3. Implement business logic in `service/` package
4. Create DTOs in `dto/` package
5. Add REST endpoints in `controller/` package
6. Write comprehensive tests

## Deployment

### Docker
```bash
# Build Docker image
docker build -t health-first-server .

# Run container
docker run -p 8080:8080 health-first-server
```

### Production Considerations
- Use environment variables for sensitive configuration
- Configure proper database connection pooling
- Set up monitoring and logging
- Use HTTPS in production
- Configure proper CORS settings
- Set up email service (SendGrid, AWS SES, etc.)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License. 