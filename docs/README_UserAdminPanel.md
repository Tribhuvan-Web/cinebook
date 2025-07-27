# MovieDekho - User and Admin Panel Setup

## Overview

Your MovieDekho application now includes comprehensive User and Admin panels with role-based access control. This implementation provides:

### ✅ Features Implemented:

1. **Role-Based Security Configuration**
   - User role: `ROLE_USER`
   - Admin role: `ROLE_ADMIN`
   - JWT-based authentication with role validation

2. **User Panel (`/api/user/`)**
   - User dashboard with profile and recent movies
   - Profile management (update email, phone, gender)
   - Password change functionality
   - Placeholders for favorites and booking history

3. **Admin Panel (`/api/admin/`)**
   - Admin dashboard with statistics
   - Complete user management (view, search, update roles, delete)
   - Movie management (create, update, delete)
   - User search functionality

4. **Enhanced Authentication**
   - Separate registration endpoints for users and admins
   - Fixed role consistency throughout the application

## Setup Instructions

### 1. Database Setup
Ensure your database is running and the `application.properties` file is configured correctly.

### 2. Build and Run
```bash
cd MovieDekho
mvn clean install
mvn spring-boot:run
```

### 3. Test the Implementation

#### Create Admin User:
```bash
curl -X POST http://localhost:8080/api/auth/register-admin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123",
    "email": "admin@moviedekho.com",
    "phone": "9999999999",
    "gender": "Male"
  }'
```

#### Create Regular User:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "user123", 
    "email": "user@moviedekho.com",
    "phone": "8888888888",
    "gender": "Female"
  }'
```

#### Login and Get JWT Token:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

#### Test Admin Panel (use JWT token from login):
```bash
curl -X GET http://localhost:8080/api/admin/dashboard/stats \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Test User Panel:
```bash
curl -X GET http://localhost:8080/api/user/dashboard \
  -H "Authorization: Bearer USER_JWT_TOKEN"
```

## API Endpoints Summary with Curl Examples

### Public Endpoints:

#### Register User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123",
    "email": "john@example.com",
    "phone": "1234567890",
    "gender": "Male"
  }'
```

#### Register Admin
```bash
curl -X POST http://localhost:8080/api/auth/register-admin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin_user",
    "password": "admin123",
    "email": "admin@moviedekho.com",
    "phone": "9999999999",
    "gender": "Female"
  }'
```

#### Login (Returns JWT Token)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "password123"
  }'
```

#### Browse Movies (Public Access)
```bash
curl -X GET http://localhost:8080/movies/recent
curl -X GET http://localhost:8080/movies/1
curl -X GET http://localhost:8080/movies/search?query=action
```

### User Panel (`ROLE_USER` required):

#### Get User Dashboard
```bash
curl -X GET http://localhost:8080/api/user/dashboard \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Get User Profile
```bash
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Update User Profile
```bash
curl -X PUT http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newemail@example.com",
    "phone": "9876543210",
    "gender": "Female"
  }'
```

#### Change Password
```bash
curl -X PUT http://localhost:8080/api/user/change-password \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "oldpassword123",
    "newPassword": "newpassword456"
  }'
```

#### Get Favorites (Coming Soon)
```bash
curl -X GET http://localhost:8080/api/user/favorites \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Get Booking History (Coming Soon)
```bash
curl -X GET http://localhost:8080/api/user/bookings \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Admin Panel (`ROLE_ADMIN` required):

#### Get Dashboard Statistics
```bash
curl -X GET http://localhost:8080/api/admin/dashboard/stats \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

#### List All Users (with pagination)
```bash
curl -X GET "http://localhost:8080/api/admin/users?page=0&size=10" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

#### Get User by ID
```bash
curl -X GET http://localhost:8080/api/admin/users/1 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

#### Update User Role
```bash
curl -X PUT "http://localhost:8080/api/admin/users/1/role?role=ROLE_ADMIN" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

#### Delete User
```bash
curl -X DELETE http://localhost:8080/api/admin/users/1 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

#### Search Users
```bash
curl -X GET "http://localhost:8080/api/admin/users/search?query=john" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

#### Create Movie
```bash
curl -X POST http://localhost:8080/api/admin/movies \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "New Movie",
    "description": "A great movie",
    "genre": "Action",
    "duration": 120,
    "releaseDate": "2024-01-01"
  }'
```

#### Update Movie
```bash
curl -X PUT http://localhost:8080/api/admin/movies/1 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Movie Title",
    "description": "Updated description",
    "genre": "Drama",
    "duration": 150
  }'
```

#### Delete Movie
```bash
curl -X DELETE http://localhost:8080/api/admin/movies/1 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

#### Get All Movies (Admin View)
```bash
curl -X GET "http://localhost:8080/api/admin/movies?page=0&size=10" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

### Existing Seat Management (updated with roles):

#### Admin Seat Management
```bash
# Create seats for a slot (Admin only)
curl -X POST http://localhost:8080/api/seats/admin/slot/1 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "rowNumber": "A",
      "seatNumber": 1,
      "price": 250.0,
      "isBooked": false
    }
  ]'

# Update seat (Admin only)
curl -X PUT http://localhost:8080/api/seats/admin/1 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "price": 300.0,
    "isBooked": false
  }'

# Delete seat (Admin only)
curl -X DELETE http://localhost:8080/api/seats/admin/1 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

#### User Seat Viewing
```bash
# Get available seats for a slot
curl -X GET http://localhost:8080/api/seats/slot/1/available \
  -H "Authorization: Bearer USER_JWT_TOKEN"

# Get booked seats for a slot
curl -X GET http://localhost:8080/api/seats/slot/1/booked \
  -H "Authorization: Bearer USER_JWT_TOKEN"

# Check seat availability
curl -X GET http://localhost:8080/api/seats/1/availability \
  -H "Authorization: Bearer USER_JWT_TOKEN"
```

## Security Features

1. **JWT Authentication**: All protected endpoints require valid JWT tokens
2. **Role-Based Access**: Different access levels for users and admins
3. **Password Encryption**: BCrypt encryption for all passwords
4. **Input Validation**: Proper validation for all user inputs
5. **Error Handling**: Comprehensive error responses

## Database Changes

No schema changes were required. The existing `User` model already had the `role` field. The implementation:
- Uses `ROLE_USER` for regular users
- Uses `ROLE_ADMIN` for administrators
- Maintains backward compatibility

## Testing

### Test Endpoints with Curl:

#### Public Access Test
```bash
curl -X GET http://localhost:8080/api/test/public
# Expected: "Public endpoint - accessible to everyone"
```

#### User Access Test (requires ROLE_USER or ROLE_ADMIN)
```bash
curl -X GET http://localhost:8080/api/test/user \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
# Expected: "User endpoint - accessible to users and admins"
```

#### Admin Access Test (requires ROLE_ADMIN only)
```bash
curl -X GET http://localhost:8080/api/test/admin \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
# Expected: "Admin endpoint - accessible to admins only"
```

### Complete Testing Workflow:

#### Step 1: Register Users
```bash
# Register admin
curl -X POST http://localhost:8080/api/auth/register-admin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testadmin",
    "password": "admin123",
    "email": "testadmin@test.com",
    "phone": "1111111111",
    "gender": "Male"
  }'

# Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "user123",
    "email": "testuser@test.com",
    "phone": "2222222222",
    "gender": "Female"
  }'
```

#### Step 2: Login and Save Tokens
```bash
# Login admin (save the JWT token)
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testadmin", "password": "admin123"}' | \
  grep -o '"jwt":"[^"]*"' | sed 's/"jwt":"//' | sed 's/"//')

# Login user (save the JWT token)
USER_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "user123"}' | \
  grep -o '"jwt":"[^"]*"' | sed 's/"jwt":"//' | sed 's/"//')
```

#### Step 3: Test Role-Based Access
```bash
# Test admin dashboard
curl -X GET http://localhost:8080/api/admin/dashboard/stats \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Test user dashboard
curl -X GET http://localhost:8080/api/user/dashboard \
  -H "Authorization: Bearer $USER_TOKEN"

# Test admin access with user token (should fail with 403)
curl -X GET http://localhost:8080/api/admin/dashboard/stats \
  -H "Authorization: Bearer $USER_TOKEN"
```

### Additional Authentication Testing:

#### Password Reset Flow
```bash
# Initiate login (sends OTP)
curl -X POST http://localhost:8080/api/auth/initiate-login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@test.com"
  }'

# Verify OTP and login
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@test.com",
    "otp": "123456"
  }'

# Forgot password
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@test.com"
  }'

# Reset password
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@test.com",
    "otp": "123456",
    "newPassword": "newpassword123"
  }'
```

#### Get Username from Token
```bash
curl -X GET http://localhost:8080/api/auth/username \
  -H "Authorization: Bearer $USER_TOKEN"
```

## Next Steps

1. **Frontend Integration**: Create React/Vue.js components for the user and admin dashboards
2. **Enhanced Features**: 
   - Implement favorites functionality
   - Add booking history
   - Add email notifications
   - Implement user profile pictures
3. **Analytics**: Add more dashboard statistics
4. **Reporting**: Generate admin reports for users and bookings

## Troubleshooting

## Troubleshooting

### Common Issues and Solutions:

#### 1. 401 Unauthorized
```bash
# Check if token is properly formatted
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -v
# Look for "Authorization" header in the request
```

#### 2. 403 Forbidden
```bash
# Verify user role - get user info
curl -X GET http://localhost:8080/api/auth/username \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Check if trying to access admin endpoint with user token
curl -X GET http://localhost:8080/api/admin/dashboard/stats \
  -H "Authorization: Bearer USER_TOKEN"
# Should return 403 for user tokens
```

#### 3. Testing Token Expiration
```bash
# Use an expired or invalid token
curl -X GET http://localhost:8080/api/user/profile \
  -H "Authorization: Bearer invalid_token" \
  -v
# Should return 401
```

#### 4. Test Different Content Types
```bash
# Missing Content-Type header (should fail)
curl -X POST http://localhost:8080/api/auth/register \
  -d '{"username": "test", "password": "test123"}'

# Correct Content-Type header
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "test", "password": "test123"}'
```

#### 5. Validation Testing
```bash
# Test weak password (should fail)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123",
    "email": "test@test.com",
    "phone": "1234567890",
    "gender": "Male"
  }'

# Test duplicate email (should fail on second attempt)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "password": "password123",
    "email": "duplicate@test.com",
    "phone": "1111111111",
    "gender": "Male"
  }'

curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user2",
    "password": "password123",
    "email": "duplicate@test.com",
    "phone": "2222222222",
    "gender": "Female"
  }'
```

### Debug Commands:

#### Check Application Status
```bash
# Health check (if implemented)
curl -X GET http://localhost:8080/actuator/health

# Test basic connectivity
curl -X GET http://localhost:8080/api/test/public
```

#### Check Logs
```bash
# Start application with debug logging
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.org.springframework.security=DEBUG"
```

### Logs to Check:
- Spring Security authentication logs
- JWT token validation logs  
- Database connection logs

The implementation is now complete and ready for testing!
