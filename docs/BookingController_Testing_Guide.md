# BookingController - API Testing Examples

This document provides practical examples for testing the enhanced BookingController with Postman, curl, or any REST client.

## 📋 Pre-requisites

1. **User Registration and Login**
2. **Valid JWT Token**
3. **Available Movie Slots with Seats**

## 🔐 Step 1: Get JWT Token

### Register/Login User
```bash
# Register user (if not already registered)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "John Doe",
    "password": "Password123",
    "email": "john@example.com",
    "phone": "+1234567890",
    "gender": "Male"
  }'

# Login to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john@example.com",
    "password": "Password123"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwicm9sZXMiOiJST0xFX1VTRVIiLCJkaXNwbGF5TmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJqb2huQGV4YW1wbGUuY29tIiwiaWF0IjoxNjkzODQ4MDAwLCJleHAiOjE2OTM5MzQ0MDB9.signature",
  "tokenType": "Bearer",
  "username": "John Doe",
  "email": "john@example.com",
  "role": "Role_User"
}
```

## 🎬 Step 2: Find Available Slots and Seats

### Get Available Movie Slots
```bash
curl -X GET http://localhost:8080/api/slots/available \
  -H "Accept: application/json"
```

### Get Available Seats for a Slot
```bash
curl -X GET http://localhost:8080/api/seats/slot/1/available \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Accept: application/json"
```

**Expected Response:**
```json
[
  {
    "seatId": 1,
    "seatNumber": "A1",
    "booked": false,
    "price": 150.0,
    "slotId": 1
  },
  {
    "seatId": 2,
    "seatNumber": "A2", 
    "booked": false,
    "price": 150.0,
    "slotId": 1
  }
]
```

## 🎫 Step 3: Create Booking with Mock Payment

### Test Case 1: Successful Booking
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "slotId": 1,
    "seatNumbers": ["A1", "A2"],
    "totalAmount": 300.0,
    "paymentMethod": "MOCK_PAYMENT",
    "cardNumber": "4111111111111111",
    "cardHolderName": "John Doe",
    "expiryDate": "12/26",
    "cvv": "123"
  }'
```

**Expected Success Response:**
```json
{
  "bookingId": 1,
  "slotId": 1,
  "movieTitle": "Avengers: Endgame",
  "theaterName": "PVR Cinemas",
  "screenType": "IMAX",
  "showDateTime": "2024-08-05T19:30:00",
  "seatNumbers": ["A1", "A2"],
  "userEmail": "john@example.com",
  "totalAmount": 300.0,
  "bookingTime": "2024-08-04T14:30:00.123456",
  "status": "CONFIRMED",
  "paymentId": "MOCK_PAY_ABC12345",
  "paymentStatus": "COMPLETED"
}
```

### Test Case 2: Payment Failure
```bash
# Use invalid card details to trigger payment failure
curl -X POST http://localhost:8080/api/bookings \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "slotId": 1,
    "seatNumbers": ["B1"],
    "totalAmount": 150.0,
    "paymentMethod": "MOCK_PAYMENT",
    "cardNumber": "123",
    "cardHolderName": "",
    "expiryDate": "13/99",
    "cvv": "12"
  }'
```

**Expected Error Response:**
```json
"Payment failed: Invalid payment details provided"
```

### Test Case 3: Seat Already Booked
```bash
# Try booking the same seats again
curl -X POST http://localhost:8080/api/bookings \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "slotId": 1,
    "seatNumbers": ["A1", "A2"],
    "totalAmount": 300.0,
    "paymentMethod": "MOCK_PAYMENT",
    "cardNumber": "4111111111111111",
    "cardHolderName": "John Doe",
    "expiryDate": "12/26",
    "cvv": "123"
  }'
```

**Expected Error Response:**
```json
"Seat A1 is already booked"
```

## 📖 Step 4: Manage Bookings

### Get My Bookings
```bash
curl -X GET http://localhost:8080/api/bookings/my-bookings \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Specific Booking
```bash
curl -X GET http://localhost:8080/api/bookings/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Cancel My Booking
```bash
curl -X DELETE http://localhost:8080/api/bookings/1/cancel \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Cancellation Response:**
```json
{
  "bookingId": 1,
  "slotId": 1,
  "movieTitle": "Avengers: Endgame",
  "theaterName": "PVR Cinemas",
  "screenType": "IMAX",
  "showDateTime": "2024-08-05T19:30:00",
  "seatNumbers": ["A1", "A2"],
  "userEmail": "john@example.com",
  "totalAmount": 300.0,
  "bookingTime": "2024-08-04T14:30:00.123456",
  "status": "CANCELLED",
  "paymentId": "MOCK_PAY_ABC12345",
  "paymentStatus": "REFUNDED"
}
```

## 🔧 Step 5: Admin Testing (Optional)

### Get All Bookings (Admin)
```bash
curl -X GET http://localhost:8080/api/bookings/admin/all \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

### Cancel User Booking (Admin)
```bash
curl -X DELETE "http://localhost:8080/api/bookings/admin/1/cancel?userEmail=john@example.com" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

## 🧪 Mock Payment Test Cases

### Different Card Types
```json
// Visa Card
{
  "cardNumber": "4111111111111111",
  "cardHolderName": "John Doe",
  "expiryDate": "12/26",
  "cvv": "123"
}

// Mastercard
{
  "cardNumber": "5555555555554444",
  "cardHolderName": "Jane Smith", 
  "expiryDate": "06/25",
  "cvv": "456"
}

// American Express
{
  "cardNumber": "378282246310005",
  "cardHolderName": "Bob Johnson",
  "expiryDate": "09/27",
  "cvv": "1234"
}
```

### Payment Validation Test Cases

#### Valid Payment Details
- Card Number: 13-19 digits
- Card Holder: Non-empty string
- Expiry Date: MM/YY format
- CVV: 3-4 digits

#### Invalid Payment Details (Will Fail)
```json
{
  "cardNumber": "123",           // Too short
  "cardHolderName": "",          // Empty
  "expiryDate": "13/99",         // Invalid month
  "cvv": "12"                    // Too short
}
```

## 🔍 Testing Checklist

### Authentication Tests
- [ ] Booking without JWT token (should fail with 401)
- [ ] Booking with invalid JWT token (should fail with 401)
- [ ] Booking with valid JWT token (should succeed)

### Payment Tests
- [ ] Valid payment details (should succeed ~95% of time)
- [ ] Invalid card number (should fail)
- [ ] Empty card holder name (should fail)
- [ ] Invalid expiry date format (should fail)
- [ ] Invalid CVV (should fail)

### Seat Selection Tests
- [ ] Valid seat numbers (should succeed)
- [ ] Invalid seat numbers (should fail)
- [ ] Already booked seats (should fail)
- [ ] Seats from different slots (should fail)

### Amount Validation Tests
- [ ] Correct total amount (should succeed)
- [ ] Incorrect total amount (should fail)

### Booking Management Tests
- [ ] Get my bookings (should return user's bookings only)
- [ ] Cancel own booking (should succeed)
- [ ] Cancel another user's booking (should fail)
- [ ] Cancel already cancelled booking (should fail)

### Admin Tests
- [ ] Admin get all bookings (should succeed)
- [ ] Admin cancel any booking (should succeed)
- [ ] User accessing admin endpoints (should fail with 403)

## 📝 Notes

1. **JWT Token:** Replace `YOUR_JWT_TOKEN` with actual token from login response
2. **Payment Success Rate:** Mock payment has ~95% success rate for valid details
3. **Seat Numbers:** Use format like "A1", "B2", not seat IDs
4. **User Extraction:** User email automatically extracted from JWT token
5. **Immediate Confirmation:** Successful payments result in CONFIRMED status
6. **Refunds:** Cancellations automatically process mock refunds

This enhanced booking system provides a complete, authenticated movie booking experience with realistic payment processing simulation.
