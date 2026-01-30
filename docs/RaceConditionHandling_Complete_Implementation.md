# Race Condition Handling and Frontend-Based Seat Selection

## Overview

This implementation addresses the race condition issue in seat booking and optimizes the system by moving seat selection state management to the frontend while ensuring atomic booking operations with race condition protection.

## Problem Solved

### Before (Issues):
- **Backend Load**: Storing seat selections in backend cache with user authentication
- **Race Conditions**: Multiple users could book the same seats simultaneously
- **Concurrency Issues**: No atomic locking mechanism for seat reservations
- **Poor UX**: Required authentication for seat selection exploration

### After (Solutions):
- **Frontend State Management**: Seat selections stored in localStorage/memory
- **Pessimistic Locking**: Database-level seat locking during payment
- **Atomic Operations**: SERIALIZABLE transactions prevent race conditions
- **Public Seat Selection**: No authentication needed for browsing seats
- **Race Condition Detection**: Clear error handling for booking conflicts

## Architecture Changes

### 1. Frontend Optimizations
```javascript
// Seat selection (NO AUTH REQUIRED)
POST /api/bookings/select-seats
// Returns seat information for frontend storage

// Optional real-time availability check
POST /api/bookings/seats/check-availability  
// Checks current seat availability

// Payment processing (AUTH REQUIRED)
POST /api/bookings/payment
// Includes selected seats data + race condition handling
```

### 2. Backend Race Condition Protection

#### Pessimistic Locking
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT s FROM Seat s WHERE s.slot = :slot AND s.seatNumber IN :seatNumbers")
List<Seat> findBySlotAndSeatNumbersWithLock(@Param("slot") MovieSlot slot, @Param("seatNumbers") List<String> seatNumbers);
```

#### Atomic Transactions
```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public BookingResponse createBookingWithRaceConditionHandling(PaymentBookingRequest paymentRequest, String userEmail) {
    // 1. Lock seats with pessimistic locking
    // 2. Check availability within transaction
    // 3. Process payment
    // 4. Atomically mark seats as booked
    // 5. Create booking record
}
```

## API Changes

### 1. New Public Endpoints (No Auth Required)

#### Check Seat Availability
```http
POST /api/bookings/seats/check-availability
Content-Type: application/json

{
  "slotId": 1,
  "seatNumbers": ["A1", "A2", "A3"]
}
```

**Response:**
```json
{
  "slotId": 1,
  "seatAvailability": {
    "A1": true,
    "A2": true,
    "A3": false
  },
  "message": "Seat availability checked successfully"
}
```

#### Get Seat Information
```http
POST /api/bookings/select-seats
Content-Type: application/json

{
  "slotId": 1,
  "seatNumbers": ["A1", "A2", "A3"]
}
```

**Response:**
```json
{
  "slotId": 1,
  "seatNumbers": ["A1", "A2", "A3"],
  "totalAmount": 450.00,
  "movieTitle": "Avengers: Endgame",
  "cinemaName": "PVR Cinemas",
  "showTime": "14:00:00",
  "showDate": "2025-08-05",
  "seatDetails": [
    {"seatNumber": "A1", "price": 150.00},
    {"seatNumber": "A2", "price": 150.00},
    {"seatNumber": "A3", "price": 150.00}
  ]
}
```

### 2. Enhanced Payment Endpoint (Auth Required)

#### Process Payment with Race Condition Handling
```http
POST /api/bookings/payment
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "slotId": 1,
  "seatNumbers": ["A1", "A2", "A3"],
  "totalAmount": 450.00,
  "paymentMethod": "CARD",
  "cardNumber": "1234567890123456",
  "cardHolderName": "John Doe",
  "expiryDate": "12/25",
  "cvv": "123"
}
```

**Success Response (201):**
```json
{
  "bookingId": 123,
  "slotId": 1,
  "movieTitle": "Avengers: Endgame",
  "seatNumbers": ["A1", "A2", "A3"],
  "totalAmount": 450.00,
  "status": "CONFIRMED",
  "paymentId": "PAY_123456"
}
```

**Race Condition Error (409):**
```json
"Seats no longer available: [A1, A2]"
```

## Race Condition Scenarios

### Scenario 1: Simultaneous Seat Selection
```
Timeline:
10:00:00 - User A selects seats A1, A2 → Stored in localStorage
10:00:05 - User B selects seats A1, A2 → Stored in localStorage
10:00:10 - Both users see selected seats and proceed to payment

Result: No conflict at selection level - handled at payment time
```

### Scenario 2: Concurrent Payment Processing
```
Timeline:
10:00:15 - User A hits payment API → Seats locked with PESSIMISTIC_WRITE
10:00:16 - User B hits payment API → Waits for User A's transaction
10:00:17 - User A: Payment succeeds → Seats marked as booked → Transaction committed
10:00:18 - User B: Availability check fails → Returns 409 Conflict

Result: User A gets booking, User B gets clear error message
```

### Scenario 3: Database-Level Protection
```sql
-- Seat table with booking status
UPDATE seats SET is_booked = true WHERE seat_id IN (1,2,3) AND is_booked = false;

-- Booking table with unique constraint
ALTER TABLE bookings ADD CONSTRAINT unique_seat_booking 
UNIQUE (slot_id, seat_number);
```

## Frontend Implementation Guide

### 1. Seat Selection Management
```javascript
class SeatSelectionManager {
  async selectSeats(slotId, seatNumbers) {
    // Call public API
    const response = await fetch('/api/bookings/select-seats', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ slotId, seatNumbers })
    });
    
    const seatData = await response.json();
    
    // Store in localStorage with expiry
    const selectionData = {
      ...seatData,
      timestamp: Date.now(),
      expiresAt: Date.now() + (15 * 60 * 1000) // 15 minutes
    };
    
    localStorage.setItem('selectedSeats', JSON.stringify(selectionData));
    return seatData;
  }
}
```

### 2. Payment Processing with Error Handling
```javascript
async processPayment(paymentDetails, authToken) {
  try {
    const storedSelection = JSON.parse(localStorage.getItem('selectedSeats'));
    
    const response = await fetch('/api/bookings/payment', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`
      },
      body: JSON.stringify({
        ...storedSelection,
        ...paymentDetails
      })
    });
    
    if (response.status === 409) {
      // Handle race condition
      localStorage.removeItem('selectedSeats');
      throw new Error('Seats no longer available');
    }
    
    const booking = await response.json();
    localStorage.removeItem('selectedSeats');
    return booking;
    
  } catch (error) {
    // Handle different error types
    handleBookingError(error);
  }
}
```

## Benefits

### Performance Improvements
- ✅ **Reduced Backend Load**: No caching required for seat selections
- ✅ **Faster Seat Selection**: Immediate frontend response
- ✅ **Stateless Backend**: Better scalability
- ✅ **Fewer API Calls**: Selection doesn't require backend storage

### Race Condition Protection
- ✅ **Pessimistic Locking**: Prevents concurrent seat booking
- ✅ **Atomic Operations**: SERIALIZABLE transactions ensure consistency
- ✅ **Clear Error Handling**: Users get meaningful feedback
- ✅ **Database Constraints**: Additional protection at DB level

### User Experience
- ✅ **No Auth for Browsing**: Users can explore seats freely
- ✅ **Persistent Selection**: localStorage preserves choices across page reloads
- ✅ **Real-time Feedback**: Optional availability checking
- ✅ **Clear Error Messages**: Users understand what went wrong

## Database Schema Updates

### 1. Seat Table (No Changes Required)
```sql
CREATE TABLE seats (
    seat_id BIGINT PRIMARY KEY,
    slot_id BIGINT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    is_booked BOOLEAN DEFAULT FALSE,
    price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (slot_id) REFERENCES movie_slots(slot_id)
);
```

### 2. Enhanced Booking Table
```sql
-- Add constraint to prevent double bookings
ALTER TABLE bookings 
ADD CONSTRAINT unique_seat_booking 
UNIQUE (slot_id, seat_number);

-- Optional: Add version for optimistic locking
ALTER TABLE seats ADD COLUMN version INT DEFAULT 0;
```

## Testing Race Conditions

### 1. Unit Tests
```java
@Test
public void testConcurrentSeatBooking() {
    // Simulate multiple threads trying to book same seats
    CompletableFuture<BookingResponse> booking1 = CompletableFuture.supplyAsync(() -> 
        bookingService.createBookingWithRaceConditionHandling(request, "user1@test.com"));
    
    CompletableFuture<BookingResponse> booking2 = CompletableFuture.supplyAsync(() -> 
        bookingService.createBookingWithRaceConditionHandling(request, "user2@test.com"));
    
    // Only one should succeed
    assertThat(oneSucceedsOneFailsWithConflict(booking1, booking2)).isTrue();
}
```

### 2. Integration Tests
```bash
# Simulate concurrent requests
curl -X POST localhost:8080/api/bookings/payment \
  -H "Authorization: Bearer token1" \
  -d '{"slotId":1,"seatNumbers":["A1"],"totalAmount":150}' &

curl -X POST localhost:8080/api/bookings/payment \
  -H "Authorization: Bearer token2" \
  -d '{"slotId":1,"seatNumbers":["A1"],"totalAmount":150}' &
```

## Monitoring and Logging

### 1. Race Condition Detection
```java
log.info("Booking created successfully with race condition protection: {} - Seats: {}", 
         booking.getBookingId(), paymentRequest.getSeatNumbers());

log.warn("Race condition detected - Seats no longer available: {}", unavailableSeats);
```

### 2. Performance Metrics
- Monitor seat selection response times
- Track race condition occurrence rates
- Measure payment success/failure ratios
- Log frontend selection patterns

## Migration Guide

### Phase 1: Deploy Backend Changes
1. Deploy new controller endpoints
2. Update PaymentBookingRequest DTO
3. Add pessimistic locking repository methods
4. Deploy race condition handling service

### Phase 2: Update Frontend
1. Implement localStorage seat management
2. Update seat selection UI to use new endpoints
3. Enhance payment flow with error handling
4. Test race condition scenarios

### Phase 3: Monitor and Optimize
1. Monitor race condition rates
2. Adjust timeouts and expiry settings
3. Optimize database locking strategies
4. Fine-tune error messages

## Conclusion

This implementation successfully addresses the race condition issue while optimizing the system architecture. The combination of frontend state management and backend atomic operations provides a robust, scalable, and user-friendly seat booking system.

Key achievements:
- **Eliminated race conditions** through pessimistic locking
- **Reduced backend load** by moving selection state to frontend  
- **Improved user experience** with public seat selection
- **Maintained data consistency** with atomic transactions
- **Provided clear error handling** for booking conflicts
