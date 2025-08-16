# BookingController - Complete Documentation

## Table of Contents
1. [Overview](#overview)
2. [Authentication & Security](#authentication--security)
3. [API Endpoints](#api-endpoints)
   - [User Endpoints](#user-endpoints)
   - [Admin Endpoints](#admin-endpoints)
   - [Streamlined Booking Flow](#streamlined-booking-flow)
4. [Enhanced Features](#enhanced-features)
5. [Mock Payment System](#mock-payment-system)
6. [Testing Guide](#testing-guide)
7. [Error Handling](#error-handling)
8. [Race Condition Handling](#race-condition-handling)
9. [Session Management](#session-management)
10. [Implementation Details](#implementation-details)

---

## Overview

The BookingController provides a comprehensive movie ticket booking system with the following key features:

- **JWT Authentication**: Secure user authentication with role-based access control
- **Seat Number Selection**: Human-readable seat selection (A1, B2, etc.)
- **Mock Payment Integration**: Realistic payment processing with validation
- **Race Condition Handling**: Thread-safe booking operations
- **Session Management**: Temporary seat locking with automatic cleanup
- **Streamlined Flow**: Two-step booking process for better UX
- **Admin Controls**: Complete administrative booking management

### Base URL
```
http://localhost:8080/api/bookings
```

---

## Authentication & Security

### 🔐 AUTHENTICATION REQUIRED

All booking endpoints require JWT authentication with appropriate roles:

```bash
Authorization: Bearer <JWT_TOKEN>
```

### Role-Based Access Control
- **USER Role**: Can create, view, and cancel own bookings
- **ADMIN Role**: Full access to all bookings and management functions

### Token Extraction
User details are automatically extracted from JWT tokens using `@RequestHeader("Authorization")`:

```java
@PostMapping
public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest request,
        @RequestHeader("Authorization") String authHeader) {
    String userEmail = extractUserEmailFromToken(authHeader);
    // ...
}
```

---

## API Endpoints

### User Endpoints

#### 1. Create Booking (Legacy - Complete Flow)

**Endpoint:** `POST /api/bookings`

Creates a new booking with all details provided in a single request.

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

**Response:**
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

#### 2. Get My Bookings

**Endpoint:** `GET /api/bookings/my-bookings`

Retrieves all bookings for the authenticated user.

```bash
curl -X GET http://localhost:8080/api/bookings/my-bookings \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 3. Get Booking by ID

**Endpoint:** `GET /api/bookings/{bookingId}`

Retrieves details of a specific booking.

```bash
curl -X GET http://localhost:8080/api/bookings/123 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 4. Cancel My Booking

**Endpoint:** `DELETE /api/bookings/{bookingId}/cancel`

Cancels a booking owned by the authenticated user.

```bash
curl -X DELETE http://localhost:8080/api/bookings/123/cancel \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 5. Confirm Booking

**Endpoint:** `PUT /api/bookings/{bookingId}/confirm`

Confirms a pending booking (if needed for multi-step booking process).

```bash
curl -X PUT http://localhost:8080/api/bookings/123/confirm \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Admin Endpoints

#### 6. Get All Bookings (Admin)

**Endpoint:** `GET /api/bookings/admin/all`

**Requires:** `ROLE_ADMIN`

```bash
curl -X GET http://localhost:8080/api/bookings/admin/all \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

#### 7. Get Bookings by Slot (Admin)

**Endpoint:** `GET /api/bookings/admin/slot/{slotId}`

**Requires:** `ROLE_ADMIN`

```bash
curl -X GET http://localhost:8080/api/bookings/admin/slot/1 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

#### 8. Get User Bookings by Email (Admin)

**Endpoint:** `GET /api/bookings/user/{userEmail}`

**Requires:** `ROLE_ADMIN`

```bash
curl -X GET http://localhost:8080/api/bookings/user/john@example.com \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

#### 9. Get Booking by ID (Admin)

**Endpoint:** `GET /api/bookings/admin/{bookingId}`

**Requires:** `ROLE_ADMIN`

```bash
curl -X GET http://localhost:8080/api/bookings/admin/123 \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

#### 10. Cancel Booking (Admin)

**Endpoint:** `DELETE /api/bookings/admin/{bookingId}/cancel?userEmail={email}`

**Requires:** `ROLE_ADMIN`

```bash
curl -X DELETE "http://localhost:8080/api/bookings/admin/123/cancel?userEmail=john@example.com" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

### Streamlined Booking Flow

#### Step 1: Select Seats

**Endpoint:** `POST /api/bookings/select-seats`

Select seats and retrieve booking details with temporary locking.

**Request:**
```json
{
  "slotId": 1,
  "seatNumbers": ["A1", "A2"]
}
```

**curl Example:**
```bash
curl -X POST http://localhost:8080/api/bookings/select-seats \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -H "X-User-ID: user@example.com" \
  -d '{
    "slotId": 1,
    "seatNumbers": ["A1", "A2"]
  }' -v
```

**Response:**
```json
{
  "slotId": 1,
  "seatNumbers": ["A1", "A2"],
  "totalAmount": 300.0,
  "movieTitle": "The Amazing Movie",
  "movieDescription": "A great movie experience",
  "cinemaName": "Multiplex Cinema",
  "screenName": "Screen 1",
  "showTime": "18:30",
  "showDate": "2025-08-05",
  "seatDetails": [
    {
      "seatNumber": "A1",
      "seatType": "STANDARD",
      "price": 150.0
    },
    {
      "seatNumber": "A2",
      "seatType": "STANDARD",
      "price": 150.0
    }
  ]
}
```

**Features:**
- Shows total cost before payment
- Validates seat availability
- Displays movie and cinema details
- Temporarily locks seats for 15 minutes
- Returns session ID in response headers

#### Step 2: Complete Booking with Payment

**Endpoint:** `POST /api/bookings/payment`

Complete booking using cached seat selection data.

**Request:**
```json
{
  "slotId": 1,
  "paymentMethod": "MOCK_PAYMENT",
  "cardNumber": "4111111111111111",
  "cardHolderName": "John Doe",
  "expiryDate": "12/25",
  "cvv": "123"
}
```

**curl Example:**
```bash
curl -X POST http://localhost:8080/api/bookings/payment \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -H "X-Session-ID: user_123456789" \
  -d '{
    "slotId": 1,
    "paymentMethod": "MOCK_PAYMENT",
    "cardNumber": "4111111111111111",
    "cardHolderName": "John Doe",
    "expiryDate": "12/25",
    "cvv": "123"
  }'
```

**Response:**
```json
{
  "bookingId": 123,
  "userEmail": "user@example.com",
  "movieTitle": "The Amazing Movie",
  "cinemaName": "Multiplex Cinema",
  "screenName": "Screen 1",
  "showDate": "2025-08-05",
  "showTime": "18:30",
  "seatNumbers": ["A1", "A2"],
  "totalAmount": 300.0,
  "bookingTime": "2025-08-04T12:15:30.123",
  "status": "CONFIRMED",
  "paymentStatus": "COMPLETED",
  "paymentId": "MOCK_PAY_ABC123",
  "paymentMethod": "MOCK_PAYMENT"
}
```

#### Additional Session Management Endpoints

**Release Seat Locks:**
```bash
curl -X DELETE http://localhost:8080/api/bookings/release-seats \
  -H "X-Session-ID: user_123456789"
```

**Check Seat Availability:**
```bash
curl -X POST http://localhost:8080/api/bookings/seats/check-availability \
  -H "Content-Type: application/json" \
  -d '{
    "slotId": 1,
    "seatNumbers": ["A1", "A2", "A3", "B1", "B2"]
  }'
```

---

## Enhanced Features

### 1. ✅ JWT Authentication Integration
- **User Email Extraction**: User details automatically extracted from JWT token
- **No Manual Email Input**: Removed requirement to pass `userEmail` in request body
- **Secure Authentication**: All booking endpoints require valid JWT token with appropriate role
- **Helper Method**: Added `extractUserEmailFromToken()` method in controller

### 2. ✅ Seat Number Selection (Human-Readable)
- **Changed from Seat IDs to Seat Numbers**: Now uses "A1", "B2", etc. instead of numeric IDs
- **Updated BookingRequest**: Changed `List<Long> seatIds` to `List<String> seatNumbers`
- **Seat Validation**: Added `validateAndGetSeatsBySeatNumbers()` method
- **Better User Experience**: Users select seats using theater-style numbering

### 3. ✅ Mock Payment Integration
- **MockPaymentService**: Complete mock payment processing service
- **Payment Validation**: Validates card number, holder name, expiry date, and CVV
- **95% Success Rate**: Realistic payment success simulation
- **Payment Details Storage**: Stores payment information as JSON in database
- **Card Type Detection**: Automatically detects Visa, Mastercard, AmEx, etc.
- **Refund Processing**: Automatic refund processing on booking cancellation

### 4. ✅ Enhanced Booking Flow
- **Immediate Confirmation**: Successful payments result in CONFIRMED status
- **Automatic Seat Allocation**: Seats marked as booked upon successful payment
- **Payment Status Tracking**: Tracks payment status throughout booking lifecycle
- **Error Handling**: Comprehensive error handling for payment failures

### 5. ✅ Session-Based Seat Management
- **Temporary Seat Locking**: Seats locked for 15 minutes during selection
- **Session ID Management**: Unique session identifiers for each user session
- **Automatic Cleanup**: Expired locks cleaned up every minute
- **Conflict Detection**: Prevents race conditions during seat selection

---

## Mock Payment System

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

### Valid Test Cards

```json
// Visa
{
  "cardNumber": "4111111111111111",
  "cardHolderName": "John Doe",
  "expiryDate": "12/26",
  "cvv": "123"
}

// MasterCard
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

### Payment Success Rate
- 95% success rate for valid payment details
- Automatic failure for invalid details

### Refund Processing
- Automatic refunds on booking cancellation
- Updates payment status to "REFUNDED"
- Stores refund details in booking

---

## Testing Guide

### Prerequisites
1. **User Registration and Login**
2. **Valid JWT Token**
3. **Available Movie Slots with Seats**

### Step 1: Get JWT Token

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

### Step 2: Find Available Slots and Seats

```bash
# Get available movie slots
curl -X GET http://localhost:8080/api/slots/available \
  -H "Accept: application/json"

# Get available seats for a slot
curl -X GET http://localhost:8080/api/seats/slot/1/available \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Accept: application/json"
```

### Step 3: Test Booking Creation

#### Test Case 1: Successful Booking (Legacy Flow)
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

#### Test Case 2: Streamlined Booking Flow
```bash
# Step 1: Select seats
curl -X POST http://localhost:8080/api/bookings/select-seats \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -H "X-User-ID: john@example.com" \
  -d '{
    "slotId": 1,
    "seatNumbers": ["A1", "A2"]
  }' -v

# Note the X-Session-ID from response headers

# Step 2: Complete payment
curl -X POST http://localhost:8080/api/bookings/payment \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -H "X-Session-ID: user_123456789" \
  -d '{
    "slotId": 1,
    "paymentMethod": "MOCK_PAYMENT",
    "cardNumber": "4111111111111111",
    "cardHolderName": "John Doe",
    "expiryDate": "12/25",
    "cvv": "123"
  }'
```

#### Test Case 3: Payment Failure
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

### Step 4: Test Booking Management

```bash
# Get my bookings
curl -X GET http://localhost:8080/api/bookings/my-bookings \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get specific booking
curl -X GET http://localhost:8080/api/bookings/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Cancel my booking
curl -X DELETE http://localhost:8080/api/bookings/1/cancel \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Step 5: Admin Testing

```bash
# Get all bookings (Admin)
curl -X GET http://localhost:8080/api/bookings/admin/all \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"

# Cancel user booking (Admin)
curl -X DELETE "http://localhost:8080/api/bookings/admin/1/cancel?userEmail=john@example.com" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

---

## Error Handling

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

### Session Management Errors
```json
"No seat selection found. Please select seats first using /select-seats endpoint."
```

```json
"Some seats are already selected by other users: [A1, A2, A3]"
```

---

## Race Condition Handling

### Features
- **Temporary Seat Locking**: Seats locked during selection process (15 minutes)
- **Session Management**: Unique session IDs prevent conflicts
- **Database Locking**: Pessimistic locking during payment processing
- **Automatic Cleanup**: Expired locks cleaned up every minute
- **Conflict Detection**: Real-time conflict detection and resolution

### Testing Race Conditions

#### Concurrent Seat Selection
```bash
# Terminal 1 - First user selects seats
curl -X POST http://localhost:8080/api/bookings/select-seats \
  -H "Content-Type: application/json" \
  -H "X-User-ID: user1@example.com" \
  -d '{
    "slotId": 1,
    "seatNumbers": ["A1", "A2", "A3"]
  }' &

# Terminal 2 - Second user tries same seats (should fail)
curl -X POST http://localhost:8080/api/bookings/select-seats \
  -H "Content-Type: application/json" \
  -H "X-User-ID: user2@example.com" \
  -d '{
    "slotId": 1,
    "seatNumbers": ["A1", "A2", "A3"]
  }'
```

#### Concurrent Payment Processing
```bash
# Terminal 1 - First user attempts payment
curl -X POST http://localhost:8080/api/bookings/payment \
  -H "Authorization: Bearer FIRST_USER_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "slotId": 1,
    "seatNumbers": ["C1", "C2"],
    "totalAmount": 300.00,
    "paymentMethod": "CREDIT_CARD",
    "paymentDetails": {...}
  }' &

# Terminal 2 - Second user attempts payment for same seats
curl -X POST http://localhost:8080/api/bookings/payment \
  -H "Authorization: Bearer SECOND_USER_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "slotId": 1,
    "seatNumbers": ["C1", "C2"],
    "totalAmount": 300.00,
    "paymentMethod": "DEBIT_CARD",
    "paymentDetails": {...}
  }'
```

---

## Session Management

### Session Features
- **Unique Session IDs**: Generated for each user/selection session
- **Temporary Locks**: 15-minute duration for seat locks
- **Automatic Cleanup**: Background cleanup every 60 seconds
- **Session Reuse**: Same session ID can modify selections
- **Manual Release**: Users can manually release locks

### Session Headers
```
X-User-ID: user@example.com      # For new sessions
X-Session-ID: user_123456789     # For existing sessions
```

### Lock Duration
- **Selection Lock**: 15 minutes
- **Cleanup Frequency**: Every 60 seconds
- **Configurable**: Can be adjusted based on requirements

---

## Implementation Details

### Files Modified/Created

#### Core Files
1. **BookingRequest.java** - Updated to use seat numbers and payment details
2. **BookingController.java** - Added JWT authentication and enhanced endpoints
3. **BookingService.java** - Integrated payment processing and seat number validation
4. **WebSecurityConfig.java** - Updated security configuration for booking endpoints

#### New Services
1. **MockPaymentService.java** - Complete mock payment processing service
2. **SeatLockingService.java** - Session-based temporary seat locking
3. **SessionManagementService.java** - User session management

### Security Configuration Updates
- **Updated WebSecurityConfig**: Added proper security for all booking endpoints
- **Role-based Access**: Admin endpoints require ADMIN role, user endpoints require USER role
- **JWT Filter Integration**: Proper JWT authentication for all protected endpoints

### Database Changes
- **Payment Information**: JSON storage for payment details
- **Session Tracking**: Temporary seat lock tables
- **Booking Status**: Enhanced status tracking
- **Audit Trail**: Complete booking lifecycle tracking

---

## Important Notes

1. **JWT Authentication:** All endpoints require valid JWT token with appropriate role
2. **Seat Numbers:** Use theater-style seat numbers (A1, B2, etc.) not seat IDs
3. **Payment Processing:** Mock payment system with 95% success rate
4. **User Extraction:** User details automatically extracted from JWT token
5. **Immediate Confirmation:** Successful payments result in confirmed bookings
6. **Role-based Access:** Users can only access their own bookings, admins have full access
7. **Cancellation Policy:** Users can cancel their own bookings, admins can cancel any booking
8. **Session Management:** Temporary seat locking prevents race conditions
9. **Automatic Cleanup:** System automatically manages expired locks
10. **Streamlined Flow:** Two-step process improves user experience and data consistency

---

## Getting Started

1. **Authenticate:** Login to get JWT token
2. **Select Movie:** Choose movie slot for booking
3. **Choose Seats:** Use streamlined seat selection or legacy flow
4. **Make Payment:** Provide payment details for mock payment
5. **Confirm Booking:** Booking automatically confirmed on successful payment
6. **Manage Bookings:** View or cancel bookings as needed

The enhanced booking system provides a complete, authenticated, and payment-integrated movie booking experience with realistic mock payment processing, race condition handling, and session management for optimal user experience and data integrity.
