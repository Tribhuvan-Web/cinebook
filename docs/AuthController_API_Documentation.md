# AuthController - Complete API Documentation

This document provides comprehensive documentation for the AuthController in the MovieDekho application, covering user authentication, registration, password management, and OTP-based operations.

## Base URL
Assuming the application runs on localhost:8080, the base URL is:
```
http://localhost:8080/api/auth
```

---

## 🔐 AUTHENTICATION ENDPOINTS

### 1. User Registration
**Endpoint:** `POST /register`

Registers a new user in the system.

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "SecurePassword123",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "gender": "Male"
  }'
```

### 2. User Login (Traditional)
**Endpoint:** `POST /login`

Traditional login with username/email and password.

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "SecurePassword123"
  }'
```

### 3. Initiate OTP Login
**Endpoint:** `POST /initiate-login`

Initiates OTP-based login by sending OTP to user's email.

```bash
# Login with email
curl -X POST http://localhost:8080/api/auth/initiate-login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com"
  }'

# Login with phone number
curl -X POST http://localhost:8080/api/auth/initiate-login \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+1234567890"
  }'
```

### 4. Verify OTP for Login
**Endpoint:** `POST /verify-otp`

Verifies the OTP and completes the login process.

```bash
# Verify with email
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "otp": "123456"
  }'

# Verify with phone number
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+1234567890",
    "otp": "123456"
  }'
```

---

## 🔑 PASSWORD MANAGEMENT

### 5. Forgot Password
**Endpoint:** `POST /forgot-password`

Initiates password reset by sending OTP for verification.

```bash
# Forgot password with email
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com"
  }'

# Forgot password with phone number
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+1234567890"
  }'
```

### 6. Reset Password
**Endpoint:** `POST /reset-password`

Resets password after OTP verification.

```bash
# Reset password with email
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "otp": "123456",
    "newPassword": "NewSecurePassword123"
  }'

# Reset password with phone number
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+1234567890",
    "otp": "123456",
    "newPassword": "NewSecurePassword123"
  }'
```

---

## 👤 USER INFORMATION

### 7. Get Username
**Endpoint:** `GET /username`

Retrieves the username from JWT token.

```bash
curl -X GET http://localhost:8080/api/auth/username \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Accept: application/json"
```

---

## 📊 EXAMPLE RESPONSES

### Successful Registration
```json
"User registered successfully"
```

### Successful Login Response
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "username": "john_doe",
  "email": "john.doe@example.com",
  "role": "Role_User"
}
```

### OTP Sent Response
```json
"OTP sent successfully"
```

### Password Reset Success
```json
"Password reset successfully"
```

### Username Response
```json
"john_doe"
```

### Error Responses

**409 - Conflict (User already exists):**
```json
"Email/phone already exist"
```

**401 - Unauthorized (Invalid credentials):**
```json
"Invalid credentials"
```

**401 - Unauthorized (Invalid OTP):**
```json
"Invalid OTP"
```

**404 - Not Found (User not found):**
```json
"User not found"
```

**400 - Bad Request:**
```json
"Must provide email or phone number"
```

**400 - Bad Request (Invalid password format):**
```json
"Invalid password format"
```

---

## 📋 DATA TRANSFER OBJECTS (DTOs)

### RegisterUserDTO (For Registration)
```json
{
  "username": "john_doe",
  "password": "SecurePassword123",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "gender": "Male"
}
```

### LoginUserDTO (For Traditional Login)
```json
{
  "username": "john_doe",
  "password": "SecurePassword123"
}
```

### LoginInitiationDto (For OTP Login/Password Reset)
```json
{
  "email": "john.doe@example.com"
}
```
**OR**
```json
{
  "phoneNumber": "+1234567890"
}
```

### OtpVerificationDto (For OTP Verification)
```json
{
  "email": "john.doe@example.com",
  "otp": "123456"
}
```
**OR**
```json
{
  "phoneNumber": "+1234567890",
  "otp": "123456"
}
```

### PasswordResetDto (For Password Reset)
```json
{
  "email": "john.doe@example.com",
  "otp": "123456",
  "newPassword": "NewSecurePassword123"
}
```
**OR**
```json
{
  "phoneNumber": "+1234567890",
  "otp": "123456",
  "newPassword": "NewSecurePassword123"
}
```

### JwtAuthenticationResponse (Login Response)
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "username": "john_doe",
  "email": "john.doe@example.com",
  "role": "Role_User"
}
```

---

## 🎭 REAL-WORLD SCENARIOS

### Scenario 1: New User Registration and Login

```bash
# Step 1: Register new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice_smith",
    "password": "MySecurePass456",
    "email": "alice.smith@example.com",
    "phone": "+1987654321",
    "gender": "Female"
  }'

# Step 2: Traditional login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice_smith",
    "password": "MySecurePass456"
  }'
```

### Scenario 2: OTP-Based Login (Passwordless)

```bash
# Step 1: Initiate OTP login
curl -X POST http://localhost:8080/api/auth/initiate-login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice.smith@example.com"
  }'

# Step 2: Verify OTP and login
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice.smith@example.com",
    "otp": "654321"
  }'
```

### Scenario 3: Forgot Password Flow

```bash
# Step 1: Initiate password reset
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice.smith@example.com"
  }'

# Step 2: Reset password with OTP
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice.smith@example.com",
    "otp": "789012",
    "newPassword": "MyNewSecurePass789"
  }'
```

### Scenario 4: Using Phone Number Instead of Email

```bash
# Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bob_jones",
    "password": "BobSecure123",
    "email": "bob.jones@example.com",
    "phone": "+1122334455",
    "gender": "Male"
  }'

# Initiate login with phone number
curl -X POST http://localhost:8080/api/auth/initiate-login \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+1122334455"
  }'

# Verify OTP
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+1122334455",
    "otp": "445566"
  }'
```

---

## 🔧 TESTING DIFFERENT SCENARIOS

### Test User Registration Validation

```bash
# Test duplicate email registration
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "password": "TestPass123",
    "email": "alice.smith@example.com",
    "phone": "+1555666777",
    "gender": "Male"
  }'

# Test duplicate phone registration
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "another_user",
    "password": "TestPass123",
    "email": "new.email@example.com",
    "phone": "+1987654321",
    "gender": "Female"
  }'
```

### Test Invalid Login Attempts

```bash
# Test with wrong password
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice_smith",
    "password": "WrongPassword"
  }'

# Test with non-existent user
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "non_existent_user",
    "password": "SomePassword"
  }'
```

### Test OTP Validation

```bash
# Test wrong OTP
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice.smith@example.com",
    "otp": "000000"
  }'

# Test expired OTP (depends on OTP expiry time)
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice.smith@example.com",
    "otp": "123456"
  }'
```

### Test Missing Required Fields

```bash
# Test registration without required fields
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "incomplete_user"
  }'

# Test login initiation without email or phone
curl -X POST http://localhost:8080/api/auth/initiate-login \
  -H "Content-Type: application/json" \
  -d '{}'
```

---

## 💻 PowerShell Examples (For Windows Users)

```powershell
# Register user using PowerShell
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body '{
    "username": "ps_user",
    "password": "PSPassword123",
    "email": "ps.user@example.com",
    "phone": "+1999888777",
    "gender": "Other"
  }'

# Login user
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body '{
    "username": "ps_user",
    "password": "PSPassword123"
  }'

# Extract token from response
$token = $loginResponse.token

# Get username using token
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/username" `
  -Method GET `
  -Headers @{
    "Authorization"="Bearer $token"
    "Accept"="application/json"
  }

# OTP-based login
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/initiate-login" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body '{
    "email": "ps.user@example.com"
  }'

# Verify OTP (replace with actual OTP received)
Invoke-RestMethod -Uri "http://localhost:8080/api/auth/verify-otp" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body '{
    "email": "ps.user@example.com",
    "otp": "123456"
  }'
```

---

## 🎯 KEY FEATURES

### Authentication Methods:
- ✅ **Traditional Login** - Username/email + password
- ✅ **OTP-based Login** - Passwordless authentication via email OTP
- ✅ **Dual Identifier Support** - Login with email or phone number
- ✅ **JWT Token Authentication** - Secure token-based sessions

### Security Features:
- ✅ **Password Encryption** - Secure password hashing
- ✅ **OTP Verification** - Time-limited OTP validation
- ✅ **Duplicate Prevention** - Unique email and phone constraints
- ✅ **JWT Token Management** - Secure token generation and validation

### User Management:
- ✅ **User Registration** - Complete user profile creation
- ✅ **Password Reset** - Secure password recovery via OTP
- ✅ **Phone/Email Flexibility** - Multiple ways to identify users
- ✅ **Role-based Access** - User role assignment

### Email Integration:
- ✅ **OTP Email Delivery** - Automated OTP sending via Brevo
- ✅ **Password Reset Emails** - Dedicated password reset communication
- ✅ **Professional Email Templates** - Branded email communications

---

## 📝 IMPORTANT NOTES

1. **OTP Expiry:** OTPs have a limited validity period (check OtpService configuration)
2. **Rate Limiting:** Consider implementing rate limiting for OTP requests
3. **Email Validation:** Ensure email addresses are properly validated
4. **Phone Format:** Use international phone number format (+countrycode)
5. **Password Strength:** Implement strong password requirements
6. **JWT Security:** Keep JWT tokens secure and implement proper token refresh
7. **HTTPS Required:** Always use HTTPS in production for security
8. **Account Lockout:** Consider implementing account lockout after failed attempts

---

## 🔄 INTEGRATION PATTERNS

### Frontend Integration:
```javascript
// Example frontend integration
const login = async (username, password) => {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });
  
  if (response.ok) {
    const data = await response.json();
    localStorage.setItem('token', data.token);
    localStorage.setItem('username', data.username);
  }
};

const otpLogin = async (email) => {
  // Step 1: Initiate OTP
  await fetch('/api/auth/initiate-login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email })
  });
  
  // Step 2: Get OTP from user and verify
  const otp = prompt('Enter OTP:');
  const response = await fetch('/api/auth/verify-otp', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, otp })
  });
  
  if (response.ok) {
    const data = await response.json();
    localStorage.setItem('token', data.token);
  }
};
```

### Protected Route Usage:
```bash
# Use JWT token for protected endpoints
curl -X GET http://localhost:8080/api/protected-endpoint \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Accept: application/json"
```

---

## 🚀 GETTING STARTED

1. **Register:** Create a new user account with complete profile
2. **Login:** Use traditional or OTP-based authentication
3. **Secure Access:** Store and use JWT tokens for protected routes
4. **Password Management:** Use forgot/reset password when needed
5. **Profile Management:** Access user information via username endpoint
