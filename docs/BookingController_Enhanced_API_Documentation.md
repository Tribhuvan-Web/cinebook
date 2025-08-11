# BookingController - Enhanced API Documentation

This document provides comprehensive documentation for the enhanced BookingController in the MovieDekho application, covering authenticated booking creation with JWT tokens, mock payment integration, and seat number-based booking.

## Base URL
```
http://localhost:8080/api/bookings
```

## 🔐 AUTHENTICATION REQUIRED

All booking endpoints require JWT authentication with `ROLE_USER`. Include the JWT token in the Authorization header:

```
Authorization: Bearer <JWT_TOKEN>
```

## 📋 API ENDPOINTS

### 1. Create Booking (Enhanced with Payment)

**Endpoint:** `POST /api/bookings`

Creates a new booking with authenticated user (from JWT token), seat number selection, and mock payment processing.

**Request Body:**
```json
{
  "slotId": 1,
  "seatNumbers": ["A1", "A2", "B3"],
  "totalAmount": 450.0,
  "paymentMethod": "MOCK_PAYMENT",
  "cardNumber": "4111111111111111",
  "cardHolderName": "John Doe", 
  "expiryDate": "12/26",
  "cvv": "123"
}
```

**curl Example:**
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

### 2. Get My Bookings

**Endpoint:** `GET /api/bookings/my-bookings`

Retrieves all bookings for the authenticated user (user extracted from JWT token).

```bash
curl -X GET http://localhost:8080/api/bookings/my-bookings \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Get Booking by ID

**Endpoint:** `GET /api/bookings/{bookingId}`

Retrieves details of a specific booking.

```bash
curl -X GET http://localhost:8080/api/bookings/123 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Cancel My Booking

**Endpoint:** `DELETE /api/bookings/{bookingId}/cancel`

Cancels a booking owned by the authenticated user.

```bash
curl -X DELETE http://localhost:8080/api/bookings/123/cancel \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 5. Confirm Booking

**Endpoint:** `PUT /api/bookings/{bookingId}/confirm`

Confirms a pending booking (if needed for multi-step booking process).

```bash
curl -X PUT http://localhost:8080/api/bookings/123/confirm \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🔐 ADMIN ENDPOINTS

### 6. Get All Bookings (Admin)

**Endpoint:** `GET /api/bookings/admin/all`

**Requires:** `ROLE_ADMIN`

```bash
curl -X GET http://localhost:8080/api/bookings/admin/all \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

### 7. Get Bookings by Slot (Admin)

**Endpoint:** `GET /api/bookings/admin/slot/{slotId}`

**Requires:** `ROLE_ADMIN`

```bash
curl -X GET http://localhost:8080/api/bookings/admin/slot/1 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

### 8. Get User Bookings by Email (Admin)

**Endpoint:** `GET /api/bookings/user/{userEmail}`

**Requires:** `ROLE_ADMIN`

```bash
curl -X GET http://localhost:8080/api/bookings/user/john@example.com \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

### 9. Get Booking by ID (Admin)

**Endpoint:** `GET /api/bookings/admin/{bookingId}`

**Requires:** `ROLE_ADMIN`

```bash
curl -X GET http://localhost:8080/api/bookings/admin/123 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

### 10. Cancel Booking (Admin)

**Endpoint:** `DELETE /api/bookings/admin/{bookingId}/cancel?userEmail={email}`

**Requires:** `ROLE_ADMIN`

```bash
curl -X DELETE "http://localhost:8080/api/bookings/admin/123/cancel?userEmail=john@example.com" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

---

## 📊 RESPONSE EXAMPLES

### Successful Booking Creation
```json
{
  "bookingId": 123,
  "slotId": 1,
  "movieTitle": "Avengers: Endgame",
  "theaterName": "PVR Cinemas",
  "screenType": "IMAX",
  "showDateTime": "2024-08-05T19:30:00",
  "seatNumbers": ["A1", "A2"],
  "userEmail": "john@example.com",
  "totalAmount": 300.0,
  "bookingTime": "2024-08-04T14:30:00",
  "status": "CONFIRMED",
  "paymentId": "MOCK_PAY_ABC12345",
  "paymentStatus": "COMPLETED"
}
```

### My Bookings Response
```json
[
  {
    "bookingId": 123,
    "slotId": 1,
    "movieTitle": "Avengers: Endgame",
    "theaterName": "PVR Cinemas",
    "screenType": "IMAX",
    "showDateTime": "2024-08-05T19:30:00",
    "seatNumbers": ["A1", "A2"],
    "userEmail": "john@example.com",
    "totalAmount": 300.0,
    "bookingTime": "2024-08-04T14:30:00",
    "status": "CONFIRMED",
    "paymentId": "MOCK_PAY_ABC12345",
    "paymentStatus": "COMPLETED"
  }
]
```

---

## 💳 MOCK PAYMENT SYSTEM

### Supported Payment Methods
- `MOCK_PAYMENT` (default)
- `CREDIT_CARD`
- `DEBIT_CARD`

### Payment Validation
The mock payment system validates:
- Card number format (13-19 digits)
- Card holder name (not empty)
- Expiry date format (MM/YY)
- CVV format (3-4 digits)

### Payment Success Rate
- 95% success rate for valid payment details
- Automatic failure for invalid details

### Payment Response Details
```json
{
  "paymentId": "MOCK_PAY_ABC12345",
  "transactionId": "TXN_XYZ789012345",
  "amount": 300.0,
  "paymentMethod": "MOCK_PAYMENT",
  "status": "SUCCESS",
  "message": "Payment processed successfully",
  "paymentDetails": {
    "gateway": "MockPayment Gateway",
    "processingTime": "2.3s",
    "maskedCardNumber": "**** **** **** 1111",
    "cardType": "Visa"
  }
}
```

---

## 🎯 KEY FEATURES

### Enhanced Authentication
- ✅ **JWT Token Extraction** - User details automatically extracted from token
- ✅ **Role-based Access** - Different endpoints for users and admins
- ✅ **Secure User Identification** - No need to pass userEmail in request

### Seat Number Selection
- ✅ **Human-readable Seat Numbers** - Use "A1", "B2" instead of seat IDs
- ✅ **Seat Validation** - Ensures seats exist and belong to correct slot
- ✅ **Availability Check** - Prevents double booking

### Mock Payment Integration
- ✅ **Realistic Payment Flow** - Complete payment processing simulation
- ✅ **Payment Validation** - Card details validation
- ✅ **Payment Response** - Detailed payment information
- ✅ **Payment Failure Handling** - Graceful failure responses

### Booking Management
- ✅ **Immediate Confirmation** - Bookings confirmed upon successful payment
- ✅ **Seat Allocation** - Automatic seat marking and slot updates
- ✅ **Cancellation Support** - Users can cancel their own bookings
- ✅ **Admin Controls** - Full admin access to all bookings

---

## 🚫 ERROR RESPONSES

### Authentication Errors
```json
"Invalid or missing authentication token"
```

### Payment Errors
```json
"Payment failed: Invalid payment details provided"
```

### Validation Errors
```json
"Seat A1 is already booked"
```

```json
"Total amount mismatch. Expected: 300.0, Provided: 250.0"
```

### Authorization Errors
```json
"You can only cancel your own bookings"
```

---

## 🎭 REAL-WORLD SCENARIOS

### Scenario 1: User Books Movie Tickets

```bash
# Step 1: User logs in and gets JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john@example.com",
    "password": "password123"
  }'

# Response includes JWT token
# {
#   "token": "eyJhbGciOiJIUzI1NiJ9...",
#   "tokenType": "Bearer",
#   "username": "John Doe",
#   "email": "john@example.com",
#   "role": "Role_User"
# }

# Step 2: User creates booking with extracted user details
curl -X POST http://localhost:8080/api/bookings \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
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

# Step 3: User checks their bookings
curl -X GET http://localhost:8080/api/bookings/my-bookings \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Scenario 2: User Cancels Booking

```bash
# Cancel own booking
curl -X DELETE http://localhost:8080/api/bookings/123/cancel \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Scenario 3: Admin Manages Bookings

```bash
# Admin views all bookings
curl -X GET http://localhost:8080/api/bookings/admin/all \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"

# Admin cancels user booking
curl -X DELETE "http://localhost:8080/api/bookings/admin/123/cancel?userEmail=john@example.com" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

---

## 📝 IMPORTANT NOTES

1. **JWT Authentication:** All endpoints require valid JWT token with appropriate role
2. **Seat Numbers:** Use theater-style seat numbers (A1, B2, etc.) not seat IDs
3. **Payment Processing:** Mock payment system with 95% success rate
4. **User Extraction:** User details automatically extracted from JWT token
5. **Immediate Confirmation:** Successful payments result in confirmed bookings
6. **Role-based Access:** Users can only access their own bookings, admins have full access
7. **Cancellation Policy:** Users can cancel their own bookings, admins can cancel any booking

---

## 🚀 GETTING STARTED

1. **Authenticate:** Login to get JWT token
2. **Select Movie:** Choose movie slot for booking
3. **Choose Seats:** Select available seats using seat numbers
4. **Make Payment:** Provide payment details for mock payment
5. **Confirm Booking:** Booking automatically confirmed on successful payment
6. **Manage Bookings:** View or cancel bookings as needed

The enhanced booking system provides a complete, authenticated, and payment-integrated movie booking experience with realistic mock payment processing.
