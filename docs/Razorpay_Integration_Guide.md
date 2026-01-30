# Razorpay Payment Integration Guide

## Overview
This guide explains how the Razorpay payment gateway has been integrated into MovieDekho application, replacing the mock payment system with a real payment gateway.

## What Was Changed

### 1. **Added Razorpay Dependency**
   - Added `razorpay-java` SDK to `pom.xml`
   - Version: 1.4.5

### 2. **Created RazorpayPaymentService**
   - Location: `src/main/java/com/movieDekho/MovieDekho/service/paymentService/RazorpayPaymentService.java`
   - Handles all Razorpay API interactions
   - Methods:
     - `createOrder()` - Creates a Razorpay order
     - `verifyPaymentSignature()` - Verifies payment authenticity
     - `getPaymentDetails()` - Fetches payment information
     - `refundPayment()` - Processes refunds

### 3. **Created PaymentController**
   - Location: `src/main/java/com/movieDekho/MovieDekho/controller/PaymentController.java`
   - New endpoints:
     - `POST /api/payments/initiate` - Start payment process
     - `POST /api/payments/verify` - Verify payment after completion
     - `GET /api/payments/{paymentId}/details` - Get payment details
     - `POST /api/payments/{paymentId}/refund` - Process refund (Admin only)
     - `POST /api/payments/webhook/razorpay` - Webhook for payment updates

### 4. **Updated BookingController**
   - Added new endpoint: `POST /api/bookings/razorpay/create-booking`
   - This creates a booking after Razorpay payment is verified

### 5. **Updated BookingService**
   - Added `createBookingAfterRazorpayPayment()` method
   - Handles booking creation with Razorpay payment details

### 6. **Configuration**
   - Added Razorpay configuration to `application.properties`:
     ```properties
     razorpay.key.id=${RAZORPAY_KEY_ID:your_key_id_here}
     razorpay.key.secret=${RAZORPAY_KEY_SECRET:your_key_secret_here}
     ```

## Configuration Setup

### Step 1: Get Razorpay API Keys
1. Go to [Razorpay Dashboard](https://dashboard.razorpay.com/)
2. Sign up or login
3. Go to **Settings → API Keys**
4. Generate keys for Test Mode
5. Copy **Key ID** and **Key Secret**

### Step 2: Configure Application

#### For Local Development
Add to `application.properties`:
```properties
razorpay.key.id=rzp_test_YOUR_KEY_ID
razorpay.key.secret=YOUR_KEY_SECRET
```

#### For Production (Recommended - Environment Variables)
Set environment variables:
```bash
# Windows PowerShell
$env:RAZORPAY_KEY_ID="rzp_live_YOUR_KEY_ID"
$env:RAZORPAY_KEY_SECRET="YOUR_KEY_SECRET"

# Linux/Mac
export RAZORPAY_KEY_ID="rzp_live_YOUR_KEY_ID"
export RAZORPAY_KEY_SECRET="YOUR_KEY_SECRET"
```

**IMPORTANT:** Never commit your actual API keys to version control!

## Payment Flow

### Frontend Integration Flow

```
┌─────────────────┐
│  1. User Selects│
│     Seats       │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  2. POST /api/payments/initiate     │
│     Request:                         │
│     {                                │
│       "slotId": 1,                   │
│       "seatNumbers": ["A1", "A2"],   │
│       "totalAmount": 450.00,         │
│       "phoneNumber": "9876543210"    │
│     }                                │
│                                      │
│     Response:                        │
│     {                                │
│       "orderId": "order_xxx",        │
│       "amount": 450.00,              │
│       "keyId": "rzp_test_xxx",       │
│       "bookingReference": "BOOK_xxx" │
│     }                                │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  3. Frontend Opens Razorpay Widget  │
│     Using Razorpay Checkout JS      │
│     https://razorpay.com/docs/      │
│                                      │
│     Razorpay.open({                 │
│       key: response.keyId,           │
│       amount: response.amount * 100, │
│       order_id: response.orderId,    │
│       handler: onSuccess             │
│     })                               │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  4. User Completes Payment          │
│     (Razorpay handles this)         │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  5. POST /api/payments/verify       │
│     Request:                         │
│     {                                │
│       "orderId": "order_xxx",        │
│       "paymentId": "pay_xxx",        │
│       "signature": "signature_hash"  │
│     }                                │
│                                      │
│     Response:                        │
│     {                                │
│       "status": "SUCCESS",           │
│       "paymentId": "pay_xxx",        │
│       "amount": 450.00               │
│     }                                │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  6. POST /api/bookings/razorpay/    │
│     create-booking                   │
│     Request:                         │
│     {                                │
│       "slotId": 1,                   │
│       "seatNumbers": ["A1", "A2"],   │
│       "totalAmount": 450.00,         │
│       "paymentId": "pay_xxx",        │
│       "orderId": "order_xxx"         │
│     }                                │
│                                      │
│     Response:                        │
│     {                                │
│       "bookingId": 123,              │
│       "status": "CONFIRMED",         │
│       "paymentId": "pay_xxx",        │
│       ...                            │
│     }                                │
└────────┬────────────────────────────┘
         │
         ▼
┌─────────────────┐
│  7. Show Ticket │
│     & Download  │
└─────────────────┘
```

## Frontend Code Example

### 1. Include Razorpay Script
```html
<script src="https://checkout.razorpay.com/v1/checkout.js"></script>
```

### 2. Initiate Payment
```javascript
async function initiatePayment() {
  try {
    const response = await fetch('/api/payments/initiate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userToken}`
      },
      body: JSON.stringify({
        slotId: 1,
        seatNumbers: ['A1', 'A2', 'A3'],
        totalAmount: 450.00,
        phoneNumber: '9876543210'
      })
    });

    const data = await response.json();
    
    if (response.ok) {
      openRazorpayCheckout(data);
    } else {
      console.error('Payment initiation failed:', data);
    }
  } catch (error) {
    console.error('Error:', error);
  }
}
```

### 3. Open Razorpay Checkout
```javascript
function openRazorpayCheckout(paymentData) {
  const options = {
    key: paymentData.keyId,
    amount: paymentData.amount * 100, // Amount in paise
    currency: 'INR',
    name: 'MovieDekho',
    description: 'Movie Ticket Booking',
    order_id: paymentData.orderId,
    handler: function (response) {
      verifyPayment(response, paymentData);
    },
    prefill: {
      email: paymentData.userEmail,
      contact: phoneNumber
    },
    theme: {
      color: '#F37254'
    },
    modal: {
      ondismiss: function() {
        alert('Payment cancelled');
      }
    }
  };

  const razorpay = new Razorpay(options);
  razorpay.open();
}
```

### 4. Verify Payment
```javascript
async function verifyPayment(razorpayResponse, paymentData) {
  try {
    const response = await fetch('/api/payments/verify', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userToken}`
      },
      body: JSON.stringify({
        orderId: razorpayResponse.razorpay_order_id,
        paymentId: razorpayResponse.razorpay_payment_id,
        signature: razorpayResponse.razorpay_signature
      })
    });

    const data = await response.json();
    
    if (response.ok && data.status === 'SUCCESS') {
      createBooking(razorpayResponse, paymentData);
    } else {
      alert('Payment verification failed!');
    }
  } catch (error) {
    console.error('Error verifying payment:', error);
  }
}
```

### 5. Create Booking
```javascript
async function createBooking(razorpayResponse, paymentData) {
  try {
    const response = await fetch('/api/bookings/razorpay/create-booking', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${userToken}`
      },
      body: JSON.stringify({
        slotId: selectedSlotId,
        seatNumbers: selectedSeats,
        totalAmount: totalAmount,
        paymentId: razorpayResponse.razorpay_payment_id,
        orderId: razorpayResponse.razorpay_order_id
      })
    });

    const booking = await response.json();
    
    if (response.ok) {
      alert('Booking confirmed! Booking ID: ' + booking.bookingId);
      window.location.href = `/tickets/${booking.bookingId}`;
    } else {
      alert('Booking failed: ' + JSON.stringify(booking));
    }
  } catch (error) {
    console.error('Error creating booking:', error);
  }
}
```

## API Endpoints

### 1. Initiate Payment
**Endpoint:** `POST /api/payments/initiate`

**Headers:**
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**Request:**
```json
{
  "slotId": 1,
  "seatNumbers": ["A1", "A2", "A3"],
  "totalAmount": 450.00,
  "phoneNumber": "9876543210"
}
```

**Response:**
```json
{
  "orderId": "order_1234567890",
  "amount": 450.00,
  "keyId": "rzp_test_xxxxx",
  "bookingReference": "BOOKING_1234567890",
  "userEmail": "user@example.com",
  "message": "Order created successfully. Proceed to payment on frontend."
}
```

---

### 2. Verify Payment
**Endpoint:** `POST /api/payments/verify`

**Request:**
```json
{
  "orderId": "order_1234567890",
  "paymentId": "pay_1234567890",
  "signature": "signature_hash_from_razorpay"
}
```

**Response:**
```json
{
  "status": "SUCCESS",
  "paymentId": "pay_1234567890",
  "orderId": "order_1234567890",
  "amount": 450.00,
  "method": "card",
  "message": "Payment verified successfully. You can now create booking."
}
```

---

### 3. Create Booking After Payment
**Endpoint:** `POST /api/bookings/razorpay/create-booking`

**Request:**
```json
{
  "slotId": 1,
  "seatNumbers": ["A1", "A2", "A3"],
  "totalAmount": 450.00,
  "paymentId": "pay_1234567890",
  "orderId": "order_1234567890"
}
```

**Response:**
```json
{
  "bookingId": 123,
  "userEmail": "user@example.com",
  "slotId": 1,
  "movieTitle": "Avengers: Endgame",
  "cinemaName": "PVR Cinemas",
  "screenName": "Screen 1",
  "showTime": "14:00:00",
  "showDate": "2025-08-05",
  "seatNumbers": ["A1", "A2", "A3"],
  "ticketFee": 450.00,
  "convenienceFee": 57.33,
  "totalAmount": 507.33,
  "status": "CONFIRMED",
  "paymentMethod": "RAZORPAY",
  "paymentStatus": "COMPLETED",
  "paymentId": "pay_1234567890",
  "bookingTime": "2025-01-26T10:30:00"
}
```

---

### 4. Get Payment Details
**Endpoint:** `GET /api/payments/{paymentId}/details`

**Response:**
```json
{
  "paymentId": "pay_1234567890",
  "status": "captured",
  "amount": 450.00,
  "method": "card",
  "orderId": "order_1234567890",
  "details": {
    "currency": "INR",
    "fee": 10.00,
    "tax": 1.80
  }
}
```

---

### 5. Process Refund (Admin Only)
**Endpoint:** `POST /api/payments/{paymentId}/refund?amount=450.00`

**Response:**
```json
{
  "refundId": "rfnd_1234567890",
  "paymentId": "pay_1234567890",
  "amount": 450.00,
  "status": "processed",
  "message": "Refund processed successfully"
}
```

## Testing

### Test Mode
1. Use test API keys (starting with `rzp_test_`)
2. Use test card numbers from Razorpay docs:
   - Card Number: `4111 1111 1111 1111`
   - CVV: Any 3 digits
   - Expiry: Any future date

### Test Payment Success
```bash
curl -X POST http://localhost:8080/api/payments/initiate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "slotId": 1,
    "seatNumbers": ["A1", "A2"],
    "totalAmount": 300.00,
    "phoneNumber": "9876543210"
  }'
```

## Migration from Mock Payment

### Old Flow (Mock Payment)
```
POST /api/bookings/payment → MockPaymentService → Create Booking
```

### New Flow (Razorpay)
```
POST /api/payments/initiate → Razorpay Order Created
↓
Frontend Razorpay Checkout → User Pays
↓
POST /api/payments/verify → Verify Signature
↓
POST /api/bookings/razorpay/create-booking → Create Booking
```

### What to Update in Frontend
1. **Remove** old payment form (card number, CVV, etc.)
2. **Add** Razorpay Checkout script
3. **Update** payment flow to use 3-step process:
   - Initiate → Pay → Create Booking
4. **Handle** Razorpay responses and callbacks

## Security Best Practices

1. **Never expose API keys in frontend code**
   - Key ID can be public
   - Key Secret must remain on backend

2. **Always verify payment signature**
   - Never trust payment status without verification
   - Use the `/verify` endpoint

3. **Use HTTPS in production**
   - Razorpay requires HTTPS for live mode

4. **Implement webhook for reliability**
   - Handle payment updates asynchronously
   - Update booking status based on webhooks

5. **Store payment details securely**
   - Payment details are encrypted in database
   - Use environment variables for keys

## Troubleshooting

### Issue: "Invalid API Key"
- Check if `RAZORPAY_KEY_ID` and `RAZORPAY_KEY_SECRET` are set correctly
- Verify you're using test keys in test mode

### Issue: "Payment Verification Failed"
- Ensure signature verification is working
- Check if payment was actually completed on Razorpay

### Issue: "Seats Already Booked"
- Payment was successful but seats were taken
- Consider adding seat reservation logic during payment
- Implement refund workflow

### Issue: "Amount Mismatch"
- Frontend and backend calculated different amounts
- Ensure calculation logic is identical

## Support & Resources

- **Razorpay Documentation:** https://razorpay.com/docs/
- **Razorpay Test Cards:** https://razorpay.com/docs/payments/payments/test-card-details/
- **Razorpay Dashboard:** https://dashboard.razorpay.com/
- **API Reference:** https://razorpay.com/docs/api/

## Next Steps

1. **Get Razorpay API Keys** from dashboard
2. **Configure** application.properties with your keys
3. **Test** the flow using Postman/cURL
4. **Integrate** frontend with Razorpay Checkout
5. **Test** end-to-end payment flow
6. **Go Live** with production keys

## Notes

- The old `POST /api/bookings/payment` endpoint still works with mock payment
- You can keep both flows during migration
- To fully remove mock payment, delete/deprecate the old endpoint
- Consider adding payment analytics and tracking
- Implement webhook handlers for better reliability
