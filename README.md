# Recipes REST API

A comprehensive REST API for managing cooking recipes with JWT-based authentication, built using Spring Boot.

## Features

- **JWT Authentication**: Secure user authentication and authorization
- **User Management**: User registration, login, and profile management
- **Recipe Management**: Full CRUD operations for recipes
- **Advanced Search**: Filter recipes by cuisine, meal type, difficulty, and dietary restrictions
- **Review System**: Rate and review recipes
- **Pagination**: Efficient data retrieval with pagination support
- **API Documentation**: Swagger/OpenAPI documentation
- **H2 Database**: In-memory database for development and testing

## Technology Stack

- **Java 17**
- **Spring Boot 3.5.5**
- **Spring Security** with JWT
- **Spring Data JPA**
- **H2 Database**
- **Maven**
- **Swagger/OpenAPI 3**

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Environment Setup

### 1. Configure Environment Variables

The application uses environment variables for sensitive configuration. Copy the example file and configure your environment:

```bash
cp .env.example .env
```

Edit the `.env` file with your configuration values:

```bash
# Generate a secure JWT secret (REQUIRED - no fallback)
JWT_SECRET=$(openssl rand -base64 32)

# Database Configuration (choose one)
# For H2 (Development/Testing)
DB_URL=jdbc:h2:mem:recipesdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
DB_DRIVER=org.h2.Driver
DB_USERNAME=sa
DB_PASSWORD=your-secure-db-password

# For PostgreSQL (Production)
# DB_URL=jdbc:postgresql://localhost:5432/recipesdb
# DB_DRIVER=org.postgresql.Driver
# DB_USERNAME=your-db-username
# DB_PASSWORD=your-db-password

# For MySQL (Production)
# DB_URL=jdbc:mysql://localhost:3306/recipesdb?useSSL=false&serverTimezone=UTC
# DB_DRIVER=com.mysql.cj.jdbc.Driver
# DB_USERNAME=your-db-username
# DB_PASSWORD=your-db-password

# Default User Configuration (for initial setup)
# Note: These are used by DataInitializer to create default users
# The application uses JWT-based authentication with custom user management
DEFAULT_ADMIN_USERNAME=admin
DEFAULT_ADMIN_EMAIL=admin@recipes.com
DEFAULT_ADMIN_FIRST_NAME=Admin
DEFAULT_ADMIN_LAST_NAME=User
DEFAULT_USER_USERNAME=chef
DEFAULT_USER_EMAIL=chef@recipes.com
DEFAULT_USER_FIRST_NAME=Master
DEFAULT_USER_LAST_NAME=Chef

# Test Configuration (REQUIRED for testing - no fallback)
TEST_ADMIN_PASSWORD=your-test-admin-password
TEST_USER_PASSWORD=your-test-user-password

# Server Configuration
SERVER_PORT=8080
```

**Important**: Never commit the `.env` file to version control. It's already included in `.gitignore`.

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd recipes
```

### 2. Build the Project

```bash
mvn clean install
```

### 3. Run the Application

#### Development Mode (Default)
```bash
mvn spring-boot:run
```

#### With Specific Profile
```bash
# Development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Production profile
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Test profile
mvn test -Dspring.profiles.active=test
```

The application will start on `http://localhost:8080`

### 4. Access the API

- **API Base URL**: `http://localhost:8080/api`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **H2 Console**: `http://localhost:8080/h2-console`

## Default Users

The application comes with pre-configured users for testing purposes. Please refer to the application configuration or contact the administrator for access credentials.

## API Endpoints

### Authentication

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "email": "user@example.com",
  "password": "your_secure_password",
  "confirmPassword": "your_secure_password",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "your_username",
  "password": "your_password"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresAt": "2024-01-15T10:30:00",
  "user": {
    "id": 2,
    "username": "your_username",
    "email": "your_email@example.com",
    "firstName": "Master",
    "lastName": "Chef",
    "role": "USER"
  }
}
```

### Recipes

#### Get All Public Recipes
```http
GET /api/recipes?page=0&size=10&sortBy=createdAt&sortDir=desc
Authorization: Bearer <jwt-token>
```

#### Get Recipe by ID
```http
GET /api/recipes/{id}
Authorization: Bearer <jwt-token>
```

#### Create Recipe
```http
POST /api/recipes
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "title": "Pasta Carbonara",
  "description": "Classic Italian pasta dish",
  "prepTime": 15,
  "cookTime": 20,
  "servings": 4,
  "difficultyLevel": "MEDIUM",
  "cuisineType": "Italian",
  "mealType": "MAIN_COURSE",
  "dietaryRestrictions": ["VEGETARIAN"],
  "isPublic": true
}
```

#### Update Recipe
```http
PUT /api/recipes/{id}
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "title": "Updated Pasta Carbonara",
  "description": "Updated description"
}
```

#### Delete Recipe
```http
DELETE /api/recipes/{id}
Authorization: Bearer <jwt-token>
```

#### Search Recipes
```http
GET /api/recipes/search?title=carbonara&page=0&size=10
Authorization: Bearer <jwt-token>
```

#### Filter Recipes
```http
GET /api/recipes/filter?cuisineType=Italian&mealType=MAIN_COURSE&difficultyLevel=MEDIUM&maxPrepTime=30&maxCookTime=45&page=0&size=10
Authorization: Bearer <jwt-token>
```

#### Get Recipes by Cuisine
```http
GET /api/recipes/cuisine/Italian?page=0&size=10
Authorization: Bearer <jwt-token>
```

#### Get Recipes by Meal Type
```http
GET /api/recipes/meal-type/MAIN_COURSE?page=0&size=10
Authorization: Bearer <jwt-token>
```

#### Get Top Rated Recipes
```http
GET /api/recipes/top-rated
Authorization: Bearer <jwt-token>
```

#### Get User's Recipes
```http
GET /api/recipes/my-recipes?page=0&size=10
Authorization: Bearer <jwt-token>
```

### Reviews

#### Create Review
```http
POST /api/recipes/{recipeId}/reviews
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "rating": 5,
  "comment": "Excellent recipe! Very easy to follow."
}
```

#### Get Recipe Reviews
```http
GET /api/recipes/{recipeId}/reviews
Authorization: Bearer <jwt-token>
```

#### Update Review
```http
PUT /api/recipes/{recipeId}/reviews/{reviewId}
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "rating": 4,
  "comment": "Updated comment"
}
```

#### Delete Review
```http
DELETE /api/recipes/{recipeId}/reviews/{reviewId}
Authorization: Bearer <jwt-token>
```

## Data Models

### Recipe
- `id`: Unique identifier
- `title`: Recipe title
- `description`: Recipe description
- `prepTime`: Preparation time in minutes
- `cookTime`: Cooking time in minutes
- `totalTime`: Total time (auto-calculated)
- `servings`: Number of servings
- `difficultyLevel`: EASY, MEDIUM, HARD, EXPERT
- `cuisineType`: Type of cuisine
- `mealType`: BREAKFAST, LUNCH, DINNER, SNACK, DESSERT, etc.
- `dietaryRestrictions`: Array of dietary restrictions
- `rating`: Average rating (0.0 - 5.0)
- `ratingCount`: Number of ratings
- `favoriteCount`: Number of favorites
- `isPublic`: Whether recipe is public
- `user`: Recipe creator
- `ingredients`: List of ingredients
- `instructions`: List of cooking instructions
- `reviews`: List of reviews

### Ingredient
- `id`: Unique identifier
- `name`: Ingredient name
- `amount`: Quantity
- `unit`: Unit of measurement
- `notes`: Additional notes
- `optional`: Whether ingredient is optional
- `recipe`: Associated recipe

### Instruction
- `id`: Unique identifier
- `stepNumber`: Step number
- `description`: Step description
- `estimatedTime`: Estimated time for step
- `tips`: Cooking tips
- `imageUrl`: Optional image URL
- `recipe`: Associated recipe

### Review
- `id`: Unique identifier
- `rating`: Rating (1-5)
- `comment`: Review comment
- `user`: Review author
- `recipe`: Reviewed recipe
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp

## Security

- **JWT Tokens**: Stateless authentication with configurable expiration
- **Password Encryption**: BCrypt password hashing
- **Role-based Access**: USER and ADMIN roles
- **Custom User Management**: UserService implements UserDetailsService
- **Protected Endpoints**: Most endpoints require authentication
- **Public Endpoints**: Only `/auth/**` endpoints are public
- **No Default Users**: No hardcoded Spring Security users
- **Environment-based Configuration**: All sensitive data via environment variables
- **No Hardcoded Secrets**: All secrets require environment variables (no fallbacks)
- **Configurable Default Users**: Default user data is configurable via environment variables
- **Secure Actuator Endpoints**: Only health and info endpoints are public
- **Production H2 Console**: Disabled in production profile

## Configuration

### Application Properties
- **Server Port**: Configurable via `SERVER_PORT` (default: 8080)
- **Context Path**: /api
- **Database**: H2 in-memory (development), PostgreSQL/MySQL (production)
- **JWT Secret**: Required via `JWT_SECRET` environment variable
- **JWT Expiration**: 24 hours (86400000 ms)

### Customization
You can modify the following properties in `application.properties`:
- JWT secret key
- JWT expiration time
- Database configuration
- Server settings

## Development

### Project Structure
```
src/main/java/com/lric3/recipes/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/            # Data Transfer Objects
├── entity/         # JPA entities
├── repository/     # Data access layer
├── security/       # Security configuration
└── service/        # Business logic
```

### Adding New Features
1. Create entity classes in the `entity` package
2. Create repository interfaces in the `repository` package
3. Implement business logic in the `service` package
4. Create DTOs in the `dto` package
5. Implement REST endpoints in the `controller` package
6. Add security configuration if needed

## Testing

### Run Tests

#### Default Test Run
```bash
mvn test
```

#### With Test Profile
```bash
mvn test -Dspring.profiles.active=test
```

#### Test Configuration
The application uses environment variables for test configuration:
- `TEST_ADMIN_PASSWORD`: Password for test admin user
- `TEST_USER_PASSWORD`: Password for test regular user

Test passwords are centralized in `TestConstants.java` for consistency across all test classes.

### Test Coverage
The project includes unit tests for:
- Services
- Controllers
- Security configuration

## Deployment

### Production Considerations
1. **Environment Variables**: Use environment variables for all sensitive configuration
2. **JWT Secret**: Generate a secure, random JWT secret using `openssl rand -base64 32`
3. **Database**: Use a production database (PostgreSQL, MySQL) with secure credentials
4. **Database Security**: 
   - Use strong, unique database passwords
   - Enable SSL/TLS connections
   - Use connection pooling (HikariCP is configured)
   - Disable H2 console in production
5. **Authentication Security**:
   - No hardcoded Spring Security users (uses custom UserDetailsService)
   - JWT-based stateless authentication
   - BCrypt password hashing
   - Role-based access control
   - Centralized test constants (no hardcoded test passwords)
   - Configurable default user data via environment variables
   - No hardcoded secrets or fallback values
6. **Logging**: Configure proper logging levels (INFO or WARN for production)
7. **Monitoring**: Set up monitoring and health checks
8. **HTTPS**: Use HTTPS in production
9. **CORS**: Configure CORS appropriately for your domain
10. **Credentials**: All credentials are now configurable via environment variables
11. **Secrets Management**: Consider using external secret management (AWS Secrets Manager, HashiCorp Vault)
12. **Profiles**: Use the `prod` profile for production deployment
13. **Environment Variables**: All sensitive configuration requires environment variables
14. **Actuator Security**: Only health and info endpoints are publicly accessible

### Docker Support
```bash
# Build Docker image
docker build -t recipes-api .

# Run Docker container with environment variables
docker run -p 8080:8080 \
  -e JWT_SECRET="your-jwt-secret" \
  -e DB_URL="jdbc:postgresql://host.docker.internal:5432/recipesdb" \
  -e DB_USERNAME="your-db-username" \
  -e DB_PASSWORD="your-db-password" \
  -e DEFAULT_ADMIN_USERNAME="admin" \
  -e DEFAULT_ADMIN_EMAIL="admin@recipes.com" \
  -e TEST_ADMIN_PASSWORD="your-test-admin-password" \
  -e TEST_USER_PASSWORD="your-test-user-password" \
  recipes-api
```

### Database Setup

#### H2 Database (Development/Testing)
- **Default**: In-memory database, no setup required
- **Persistent**: Change URL to `jdbc:h2:file:./data/recipesdb`

#### PostgreSQL (Production)
```sql
-- Create database
CREATE DATABASE recipesdb;

-- Create user
CREATE USER recipes_user WITH PASSWORD 'your-secure-password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE recipesdb TO recipes_user;
```

#### MySQL (Production)
```sql
-- Create database
CREATE DATABASE recipesdb;

-- Create user
CREATE USER 'recipes_user'@'%' IDENTIFIED BY 'your-secure-password';

-- Grant privileges
GRANT ALL PRIVILEGES ON recipesdb.* TO 'recipes_user'@'%';
FLUSH PRIVILEGES;
```

## API Documentation

The API is fully documented using Swagger/OpenAPI 3. Access the interactive documentation at:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For questions and support, please open an issue in the repository.
