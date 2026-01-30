# AuthController - Complete API Documentation

This document provides comprehensive documentation for the AuthController in the MovieDekho application, covering user
authentication, registration, password management, and OTP-based operations.

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
    "username": "Tribhuvan nath sagar",
    "password": "Reyansh@2004",
    "email"tribhu@gmail.com",
    "phone": "+91234567890",
    "gender": "Male"
  }'
```

### 2. User Login

**Endpoint:** `POST /login`

login with username/email/phone and password.

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "Tribhuvan nath sagar",
    "password": "Reyansh@2004"
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
    "email"tribhu@gmail.com"
  }'

# Login with phone number
curl -X POST http://localhost:8080/api/auth/initiate-login \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+91234567890"
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
    "email": "tribhu@gmail.com",
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
    "email": "tribhu@gmail.com"
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
    "email": "tribhu@gmail.com",
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
  "email": "tribhu@gmail.com",
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

## 🎭 REAL-WORLD SCENARIOS

### Scenario 1: New User Registration and Login

```bash
# Step 1: Register new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice_smith",
    "password": "MySecurePass456",
    "email": "alitribhu@gmail.com",
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
    "email": "alitribhu@gmail.com"
  }'

# Step 2: Verify OTP and login
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alitribhu@gmail.com",
    "otp": "654321"
  }'
```

### Scenario 3: Forgot Password Flow

```bash
# Step 1: Initiate password reset
curl -X POST http://localhost:8080/api/auth/forgot-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alitribhu@gmail.com"
  }'

# Step 2: Reset password with OTP
curl -X POST http://localhost:8080/api/auth/reset-password \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alitribhu@gmail.com",
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
    "email": "btribhu@gmail.com",
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

## 🎯 KEY FEATURES

### Authentication Methods:

- ✅ **Traditional Login** - phone/email + password
- ✅ **OTP-based Login** - Password less authentication via email OTP
- ✅ **Dual Identifier Support** - Login with email or phone number
- ✅ **JWT Token Authentication** - Secure token-based sessions

### Security Features:

- ✅ **Password Encryption** - Secure password hashing using HMAC256 ALG
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
2. **Rate Limiting:** Implemented Rate Limit of 30sec in OTP Resend request
3. **Email Validation:** Ensure email addresses are properly validated
4. **Password Strength:** Implement strong password requirements
5. **JWT Security:** Keep JWT tokens secure and implement proper token refresh
6. **HTTPS Required:** Always use HTTPS in production for security

---

## 🚀 GETTING STARTED

1. **Register:** Create a new user account with complete profile
2. **Login:** Use traditional or OTP-based authentication
3. **Secure Access:** Store and use JWT tokens for protected routes
4. **Password Management:** Use forgot/reset password when needed
5. **Profile Management:** Access user information via username endpoint
