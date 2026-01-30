# SeatController - Complete API Documentation

This document provides comprehensive documentation for the SeatController in the MovieDekho application, covering both Admin and User perspectives with complete CURL request examples.

## Base URL
Assuming the application runs on localhost:8080, the base URL is:
```
http://localhost:8080/api/seats
```

---

## 🔐 ADMIN ENDPOINTS

### 1. Create Seats for a Movie Slot
**Endpoint:** `POST /admin/slot/{slotId}`

Creates multiple seats for a specific movie slot.

```bash
curl -X POST http://localhost:8080/api/seats/admin/slot/1 \
  -H "Content-Type: application/json" \
  -d '[
    {
      "seatNumber": "A1",
      "price": 150.0
    },
    {
      "seatNumber": "A2",
      "price": 150.0
    },
    {
      "seatNumber": "A3",
      "price": 150.0
    }
  ]'
```

### 2. Get All Seats for a Slot (Admin View)
**Endpoint:** `GET /admin/slot/{slotId}`

Retrieves all seats for a specific slot with complete details.

```bash
curl -X GET http://localhost:8080/api/seats/admin/slot/1 \
  -H "Accept: application/json"
```

### 3. Update Seat Details
**Endpoint:** `PUT /admin/{seatId}`

Updates seat information such as seat number, price, or booking status.

```bash
# Update seat number and price
curl -X PUT http://localhost:8080/api/seats/admin/1 \
  -H "Content-Type: application/json" \
  -d '{
    "seatNumber": "A1-Premium",
    "price": 200.0
  }'

# Update only booking status
curl -X PUT http://localhost:8080/api/seats/admin/1 \
  -H "Content-Type: application/json" \
  -d '{
    "booked": true
  }'

# Update only price
curl -X PUT http://localhost:8080/api/seats/admin/1 \
  -H "Content-Type: application/json" \
  -d '{
    "price": 175.0
  }'
```

### 4. Delete a Seat
**Endpoint:** `DELETE /admin/{seatId}`

Removes a seat from the system and updates slot totals.

```bash
curl -X DELETE http://localhost:8080/api/seats/admin/1 \
  -H "Accept: application/json"
```

### 5. Update Seat Booking Status
**Endpoint:** `PUT /admin/{seatId}/booking-status`

Specifically updates the booking status of a seat.

```bash
# Mark seat as booked
curl -X PUT "http://localhost:8080/api/seats/admin/1/booking-status?isBooked=true" \
  -H "Accept: application/json"

# Mark seat as available
curl -X PUT "http://localhost:8080/api/seats/admin/1/booking-status?isBooked=false" \
  -H "Accept: application/json"
```

### 6. Bulk Create Seats with Pattern
**Endpoint:** `POST /admin/slot/{slotId}/bulk`

Creates seats in bulk using a row pattern (e.g., A1-A10, B1-B10, etc.).

```bash
# Create seats from row A to E, 10 seats per row, at 150.0 per seat
curl -X POST "http://localhost:8080/api/seats/admin/slot/1/bulk?rowStart=A&rowEnd=E&seatsPerRow=10&price=150.0" \
  -H "Accept: application/json"

# Create premium seats from row F to H, 8 seats per row, at 250.0 per seat
curl -X POST "http://localhost:8080/api/seats/admin/slot/1/bulk?rowStart=F&rowEnd=H&seatsPerRow=8&price=250.0" \
  -H "Accept: application/json"
```

---

## 👥 USER ENDPOINTS

### 7. Get Available Seats for a Slot
**Endpoint:** `GET /slot/{slotId}/available`

Returns only the seats that are available for booking.

```bash
curl -X GET http://localhost:8080/api/seats/slot/1/available \
  -H "Accept: application/json"
```

=> Completed Till here

### 8. Get Booked Seats for a Slot
**Endpoint:** `GET /slot/{slotId}/booked`

Returns only the seats that are already booked.

```bash
curl -X GET http://localhost:8080/api/seats/slot/1/booked \
  -H "Accept: application/json"
```

### 9. Get All Seats with Status
**Endpoint:** `GET /slot/{slotId}`

Returns all seats for a slot with their current booking status.

```bash
curl -X GET http://localhost:8080/api/seats/slot/1 \
  -H "Accept: application/json"
```

### 10. Get Seat by ID
**Endpoint:** `GET /{seatId}`

Retrieves details of a specific seat.

```bash
curl -X GET http://localhost:8080/api/seats/1 \
  -H "Accept: application/json"
```

### 11. Check Seat Availability
**Endpoint:** `GET /{seatId}/availability`

Checks if a specific seat is available for booking.

```bash
curl -X GET http://localhost:8080/api/seats/1/availability \
  -H "Accept: application/json"
```

### 12. Get Seats by Price Range
**Endpoint:** `GET /slot/{slotId}/price-range`

Finds seats within a specific price range.

```bash
# Get seats between 100 and 200
curl -X GET "http://localhost:8080/api/seats/slot/1/price-range?minPrice=100.0&maxPrice=200.0" \
  -H "Accept: application/json"

# Get premium seats (above 250)
curl -X GET "http://localhost:8080/api/seats/slot/1/price-range?minPrice=250.0&maxPrice=500.0" \
  -H "Accept: application/json"

# Get economy seats (below 150)
curl -X GET "http://localhost:8080/api/seats/slot/1/price-range?minPrice=50.0&maxPrice=150.0" \
  -H "Accept: application/json"
```

---

## 📊 EXAMPLE RESPONSES

### Successful SeatResponse
```json
{
  "seatId": 1,
  "seatNumber": "A1",
  "booked": false,
  "price": 150.0,
  "slotId": 1
}
```

### Multiple Seats Response
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
    "booked": true,
    "price": 150.0,
    "slotId": 1
  }
]
```

### Seat Availability Response
```json
{
  "seatId": 1,
  "seatNumber": "A1",
  "available": true,
  "price": 150.0
}
```

### Error Responses

**404 - Not Found:**
```json
"Seat not found with ID: 1"
```

**400 - Bad Request (Duplicate seat numbers):**
```json
"Seat numbers already exist: [A1, A2]"
```

**500 - Internal Server Error:**
```json
"Error creating seats: Some error message"
```

---

## 📋 DATA TRANSFER OBJECTS (DTOs)

### SeatRequest (For Creation)
```json
{
  "seatNumber": "A1",
  "price": 150.0
}
```

### SeatUpdateRequest (For Updates)
```json
{
  "seatNumber": "A1-Premium",
  "price": 200.0,
  "booked": true
}
```

### SeatResponse (API Response)
```json
{
  "seatId": 1,
  "seatNumber": "A1",
  "booked": false,
  "price": 150.0,
  "slotId": 1
}
```

---

## 🎭 REAL-WORLD SCENARIOS

### Scenario 1: Setting up a New Theater Screen

```bash
# Step 1: Create regular seats (Rows A-H, 12 seats each)
curl -X POST "http://localhost:8080/api/seats/admin/slot/1/bulk?rowStart=A&rowEnd=H&seatsPerRow=12&price=120.0" \
  -H "Accept: application/json"

# Step 2: Create premium seats (Rows I-K, 10 seats each)
curl -X POST "http://localhost:8080/api/seats/admin/slot/1/bulk?rowStart=I&rowEnd=K&seatsPerRow=10&price=180.0" \
  -H "Accept: application/json"

# Step 3: Create VIP seats (Row L, 8 seats)
curl -X POST "http://localhost:8080/api/seats/admin/slot/1/bulk?rowStart=L&rowEnd=L&seatsPerRow=8&price=250.0" \
  -H "Accept: application/json"
```

### Scenario 2: Customer Browsing Available Seats

```bash
# Step 1: Check all available seats
curl -X GET http://localhost:8080/api/seats/slot/1/available \
  -H "Accept: application/json"

# Step 2: Filter by budget (under 150)
curl -X GET "http://localhost:8080/api/seats/slot/1/price-range?minPrice=50.0&maxPrice=150.0" \
  -H "Accept: application/json"

# Step 3: Check specific seat availability
curl -X GET http://localhost:8080/api/seats/15/availability \
  -H "Accept: application/json"
```

### Scenario 3: Admin Managing Bookings

```bash
# Check current booking status
curl -X GET http://localhost:8080/api/seats/admin/slot/1 \
  -H "Accept: application/json"

# Manually book a seat (for phone bookings)
curl -X PUT "http://localhost:8080/api/seats/admin/5/booking-status?isBooked=true" \
  -H "Accept: application/json"

# Cancel a booking
curl -X PUT "http://localhost:8080/api/seats/admin/5/booking-status?isBooked=false" \
  -H "Accept: application/json"

# Update seat price (dynamic pricing)
curl -X PUT http://localhost:8080/api/seats/admin/5 \
  -H "Content-Type: application/json" \
  -d '{
    "price": 200.0
  }'
```

### Scenario 4: Different Seat Categories

```bash
# Create Economy Section (Rows A-C)
curl -X POST "http://localhost:8080/api/seats/admin/slot/2/bulk?rowStart=A&rowEnd=C&seatsPerRow=15&price=100.0" \
  -H "Accept: application/json"

# Create Standard Section (Rows D-G)
curl -X POST "http://localhost:8080/api/seats/admin/slot/2/bulk?rowStart=D&rowEnd=G&seatsPerRow=12&price=150.0" \
  -H "Accept: application/json"

# Create Premium Section (Rows H-J)
curl -X POST "http://localhost:8080/api/seats/admin/slot/2/bulk?rowStart=H&rowEnd=J&seatsPerRow=10&price=200.0" \
  -H "Accept: application/json"

# Create Recliner Section (Rows K-L)
curl -X POST "http://localhost:8080/api/seats/admin/slot/2/bulk?rowStart=K&rowEnd=L&seatsPerRow=6&price=300.0" \
  -H "Accept: application/json"
```

---

## 🔧 TESTING DIFFERENT SCENARIOS

### Test Error Handling

```bash
# Test 404 - Non-existent slot
curl -X GET http://localhost:8080/api/seats/slot/999/available \
  -H "Accept: application/json"

# Test 404 - Non-existent seat
curl -X GET http://localhost:8080/api/seats/999 \
  -H "Accept: application/json"

# Test duplicate seat creation
curl -X POST http://localhost:8080/api/seats/admin/slot/1 \
  -H "Content-Type: application/json" \
  -d '[
    {
      "seatNumber": "A1",
      "price": 150.0
    },
    {
      "seatNumber": "A1",
      "price": 150.0
    }
  ]'
```

### Test Bulk Operations

```bash
# Large theater setup (A-Z rows, 20 seats each)
curl -X POST "http://localhost:8080/api/seats/admin/slot/3/bulk?rowStart=A&rowEnd=Z&seatsPerRow=20&price=130.0" \
  -H "Accept: application/json"

# Small intimate theater (A-E rows, 6 seats each)
curl -X POST "http://localhost:8080/api/seats/admin/slot/4/bulk?rowStart=A&rowEnd=E&seatsPerRow=6&price=180.0" \
  -H "Accept: application/json"
```

---

## 💻 PowerShell Examples (For Windows Users)

```powershell
# Create seats using PowerShell
Invoke-RestMethod -Uri "http://localhost:8080/api/seats/admin/slot/1" `
  -Method POST `
  -Headers @{"Content-Type"="application/json"} `
  -Body '[
    {
      "seatNumber": "A1",
      "price": 150.0
    },
    {
      "seatNumber": "A2",
      "price": 150.0
    }
  ]'

# Get available seats
Invoke-RestMethod -Uri "http://localhost:8080/api/seats/slot/1/available" `
  -Method GET `
  -Headers @{"Accept"="application/json"}

# Bulk create seats
Invoke-RestMethod -Uri "http://localhost:8080/api/seats/admin/slot/1/bulk?rowStart=A&rowEnd=E&seatsPerRow=10&price=150.0" `
  -Method POST `
  -Headers @{"Accept"="application/json"}
```

---

## 🎯 KEY FEATURES

### Admin Features:
- ✅ **Create individual or bulk seats**
- ✅ **Update seat details** (number, price, booking status)
- ✅ **Delete seats** with automatic slot total updates
- ✅ **Bulk seat generation** with row patterns
- ✅ **Manual booking management** for phone/counter bookings
- ✅ **Complete seat overview** for administrative purposes

### User Features:
- ✅ **View available seats** for booking selection
- ✅ **Check seat availability** before booking
- ✅ **Filter by price range** for budget-conscious selection
- ✅ **View all seats with status** for informed decisions
- ✅ **Get specific seat details** for verification

### Smart Features:
- ✅ **Duplicate prevention** - prevents duplicate seat numbers
- ✅ **Automatic slot updates** - maintains accurate totals
- ✅ **Price range filtering** - helps users find suitable seats
- ✅ **Pattern-based bulk creation** - efficient theater setup
- ✅ **Comprehensive error handling** - user-friendly error messages

---

## 📝 IMPORTANT NOTES

1. **Seat Numbering:** Follow standard theater conventions (A1, A2, B1, B2, etc.)
2. **Price Management:** Prices can be updated for dynamic pricing strategies
3. **Booking Status:** Only admin can directly modify booking status
4. **Bulk Operations:** Use bulk creation for efficient theater setup
5. **Error Handling:** All endpoints include comprehensive error responses
6. **Data Validation:** Duplicate seat numbers within the same slot are prevented
7. **Automatic Updates:** Slot totals are automatically maintained

---

## 🔄 INTEGRATION WITH BOOKING SYSTEM

This SeatController is designed to integrate seamlessly with a future BookingController. The seat availability and booking status management provides the foundation for:

- **Seat Selection Process**
- **Booking Confirmation**
- **Payment Processing Integration**
- **Booking History Management**
- **Cancellation Handling**

The current API ensures that seat data remains consistent and accurate for any booking operations that will be implemented later.

---

## 🚀 GETTING STARTED

1. **Setup Theater:** Use bulk creation endpoints to set up your theater layout
2. **Configure Pricing:** Set appropriate prices for different seat categories
3. **Monitor Availability:** Use user endpoints to check real-time availability
4. **Manage Bookings:** Use admin endpoints for manual booking management
5. **Integrate Frontend:** Use the standardized API responses in your UI components
