# BookingController - Updated API Usage Examples

## ✅ **UPDATED**: Using @RequestHeader instead of HttpServletRequest

All JWT token-based endpoints now use `@RequestHeader("Authorization")` for cleaner parameter handling.

### 🔐 Authentication Required

All endpoints require JWT authentication token in the Authorization header:

```
Authorization: Bearer <JWT_TOKEN>
```

## 📋 **UPDATED API ENDPOINTS**

### 1. Create Booking

**Endpoint:** `POST /api/bookings`

```bash
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
```

### 2. Get My Bookings

**Endpoint:** `GET /api/bookings/my-bookings`

```bash
curl -X GET http://localhost:8080/api/bookings/my-bookings \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### 3. Cancel My Booking

**Endpoint:** `DELETE /api/bookings/{bookingId}/cancel`

```bash
curl -X DELETE http://localhost:8080/api/bookings/123/cancel \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

## 🔧 **Code Changes Made**

### Controller Method Signatures Updated:

#### Before:
```java
@PostMapping
public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest request,
        HttpServletRequest httpRequest) {
    String userEmail = extractUserEmailFromToken(httpRequest);
    // ...
}
```

#### After:
```java
@PostMapping
public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest request,
        @RequestHeader("Authorization") String authHeader) {
    String userEmail = extractUserEmailFromToken(authHeader);
    // ...
}
```

### Helper Method Updated:

#### Before:
```java
private String extractUserEmailFromToken(HttpServletRequest request) {
    try {
        String jwt = jwtUtils.getJwtFromHeader(request);
        if (jwt != null && jwtUtils.validateToken(jwt)) {
            return jwtUtils.getNameFromJwt(jwt);
        }
    } catch (Exception e) {
        log.error("Error extracting user from token: ", e);
    }
    return null;
}
```

#### After:
```java
private String extractUserEmailFromToken(String authHeader) {
    try {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            if (jwtUtils.validateToken(jwt)) {
                return jwtUtils.getNameFromJwt(jwt);
            }
        }
    } catch (Exception e) {
        log.error("Error extracting user from token: ", e);
    }
    return null;
}
```

## 🎯 **Benefits of @RequestHeader Approach**

1. **Cleaner Code**: Direct parameter binding without manual extraction
2. **Better Type Safety**: Spring handles the header extraction
3. **Simplified Logic**: No need for HttpServletRequest utilities
4. **Explicit Declaration**: Clearly shows which header is required
5. **Less Boilerplate**: Reduced code for token extraction

## 🧪 **Testing the Updated API**

### Step 1: Get JWT Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "username": "John Doe",
  "email": "john@example.com",
  "role": "Role_User"
}
```

### Step 2: Use Token in Booking Request
```bash
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
```

**Expected Response:**
```json
{
  "bookingId": 1,
  "slotId": 1,
  "movieTitle": "Avengers: Endgame",
  "theaterName": "PVR Cinemas",
  "seatNumbers": ["A1", "A2"],
  "userEmail": "john@example.com",
  "totalAmount": 300.0,
  "status": "CONFIRMED",
  "paymentId": "MOCK_PAY_ABC12345",
  "paymentStatus": "COMPLETED"
}
```

## ✅ **All Issues Fixed**

1. ✅ Removed unnecessary `UserController` dependency
2. ✅ Fixed variable name mismatch (`userName` vs `userEmail`)
3. ✅ Updated to use `@RequestHeader("Authorization")` 
4. ✅ Simplified token extraction logic
5. ✅ Removed unused imports
6. ✅ Updated all affected endpoints consistently

The BookingController is now ready for testing with proper JWT authentication using @RequestHeader!
