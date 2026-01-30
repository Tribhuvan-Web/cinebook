# MovieSlotController - Complete API Documentation

This document provides comprehensive documentation for the MovieSlotController in the MovieDekho application, covering movie slot management operations for both administrators and regular users.

## Base URL
Assuming the application runs on localhost:8080, the base URL is:
```
http://localhost:8080/api/movie-slots
```

---

## 🎬 MOVIE SLOT MANAGEMENT ENDPOINTS

### 1. Create Movie Slot (Admin Only)
**Endpoint:** `POST /create`  
**Authentication:** Required (Admin Role)

Creates a new movie slot with show times and theater information. Only accessible by administrators.

```bash
curl -X POST http://localhost:8080/api/movie-slots/create \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "movieId": 1,
    "showDate": "2024-02-15",
    "showTime": "19:30:00",
    "endTime": "22:30:00",
    "theaterName": "PVR Cinemas - Mall of India",
    "screenNumber": "Screen 3",
    "totalSeats": 120,
    "availableSeats": 120,
    "pricePerSeat": 350.00,
    "slotType": "REGULAR"
  }'
```

### 2. Get All Movie Slots
**Endpoint:** `GET /all`  
**Authentication:** Optional

Retrieves all movie slots from the system. Accessible to all users.

```bash
curl -X GET http://localhost:8080/api/movie-slots/all \
  -H "Accept: application/json"
```

### 3. Get Movie Slot by ID
**Endpoint:** `GET /{id}`  
**Authentication:** Optional

Retrieves a specific movie slot by its ID.

```bash
# Get movie slot with ID 1
curl -X GET http://localhost:8080/api/movie-slots/1 \
  -H "Accept: application/json"

# Get movie slot with ID 5
curl -X GET http://localhost:8080/api/movie-slots/5 \
  -H "Accept: application/json"
```

### 4. Update Movie Slot (Admin Only)
**Endpoint:** `PUT /{id}`  
**Authentication:** Required (Admin Role)

Updates an existing movie slot. Only accessible by administrators.

```bash
curl -X PUT http://localhost:8080/api/movie-slots/1 \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "movieId": 1,
    "showDate": "2024-02-15",
    "showTime": "20:00:00",
    "endTime": "23:00:00",
    "theaterName": "PVR Cinemas - Mall of India",
    "screenNumber": "Screen 3",
    "totalSeats": 120,
    "availableSeats": 95,
    "pricePerSeat": 375.00,
    "slotType": "PREMIUM"
  }'
```

### 5. Delete Movie Slot (Admin Only)
**Endpoint:** `DELETE /{id}`  
**Authentication:** Required (Admin Role)

Removes a movie slot from the system. Only accessible by administrators.

```bash
curl -X DELETE http://localhost:8080/api/movie-slots/1 \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

### 6. Get Slots by Movie ID
**Endpoint:** `GET /movie/{movieId}`  
**Authentication:** Optional

Retrieves all slots for a specific movie.

```bash
# Get all slots for movie with ID 1
curl -X GET http://localhost:8080/api/movie-slots/movie/1 \
  -H "Accept: application/json"

# Get all slots for movie with ID 3
curl -X GET http://localhost:8080/api/movie-slots/movie/3 \
  -H "Accept: application/json"
```

### 7. Get Available Slots
**Endpoint:** `GET /available`  
**Authentication:** Optional

Retrieves only the slots that have available seats.

```bash
curl -X GET http://localhost:8080/api/movie-slots/available \
  -H "Accept: application/json"
```

### 8. Get Slots by Date
**Endpoint:** `GET /date/{date}`  
**Authentication:** Optional

Retrieves all slots for a specific date.

```bash
# Get slots for a specific date
curl -X GET http://localhost:8080/api/movie-slots/date/2024-02-15 \
  -H "Accept: application/json"

# Get slots for today (if today is 2024-02-10)
curl -X GET http://localhost:8080/api/movie-slots/date/2024-02-10 \
  -H "Accept: application/json"
```

### 9. Get Slots by Theater
**Endpoint:** `GET /theater/{theaterName}`  
**Authentication:** Optional

Retrieves all slots for a specific theater.

```bash
# Get slots for PVR Cinemas
curl -X GET "http://localhost:8080/api/movie-slots/theater/PVR%20Cinemas%20-%20Mall%20of%20India" \
  -H "Accept: application/json"

# Get slots for INOX theater
curl -X GET "http://localhost:8080/api/movie-slots/theater/INOX%20Mega%20Mall" \
  -H "Accept: application/json"
```

### 10. Search Movie Slots
**Endpoint:** `GET /search`  
**Authentication:** Optional

Searches for movie slots based on various criteria.

```bash
# Search by movie ID and date
curl -X GET "http://localhost:8080/api/movie-slots/search?movieId=1&showDate=2024-02-15" \
  -H "Accept: application/json"

# Search by theater name
curl -X GET "http://localhost:8080/api/movie-slots/search?theaterName=PVR%20Cinemas" \
  -H "Accept: application/json"

# Search by slot type
curl -X GET "http://localhost:8080/api/movie-slots/search?slotType=PREMIUM" \
  -H "Accept: application/json"

# Search by date range (from date)
curl -X GET "http://localhost:8080/api/movie-slots/search?fromDate=2024-02-15" \
  -H "Accept: application/json"

# Search by date range (to date)
curl -X GET "http://localhost:8080/api/movie-slots/search?toDate=2024-02-20" \
  -H "Accept: application/json"

# Combined search
curl -X GET "http://localhost:8080/api/movie-slots/search?movieId=1&theaterName=PVR&fromDate=2024-02-15&toDate=2024-02-20" \
  -H "Accept: application/json"
```

### 11. Get Slots by Price Range
**Endpoint:** `GET /price-range`  
**Authentication:** Optional

Retrieves slots within a specific price range.

```bash
# Get slots between ₹200 and ₹500
curl -X GET "http://localhost:8080/api/movie-slots/price-range?minPrice=200&maxPrice=500" \
  -H "Accept: application/json"

# Get budget-friendly slots (under ₹300)
curl -X GET "http://localhost:8080/api/movie-slots/price-range?minPrice=0&maxPrice=300" \
  -H "Accept: application/json"

# Get premium slots (above ₹400)
curl -X GET "http://localhost:8080/api/movie-slots/price-range?minPrice=400&maxPrice=1000" \
  -H "Accept: application/json"
```

### 12. Update Available Seats (Admin Only)
**Endpoint:** `PATCH /{id}/seats`  
**Authentication:** Required (Admin Role)

Updates the available seats count for a specific slot. Typically used after bookings.

```bash
curl -X PATCH http://localhost:8080/api/movie-slots/1/seats \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "availableSeats": 85
  }'
```

---

## 📊 EXAMPLE RESPONSES

### Successful Slot Creation
```json
{
  "id": 1,
  "movieId": 1,
  "movieTitle": "Avengers: Endgame",
  "showDate": "2024-02-15",
  "showTime": "19:30:00",
  "endTime": "22:30:00",
  "theaterName": "PVR Cinemas - Mall of India",
  "screenNumber": "Screen 3",
  "totalSeats": 120,
  "availableSeats": 120,
  "pricePerSeat": 350.00,
  "slotType": "REGULAR",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### All Movie Slots Response
```json
[
  {
    "id": 1,
    "movieId": 1,
    "movieTitle": "Avengers: Endgame",
    "showDate": "2024-02-15",
    "showTime": "19:30:00",
    "endTime": "22:30:00",
    "theaterName": "PVR Cinemas - Mall of India",
    "screenNumber": "Screen 3",
    "totalSeats": 120,
    "availableSeats": 120,
    "pricePerSeat": 350.00,
    "slotType": "REGULAR"
  },
  {
    "id": 2,
    "movieId": 2,
    "movieTitle": "The Dark Knight",
    "showDate": "2024-02-15",
    "showTime": "21:00:00",
    "endTime": "23:32:00",
    "theaterName": "INOX Mega Mall",
    "screenNumber": "Screen 1",
    "totalSeats": 150,
    "availableSeats": 142,
    "pricePerSeat": 400.00,
    "slotType": "PREMIUM"
  }
]
```

### Single Movie Slot Response
```json
{
  "id": 1,
  "movieId": 1,
  "movieTitle": "Avengers: Endgame",
  "showDate": "2024-02-15",
  "showTime": "19:30:00",
  "endTime": "22:30:00",
  "theaterName": "PVR Cinemas - Mall of India",
  "screenNumber": "Screen 3",
  "totalSeats": 120,
  "availableSeats": 95,
  "pricePerSeat": 350.00,
  "slotType": "REGULAR",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-20T14:45:00"
}
```

### Available Slots Response
```json
[
  {
    "id": 1,
    "movieId": 1,
    "movieTitle": "Avengers: Endgame",
    "showDate": "2024-02-15",
    "showTime": "19:30:00",
    "endTime": "22:30:00",
    "theaterName": "PVR Cinemas - Mall of India",
    "screenNumber": "Screen 3",
    "totalSeats": 120,
    "availableSeats": 95,
    "pricePerSeat": 350.00,
    "slotType": "REGULAR"
  },
  {
    "id": 3,
    "movieId": 3,
    "movieTitle": "Spider-Man: No Way Home",
    "showDate": "2024-02-16",
    "showTime": "18:00:00",
    "endTime": "20:28:00",
    "theaterName": "Cinepolis",
    "screenNumber": "Screen 2",
    "totalSeats": 100,
    "availableSeats": 67,
    "pricePerSeat": 325.00,
    "slotType": "REGULAR"
  }
]
```

### Search Results Response
```json
[
  {
    "id": 1,
    "movieId": 1,
    "movieTitle": "Avengers: Endgame",
    "showDate": "2024-02-15",
    "showTime": "19:30:00",
    "endTime": "22:30:00",
    "theaterName": "PVR Cinemas - Mall of India",
    "screenNumber": "Screen 3",
    "totalSeats": 120,
    "availableSeats": 95,
    "pricePerSeat": 350.00,
    "slotType": "REGULAR"
  },
  {
    "id": 4,
    "movieId": 1,
    "movieTitle": "Avengers: Endgame",
    "showDate": "2024-02-15",
    "showTime": "22:00:00",
    "endTime": "01:01:00",
    "theaterName": "PVR Cinemas - Mall of India",
    "screenNumber": "Screen 5",
    "totalSeats": 120,
    "availableSeats": 110,
    "pricePerSeat": 300.00,
    "slotType": "LATE_NIGHT"
  }
]
```

### Error Responses

**404 - Movie Slot Not Found:**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Movie slot not found with id: 999",
  "path": "/api/movie-slots/999"
}
```

**403 - Forbidden (Admin Only Endpoints):**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied. Admin role required.",
  "path": "/api/movie-slots/create"
}
```

**400 - Bad Request (Invalid Slot Data):**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/movie-slots/create",
  "validationErrors": [
    "Movie ID is required",
    "Show date cannot be in the past",
    "Total seats must be positive",
    "Price per seat must be positive"
  ]
}
```

**409 - Conflict (Slot Time Overlap):**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Slot time conflicts with existing slot in the same screen",
  "path": "/api/movie-slots/create"
}
```

---

## 📋 DATA TRANSFER OBJECTS (DTOs)

### MovieSlotDto (Request)
```json
{
  "movieId": 1,
  "showDate": "2024-02-15",
  "showTime": "19:30:00",
  "endTime": "22:30:00",
  "theaterName": "PVR Cinemas - Mall of India",
  "screenNumber": "Screen 3",
  "totalSeats": 120,
  "availableSeats": 120,
  "pricePerSeat": 350.00,
  "slotType": "REGULAR"
}
```

### MovieSlot Entity (Response)
```json
{
  "id": 1,
  "movieId": 1,
  "movieTitle": "Avengers: Endgame",
  "showDate": "2024-02-15",
  "showTime": "19:30:00",
  "endTime": "22:30:00",
  "theaterName": "PVR Cinemas - Mall of India",
  "screenNumber": "Screen 3",
  "totalSeats": 120,
  "availableSeats": 95,
  "pricePerSeat": 350.00,
  "slotType": "REGULAR",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-20T14:45:00"
}
```

### SeatUpdateRequest (For Seat Updates)
```json
{
  "availableSeats": 85
}
```

### SlotType Enum Values
- `REGULAR` - Standard pricing and timing
- `PREMIUM` - Premium experience with higher pricing
- `MATINEE` - Afternoon shows with discounted pricing
- `LATE_NIGHT` - Late night shows
- `WEEKEND_SPECIAL` - Weekend special pricing
- `HOLIDAY_SPECIAL` - Holiday special pricing

---

## 🎭 REAL-WORLD SCENARIOS

### Scenario 1: Admin Setting Up Multiple Slots for a Popular Movie

```bash
# Create morning show
curl -X POST http://localhost:8080/api/movie-slots/create \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "movieId": 1,
    "showDate": "2024-02-15",
    "showTime": "10:00:00",
    "endTime": "13:01:00",
    "theaterName": "PVR Cinemas - Mall of India",
    "screenNumber": "Screen 1",
    "totalSeats": 120,
    "availableSeats": 120,
    "pricePerSeat": 250.00,
    "slotType": "MATINEE"
  }'

# Create afternoon show
curl -X POST http://localhost:8080/api/movie-slots/create \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "movieId": 1,
    "showDate": "2024-02-15",
    "showTime": "14:30:00",
    "endTime": "17:31:00",
    "theaterName": "PVR Cinemas - Mall of India",
    "screenNumber": "Screen 1",
    "totalSeats": 120,
    "availableSeats": 120,
    "pricePerSeat": 350.00,
    "slotType": "REGULAR"
  }'

# Create evening show (premium)
curl -X POST http://localhost:8080/api/movie-slots/create \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "movieId": 1,
    "showDate": "2024-02-15",
    "showTime": "19:30:00",
    "endTime": "22:31:00",
    "theaterName": "PVR Cinemas - Mall of India",
    "screenNumber": "Screen 1",
    "totalSeats": 120,
    "availableSeats": 120,
    "pricePerSeat": 450.00,
    "slotType": "PREMIUM"
  }'

# Create late night show
curl -X POST http://localhost:8080/api/movie-slots/create \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "movieId": 1,
    "showDate": "2024-02-15",
    "showTime": "23:00:00",
    "endTime": "02:01:00",
    "theaterName": "PVR Cinemas - Mall of India",
    "screenNumber": "Screen 1",
    "totalSeats": 120,
    "availableSeats": 120,
    "pricePerSeat": 300.00,
    "slotType": "LATE_NIGHT"
  }'
```

### Scenario 2: User Browsing and Selecting Movie Slots

```bash
# User wants to watch a specific movie
curl -X GET http://localhost:8080/api/movie-slots/movie/1 \
  -H "Accept: application/json"

# User checks what's available today
curl -X GET http://localhost:8080/api/movie-slots/date/2024-02-15 \
  -H "Accept: application/json"

# User looks for budget-friendly options
curl -X GET "http://localhost:8080/api/movie-slots/price-range?minPrice=0&maxPrice=300" \
  -H "Accept: application/json"

# User checks available slots (with seats)
curl -X GET http://localhost:8080/api/movie-slots/available \
  -H "Accept: application/json"

# User wants to see shows at a specific theater
curl -X GET "http://localhost:8080/api/movie-slots/theater/PVR%20Cinemas%20-%20Mall%20of%20India" \
  -H "Accept: application/json"
```

### Scenario 3: Admin Managing Slot Operations

```bash
# Check all slots to monitor occupancy
curl -X GET http://localhost:8080/api/movie-slots/all \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Accept: application/json"

# Update pricing for a premium slot
curl -X PUT http://localhost:8080/api/movie-slots/1 \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "movieId": 1,
    "showDate": "2024-02-15",
    "showTime": "19:30:00",
    "endTime": "22:31:00",
    "theaterName": "PVR Cinemas - Mall of India",
    "screenNumber": "Screen 1",
    "totalSeats": 120,
    "availableSeats": 95,
    "pricePerSeat": 475.00,
    "slotType": "PREMIUM"
  }'

# After booking, update available seats
curl -X PATCH http://localhost:8080/api/movie-slots/1/seats \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "availableSeats": 90
  }'

# Cancel a slot if needed
curl -X DELETE http://localhost:8080/api/movie-slots/5 \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"
```

### Scenario 4: Advanced Search and Planning

```bash
# Find all slots for next week
curl -X GET "http://localhost:8080/api/movie-slots/search?fromDate=2024-02-20&toDate=2024-02-27" \
  -H "Accept: application/json"

# Find premium slots for a specific movie
curl -X GET "http://localhost:8080/api/movie-slots/search?movieId=1&slotType=PREMIUM" \
  -H "Accept: application/json"

# Find weekend slots at INOX theaters
curl -X GET "http://localhost:8080/api/movie-slots/search?theaterName=INOX&fromDate=2024-02-17&toDate=2024-02-18" \
  -H "Accept: application/json"

# Find matinee shows (budget-friendly)
curl -X GET "http://localhost:8080/api/movie-slots/search?slotType=MATINEE" \
  -H "Accept: application/json"
```

### Scenario 5: Theater-wise Operations

```bash
# Get all slots for PVR to check screen utilization
curl -X GET "http://localhost:8080/api/movie-slots/theater/PVR%20Cinemas%20-%20Mall%20of%20India" \
  -H "Accept: application/json"

# Create slots for different theaters
curl -X POST http://localhost:8080/api/movie-slots/create \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "movieId": 2,
    "showDate": "2024-02-16",
    "showTime": "20:00:00",
    "endTime": "22:32:00",
    "theaterName": "INOX Mega Mall",
    "screenNumber": "Screen 2",
    "totalSeats": 150,
    "availableSeats": 150,
    "pricePerSeat": 380.00,
    "slotType": "REGULAR"
  }'

curl -X POST http://localhost:8080/api/movie-slots/create \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "movieId": 3,
    "showDate": "2024-02-16",
    "showTime": "18:30:00",
    "endTime": "20:58:00",
    "theaterName": "Cinepolis Fun City",
    "screenNumber": "Screen 4",
    "totalSeats": 100,
    "availableSeats": 100,
    "pricePerSeat": 320.00,
    "slotType": "REGULAR"
  }'
```

---

## 🔧 TESTING DIFFERENT SCENARIOS

### Test Slot Validation

```bash
# Test creating slot with past date
curl -X POST http://localhost:8080/api/movie-slots/create \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "movieId": 1,
    "showDate": "2023-01-01",
    "showTime": "19:30:00",
    "endTime": "22:30:00",
    "theaterName": "Test Theater",
    "screenNumber": "Screen 1",
    "totalSeats": 100,
    "availableSeats": 100,
    "pricePerSeat": 300.00,
    "slotType": "REGULAR"
  }'

# Test creating slot with invalid time (end before start)
curl -X POST http://localhost:8080/api/movie-slots/create \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "movieId": 1,
    "showDate": "2024-02-20",
    "showTime": "22:30:00",
    "endTime": "19:30:00",
    "theaterName": "Test Theater",
    "screenNumber": "Screen 1",
    "totalSeats": 100,
    "availableSeats": 100,
    "pricePerSeat": 300.00,
    "slotType": "REGULAR"
  }'

# Test creating slot with negative pricing
curl -X POST http://localhost:8080/api/movie-slots/create \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "movieId": 1,
    "showDate": "2024-02-20",
    "showTime": "19:30:00",
    "endTime": "22:30:00",
    "theaterName": "Test Theater",
    "screenNumber": "Screen 1",
    "totalSeats": 100,
    "availableSeats": 100,
    "pricePerSeat": -50.00,
    "slotType": "REGULAR"
  }'
```

### Test Access Control

```bash
# Test admin operations without authentication
curl -X POST http://localhost:8080/api/movie-slots/create \
  -H "Content-Type: application/json" \
  -d '{
    "movieId": 1,
    "showDate": "2024-02-20",
    "showTime": "19:30:00",
    "endTime": "22:30:00",
    "theaterName": "Unauthorized Theater",
    "screenNumber": "Screen 1",
    "totalSeats": 100,
    "availableSeats": 100,
    "pricePerSeat": 300.00,
    "slotType": "REGULAR"
  }'

# Test admin operations with user token
curl -X POST http://localhost:8080/api/movie-slots/create \
  -H "Authorization: Bearer USER_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "movieId": 1,
    "showDate": "2024-02-20",
    "showTime": "19:30:00",
    "endTime": "22:30:00",
    "theaterName": "User Theater",
    "screenNumber": "Screen 1",
    "totalSeats": 100,
    "availableSeats": 100,
    "pricePerSeat": 300.00,
    "slotType": "REGULAR"
  }'
```

### Test Edge Cases

```bash
# Test search with invalid movie ID
curl -X GET http://localhost:8080/api/movie-slots/movie/999999 \
  -H "Accept: application/json"

# Test search with invalid date format
curl -X GET http://localhost:8080/api/movie-slots/date/invalid-date \
  -H "Accept: application/json"

# Test price range with invalid parameters
curl -X GET "http://localhost:8080/api/movie-slots/price-range?minPrice=1000&maxPrice=100" \
  -H "Accept: application/json"

# Test updating seats to exceed total capacity
curl -X PATCH http://localhost:8080/api/movie-slots/1/seats \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "availableSeats": 150
  }'
```

---

## 💻 PowerShell Examples (For Windows Users)

```powershell
# Setup admin token
$adminToken = "YOUR_ADMIN_JWT_TOKEN"

# Create multiple slots for a movie
$slot1 = @{
    movieId = 1
    showDate = "2024-02-15"
    showTime = "10:00:00"
    endTime = "13:01:00"
    theaterName = "PVR Cinemas - Mall of India"
    screenNumber = "Screen 1"
    totalSeats = 120
    availableSeats = 120
    pricePerSeat = 250.00
    slotType = "MATINEE"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/movie-slots/create" `
  -Method POST `
  -Headers @{
    "Authorization" = "Bearer $adminToken"
    "Content-Type" = "application/json"
  } `
  -Body $slot1

# Get all available slots
$availableSlots = Invoke-RestMethod -Uri "http://localhost:8080/api/movie-slots/available" `
  -Method GET `
  -Headers @{"Accept" = "application/json"}

Write-Output "Available slots: $($availableSlots.Count)"

# Search for slots by movie
$movieSlots = Invoke-RestMethod -Uri "http://localhost:8080/api/movie-slots/movie/1" `
  -Method GET `
  -Headers @{"Accept" = "application/json"}

foreach ($slot in $movieSlots) {
    Write-Output "Slot ID: $($slot.id) - Time: $($slot.showTime) - Available: $($slot.availableSeats)/$($slot.totalSeats)"
}

# Find budget-friendly slots
$budgetSlots = Invoke-RestMethod -Uri "http://localhost:8080/api/movie-slots/price-range?minPrice=0&maxPrice=300" `
  -Method GET `
  -Headers @{"Accept" = "application/json"}

Write-Output "Budget-friendly slots: $($budgetSlots.Count)"

# Update slot after booking
$seatUpdate = @{
    availableSeats = 85
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/movie-slots/1/seats" `
  -Method PATCH `
  -Headers @{
    "Authorization" = "Bearer $adminToken"
    "Content-Type" = "application/json"
  } `
  -Body $seatUpdate

# Get today's slots
$today = Get-Date -Format "yyyy-MM-dd"
$todaySlots = Invoke-RestMethod -Uri "http://localhost:8080/api/movie-slots/date/$today" `
  -Method GET `
  -Headers @{"Accept" = "application/json"}

Write-Output "Slots today: $($todaySlots.Count)"
```

---

## 🎯 KEY FEATURES

### Slot Management:
- ✅ **CRUD Operations** - Complete create, read, update, delete functionality
- ✅ **Admin Controls** - Secure admin-only slot management
- ✅ **Real-time Availability** - Dynamic seat availability tracking
- ✅ **Time Validation** - Prevent overlapping shows and past date bookings

### Search & Discovery:
- ✅ **Multi-criteria Search** - Search by movie, theater, date, price, type
- ✅ **Date-based Filtering** - Find slots for specific dates or date ranges
- ✅ **Theater-wise Filtering** - Filter by specific theaters
- ✅ **Price Range Filtering** - Find slots within budget
- ✅ **Availability Filtering** - Show only slots with available seats

### Business Intelligence:
- ✅ **Slot Types** - Support for different pricing and experience tiers
- ✅ **Dynamic Pricing** - Flexible pricing per slot
- ✅ **Capacity Management** - Track total and available seats
- ✅ **Theater Integration** - Multi-theater support with screen management

### User Experience:
- ✅ **Public Browsing** - View slots without authentication
- ✅ **Detailed Information** - Complete slot metadata including movie details
- ✅ **Flexible Booking** - Support various show times and theaters
- ✅ **Real-time Updates** - Immediate availability updates

---

## 📝 IMPORTANT NOTES

1. **Authentication:** Admin operations require valid JWT token with admin role
2. **Time Validation:** Slots cannot be created for past dates
3. **Seat Management:** Available seats cannot exceed total seats
4. **Screen Conflicts:** Validate screen availability before creating overlapping slots
5. **Pricing:** All prices should be positive values
6. **Date Format:** Use ISO date format (YYYY-MM-DD) for dates
7. **Time Format:** Use HH:MM:SS format for times
8. **Slot Types:** Use predefined enum values for slot types
9. **Theater Names:** Consistent theater naming for proper filtering
10. **Booking Integration:** Update available seats after each booking

---

## 🔄 INTEGRATION PATTERNS

### Frontend Integration:
```javascript
// Slot browsing functionality
const getAvailableSlots = async () => {
  const response = await fetch('/api/movie-slots/available');
  return await response.json();
};

const searchSlots = async (searchParams) => {
  const queryString = new URLSearchParams(searchParams).toString();
  const response = await fetch(`/api/movie-slots/search?${queryString}`);
  return await response.json();
};

const getMovieSlots = async (movieId) => {
  const response = await fetch(`/api/movie-slots/movie/${movieId}`);
  return await response.json();
};

// Admin slot management
const createSlot = async (slotData, adminToken) => {
  const response = await fetch('/api/movie-slots/create', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${adminToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(slotData)
  });
  return await response.json();
};

const updateAvailableSeats = async (slotId, availableSeats, adminToken) => {
  await fetch(`/api/movie-slots/${slotId}/seats`, {
    method: 'PATCH',
    headers: {
      'Authorization': `Bearer ${adminToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ availableSeats })
  });
};
```

### Booking System Integration:
```bash
# Typical booking flow
# 1. User searches for movie slots
curl -X GET http://localhost:8080/api/movie-slots/movie/1

# 2. User selects a slot
curl -X GET http://localhost:8080/api/movie-slots/1

# 3. System can use slot ID for seat selection
curl -X GET http://localhost:8080/api/seats/slot/1

# 4. After booking, update available seats
curl -X PATCH http://localhost:8080/api/movie-slots/1/seats \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -d '{"availableSeats": 115}'
```

---

## 🚀 GETTING STARTED

1. **Browse Slots:** Use GET /all or /available to see current offerings
2. **Search:** Use search endpoints to find specific slots
3. **Admin Setup:** Ensure admin authentication for management operations
4. **Seat Management:** Integrate with booking system for real-time updates
5. **Theater Planning:** Use theater and date filters for operational insights
