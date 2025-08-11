# Test Data for Movie Booking API

## Sample Valid Payment Details for Testing

### Test Card 1 (Visa)
```json
{
  "cardNumber": "4111111111111111",
  "cardHolderName": "John Doe",
  "expiryDate": "12/25",
  "cvv": "123"
}
```

### Test Card 2 (MasterCard)
```json
{
  "cardNumber": "5555555555554444",
  "cardHolderName": "Jane Smith",
  "expiryDate": "06/26",
  "cvv": "456"
}
```

### Test Card 3 (American Express)
```json
{
  "cardNumber": "378282246310005",
  "cardHolderName": "Bob Johnson",
  "expiryDate": "09/27",
  "cvv": "7890"
}
```

## Complete Booking Request Example

### Step 1: Select Seats
```
POST /api/bookings/select-seats
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "slotId": 1,
  "seatNumbers": ["A1", "A2"]
}
```

### Step 2: Complete Booking with Payment
```
POST /api/bookings
Authorization: Bearer <jwt-token>
Content-Type: application/json

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

## Payment Validation Rules

The mock payment service validates:
- Card number: 13-19 digits
- Card holder name: 2-50 characters, not empty
- Expiry date: MM/YY format
- CVV: 3-4 digits

## Two-Step Booking Process

1. **Seat Selection**: Use `/api/bookings/select-seats` to see available seats, pricing, and movie details
2. **Payment & Booking**: Use `/api/bookings` with valid payment details to complete the booking

This ensures users can see the total cost before entering payment information.
