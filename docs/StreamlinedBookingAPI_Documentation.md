# Streamlined Movie Booking API Documentation

## Overview
The booking system now provides a streamlined two-step process:
1. **Seat Selection**: Select seats and see pricing details
2. **Payment & Booking**: Complete booking with payment details only

## API Endpoints

### 1. Select Seats
**Endpoint**: `POST /api/bookings/select-seats`
**Purpose**: Select seats and retrieve booking details

#### Request
```json
{
  "slotId": 1,
  "seatNumbers": ["A1", "A2"]
}
```

#### Response
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

### 2. Complete Booking (Streamlined)
**Endpoint**: `POST /api/bookings/payment`
**Purpose**: Complete booking using cached seat selection data

#### Request
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

#### Response
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

### 3. Legacy Complete Booking (Manual Data Entry)
**Endpoint**: `POST /api/bookings`
**Purpose**: Create booking with all details manually provided

#### Request
```json
{
  "slotId": 1,
  "seatNumbers": ["A1", "A2"],
  "totalAmount": 300.0,
  "paymentMethod": "MOCK_PAYMENT",
  "cardNumber": "4111111111111111",
  "cardHolderName": "John Doe",
  "expiryDate": "12/25",
  "cvv": "123"
}
```

## Streamlined Booking Flow

### Step 1: Seat Selection
```bash
curl -X POST http://localhost:8080/api/bookings/select-seats \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "slotId": 1,
    "seatNumbers": ["A1", "A2"]
  }'
```

**Benefits**:
- Shows total cost before payment
- Validates seat availability
- Displays movie and cinema details
- Caches selection for 30 minutes

### Step 2: Payment & Booking
```bash
curl -X POST http://localhost:8080/api/bookings/payment \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "slotId": 1,
    "paymentMethod": "MOCK_PAYMENT",
    "cardNumber": "4111111111111111",
    "cardHolderName": "John Doe",
    "expiryDate": "12/25",
    "cvv": "123"
  }'
```

**Benefits**:
- Uses cached seat selection data automatically
- Only requires payment details
- Ensures data consistency
- Validates seat availability again before booking

## Key Features

### 1. Automatic Data Population
- Seat numbers, total amount, and movie details are automatically populated from the seat selection
- No need to manually enter booking details

### 2. Session Management
- Seat selections are cached for 30 minutes per user/slot
- Automatic cleanup of expired selections
- Prevents data inconsistency

### 3. Validation
- Validates seat availability at both steps
- Ensures payment details are correct
- Confirms slot and user validity

### 4. Error Handling
- Clear error messages for expired selections
- Proper validation of payment details
- Seat availability conflicts handled gracefully

## Test Cards for Mock Payment

### Valid Test Cards
```json
// Visa
{
  "cardNumber": "4111111111111111",
  "cardHolderName": "John Doe",
  "expiryDate": "12/25",
  "cvv": "123"
}

// MasterCard
{
  "cardNumber": "5555555555554444",
  "cardHolderName": "Jane Smith",
  "expiryDate": "06/26",
  "cvv": "456"
}

// Amex
{
  "cardNumber": "378282246310005",
  "cardHolderName": "Bob Johnson",
  "expiryDate": "09/27",
  "cvv": "7890"
}
```

## Error Scenarios

### 1. No Seat Selection Found
```json
{
  "error": "No seat selection found. Please select seats first using /select-seats endpoint."
}
```

### 2. Expired Seat Selection
```json
{
  "error": "No seat selection found. Please select seats first using /select-seats endpoint."
}
```

### 3. Seat No Longer Available
```json
{
  "error": "Seat A1 is no longer available for slot 1"
}
```

### 4. Invalid Payment Details
```json
{
  "error": "Payment failed: Invalid payment details provided"
}
```

## Advantages of New Flow

1. **User Experience**: Users see total cost before entering payment details
2. **Data Consistency**: Automatic population prevents manual entry errors
3. **Security**: Sensitive data handling is minimized
4. **Performance**: Cached selections reduce database queries
5. **Reliability**: Double validation ensures booking accuracy
