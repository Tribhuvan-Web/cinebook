# Ticket Verification System - Complete Documentation

## 🎯 Overview
The CineBook ticket verification system provides secure QR code-based ticket verification functionality. This comprehensive system automatically generates unique verification tokens and QR codes for each confirmed booking, enabling real-time ticket verification by cinema administrators.

## ✨ Key Features

### 🔐 Automatic QR Code Generation
- **Trigger**: Automatically generated when booking status changes to confirmed
- **Components**: 
  - **Verification Token**: Base64-encoded UUID for uniqueness
  - **Random String**: 8-character alphanumeric string for additional security
  - **QR Code**: Combined format `{verification_token}:{random_string}`
- **Security**: Non-predictable, unique per booking, URL-safe encoding

### 👤 User Experience
- View ticket details with embedded QR code
- Check verification status in real-time
- Secure access to own tickets only
- Mobile-friendly QR code display

### 👨‍💼 Admin Management
- Real-time ticket verification via QR scanning
- Dashboard view of all tickets for specific shows
- Verification statistics and reporting
- Comprehensive audit trail

## 🗄️ Database Schema

### New Columns Added to `bookings` Table
```sql
ALTER TABLE bookings ADD COLUMN verification_token VARCHAR(255);
ALTER TABLE bookings ADD COLUMN random_string VARCHAR(100);
ALTER TABLE bookings ADD COLUMN qr_code VARCHAR(355);
ALTER TABLE bookings ADD COLUMN is_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE bookings ADD COLUMN verification_time TIMESTAMP NULL;
ALTER TABLE bookings ADD COLUMN verified_by VARCHAR(255);
```

### Performance Indexes
```sql
CREATE INDEX idx_bookings_qr_code ON bookings(qr_code);
CREATE INDEX idx_bookings_verification_status ON bookings(is_verified);
CREATE INDEX idx_bookings_verification_token ON bookings(verification_token);
CREATE INDEX idx_bookings_cinema_date ON bookings(cinema_name, show_date);
```

### Column Descriptions
| Column | Type | Description |
|--------|------|-------------|
| `verification_token` | VARCHAR(255) | Base64-encoded UUID for unique identification |
| `random_string` | VARCHAR(100) | 8-character random string for additional security |
| `qr_code` | VARCHAR(355) | Combined QR code string (token:random_string) |
| `is_verified` | BOOLEAN | Verification status (default: false) |
| `verification_time` | TIMESTAMP | When the ticket was verified |
| `verified_by` | VARCHAR(255) | Email of admin who verified the ticket |

## 🔌 API Endpoints

### 👤 User Endpoints

#### Get Ticket Details
```http
GET /api/bookings/{bookingId}/ticket
Authorization: Bearer <jwt_token>
```

**Description**: Retrieve ticket details with QR code for a specific booking

**Response Example**:
```json
{
    "verificationToken": "YWJjZGVmZ2hpams",
    "qrCode": "YWJjZGVmZ2hpams:A1B2C3D4",
    "bookingId": 123,
    "movieTitle": "Avengers: Endgame",
    "cinemaName": "PVR Cinemas",
    "showDate": "2025-08-05",
    "showTime": "14:00:00",
    "seatNumbers": "A1, A2, A3",
    "userEmail": "user@example.com",
    "verified": false,
    "verificationTime": null,
    "verifiedBy": null,
    "totalSeats": 3,
    "bookingStatus": "CONFIRMED"
}
```

### 👨‍💼 Admin Endpoints

#### Verify Ticket
```http
POST /api/bookings/admin/verify-ticket
Authorization: Bearer <admin_jwt_token>
Content-Type: application/json

{
    "qrCode": "YWJjZGVmZ2hpams:A1B2C3D4"
}
```

**Success Response**:
```json
{
    "valid": true,
    "message": "Ticket verified successfully",
    "ticketDetails": {
        "bookingId": 123,
        "movieTitle": "Avengers: Endgame",
        "cinemaName": "PVR Cinemas",
        "showDate": "2025-08-05",
        "showTime": "14:00:00",
        "seatNumbers": "A1, A2, A3",
        "userEmail": "user@example.com",
        "verified": true,
        "totalSeats": 3
    },
    "verificationTime": "2025-08-16T10:30:00Z"
}
```

**Error Response**:
```json
{
    "valid": false,
    "message": "Invalid ticket QR code",
    "error": "INVALID_QR_CODE"
}
```

#### Get Show Tickets
```http
GET /api/bookings/admin/slot/{slotId}/tickets
Authorization: Bearer <admin_jwt_token>
```

**Description**: Get all tickets for a specific movie slot/show

**Response Example**:
```json
{
    "slotId": 456,
    "movieTitle": "Avengers: Endgame",
    "showTime": "14:00:00",
    "showDate": "2025-08-05",
    "cinemaName": "PVR Cinemas",
    "tickets": [
        {
            "bookingId": 123,
            "qrCode": "YWJjZGVmZ2hpams:A1B2C3D4",
            "seatNumbers": "A1, A2, A3",
            "userEmail": "user@example.com",
            "verified": false,
            "totalSeats": 3
        }
    ],
    "totalTickets": 1,
    "verifiedTickets": 0,
    "pendingTickets": 1
}
```

#### Get Cinema Today's Tickets
```http
GET /api/bookings/admin/cinema/{cinemaName}/today-tickets
Authorization: Bearer <admin_jwt_token>
```

**Description**: Get all today's tickets for a specific cinema

#### Get Verification Summary
```http
GET /api/bookings/admin/verification-summary
Authorization: Bearer <admin_jwt_token>
```

**Response Example**:
```json
{
    "totalTicketsToday": 150,
    "verifiedTicketsToday": 120,
    "pendingTicketsToday": 30,
    "verificationRate": 80.0,
    "recentVerifications": [
        {
            "bookingId": 123,
            "movieTitle": "Avengers: Endgame",
            "verificationTime": "2025-08-16T10:30:00Z",
            "verifiedBy": "admin@cinema.com"
        }
    ]
}
```

## 🔒 Security Implementation

### QR Code Security
- **Unique Generation**: Each booking receives a cryptographically unique verification token
- **Random Component**: 8-character random string prevents pattern prediction
- **Base64 Encoding**: Ensures URL safety and prevents character conflicts
- **Format Validation**: Server validates QR code format before processing

### Access Control
- **JWT Authentication**: All endpoints require valid JWT tokens
- **Role-Based Authorization**: Admin endpoints restricted to ADMIN role
- **Ownership Validation**: Users can only access their own booking tickets
- **Request Validation**: Input sanitization and validation on all endpoints

### Audit Trail
- **Verification Logging**: Complete record of all verification attempts
- **Admin Tracking**: Records which administrator performed each verification
- **Timestamp Precision**: Accurate verification time recording
- **Status History**: Maintains verification state changes

## 🔄 System Workflow

### User Journey
1. **Booking Creation** → User completes booking process
2. **Booking Confirmation** → Payment processed, booking status set to CONFIRMED
3. **Auto QR Generation** → System automatically generates:
   - Unique verification token (Base64 UUID)
   - Random 8-character string
   - Combined QR code string
4. **Ticket Access** → User can view ticket details with QR code
5. **Cinema Arrival** → User presents QR code for verification

### Admin Verification Flow
1. **Dashboard Access** → Admin views today's tickets or specific show tickets
2. **QR Code Scanning** → Admin scans or manually enters QR code
3. **System Validation** → Server validates QR format and booking status
4. **Verification Recording** → System records:
   - Verification timestamp
   - Admin identifier
   - Booking status update
5. **Confirmation** → Admin receives verification confirmation with ticket details

## 💻 Frontend Integration

### User Panel - Ticket Display
```javascript
// Fetch and display user ticket
const displayUserTicket = async (bookingId) => {
    try {
        const response = await fetch(`/api/bookings/${bookingId}/ticket`, {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('userToken')}`,
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to fetch ticket details');
        }
        
        const ticket = await response.json();
        
        // Generate QR code using qrcode library
        const qrCodeElement = document.getElementById('qr-code');
        QRCode.toCanvas(qrCodeElement, ticket.qrCode, {
            width: 200,
            margin: 2
        });
        
        // Display ticket information
        document.getElementById('movie-title').textContent = ticket.movieTitle;
        document.getElementById('cinema-name').textContent = ticket.cinemaName;
        document.getElementById('show-time').textContent = `${ticket.showDate} ${ticket.showTime}`;
        document.getElementById('seats').textContent = ticket.seatNumbers;
        
        // Show verification status
        const statusElement = document.getElementById('verification-status');
        if (ticket.verified) {
            statusElement.innerHTML = `
                <span class="verified">✅ Verified</span>
                <small>Verified on ${new Date(ticket.verificationTime).toLocaleString()}</small>
            `;
        } else {
            statusElement.innerHTML = '<span class="pending">⏳ Not Verified</span>';
        }
        
    } catch (error) {
        console.error('Error displaying ticket:', error);
        showErrorMessage('Failed to load ticket details');
    }
};
```

### Admin Panel - Verification Dashboard
```javascript
// Admin ticket verification system
class TicketVerificationAdmin {
    constructor(adminToken) {
        this.adminToken = adminToken;
        this.apiHeaders = {
            'Authorization': `Bearer ${adminToken}`,
            'Content-Type': 'application/json'
        };
    }
    
    // Verify ticket using QR code
    async verifyTicket(qrCode) {
        try {
            const response = await fetch('/api/bookings/admin/verify-ticket', {
                method: 'POST',
                headers: this.apiHeaders,
                body: JSON.stringify({ qrCode })
            });
            
            const result = await response.json();
            
            if (result.valid) {
                this.showVerificationSuccess(result);
                this.updateTicketStatus(result.ticketDetails.bookingId, true);
            } else {
                this.showVerificationError(result.message);
            }
            
            return result;
        } catch (error) {
            console.error('Verification error:', error);
            this.showVerificationError('Network error during verification');
        }
    }
    
    // Load tickets for a specific show
    async loadShowTickets(slotId) {
        try {
            const response = await fetch(`/api/bookings/admin/slot/${slotId}/tickets`, {
                headers: this.apiHeaders
            });
            
            const data = await response.json();
            this.displayTicketsList(data.tickets);
            this.updateShowSummary(data);
            
        } catch (error) {
            console.error('Error loading show tickets:', error);
        }
    }
    
    // Display tickets in admin dashboard
    displayTicketsList(tickets) {
        const container = document.getElementById('tickets-list');
        container.innerHTML = tickets.map(ticket => `
            <div class="ticket-card ${ticket.verified ? 'verified' : 'pending'}">
                <div class="ticket-info">
                    <h4>Booking #${ticket.bookingId}</h4>
                    <p><strong>Seats:</strong> ${ticket.seatNumbers}</p>
                    <p><strong>User:</strong> ${ticket.userEmail}</p>
                    <p><strong>Status:</strong> 
                        ${ticket.verified ? 
                            `✅ Verified on ${new Date(ticket.verificationTime).toLocaleString()}` : 
                            '⏳ Pending Verification'
                        }
                    </p>
                </div>
                <div class="ticket-qr">
                    <canvas id="qr-${ticket.bookingId}"></canvas>
                    ${!ticket.verified ? 
                        `<button onclick="verifyTicket('${ticket.qrCode}')" class="verify-btn">
                            Verify Ticket
                        </button>` : 
                        '<span class="verified-badge">Verified ✓</span>'
                    }
                </div>
            </div>
        `).join('');
        
        // Generate QR codes for each ticket
        tickets.forEach(ticket => {
            const canvas = document.getElementById(`qr-${ticket.bookingId}`);
            QRCode.toCanvas(canvas, ticket.qrCode, { width: 100 });
        });
    }
    
    // Show verification success message
    showVerificationSuccess(result) {
        const message = `
            <div class="success-message">
                <h3>✅ Ticket Verified Successfully!</h3>
                <p><strong>Movie:</strong> ${result.ticketDetails.movieTitle}</p>
                <p><strong>Seats:</strong> ${result.ticketDetails.seatNumbers}</p>
                <p><strong>User:</strong> ${result.ticketDetails.userEmail}</p>
                <p><strong>Verified at:</strong> ${new Date(result.verificationTime).toLocaleString()}</p>
            </div>
        `;
        document.getElementById('verification-result').innerHTML = message;
    }
    
    // Show verification error
    showVerificationError(message) {
        document.getElementById('verification-result').innerHTML = `
            <div class="error-message">
                <h3>❌ Verification Failed</h3>
                <p>${message}</p>
            </div>
        `;
    }
}

// Initialize admin verification system
const adminVerification = new TicketVerificationAdmin(
    localStorage.getItem('adminToken')
);
```

### QR Code Scanner Integration
```javascript
// QR Code scanner for admin verification
import QrScanner from 'qr-scanner';

class QRVerificationScanner {
    constructor(videoElement, verificationCallback) {
        this.scanner = new QrScanner(
            videoElement,
            result => this.handleScanResult(result),
            {
                returnDetailedScanResult: true,
                highlightScanRegion: true
            }
        );
        this.verificationCallback = verificationCallback;
    }
    
    async startScanning() {
        try {
            await this.scanner.start();
            console.log('QR Scanner started');
        } catch (error) {
            console.error('Failed to start scanner:', error);
        }
    }
    
    stopScanning() {
        this.scanner.stop();
        console.log('QR Scanner stopped');
    }
    
    handleScanResult(result) {
        const qrCode = result.data;
        
        // Validate QR code format (should contain :)
        if (!qrCode.includes(':')) {
            alert('Invalid QR code format');
            return;
        }
        
        // Stop scanning temporarily to prevent multiple scans
        this.scanner.stop();
        
        // Call verification callback
        this.verificationCallback(qrCode).finally(() => {
            // Resume scanning after verification attempt
            setTimeout(() => this.scanner.start(), 2000);
        });
    }
}

// Usage example
const videoElement = document.getElementById('scanner-video');
const scanner = new QRVerificationScanner(
    videoElement,
    qrCode => adminVerification.verifyTicket(qrCode)
);
```

## 🚀 Deployment Guide

### 1. Database Migration
```sql
-- Execute the migration script
-- File: database_migrations/add_ticket_verification_columns.sql

-- Add verification columns
ALTER TABLE bookings ADD COLUMN verification_token VARCHAR(255);
ALTER TABLE bookings ADD COLUMN random_string VARCHAR(100);
ALTER TABLE bookings ADD COLUMN qr_code VARCHAR(355);
ALTER TABLE bookings ADD COLUMN is_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE bookings ADD COLUMN verification_time TIMESTAMP NULL;
ALTER TABLE bookings ADD COLUMN verified_by VARCHAR(255);

-- Create performance indexes
CREATE INDEX idx_bookings_qr_code ON bookings(qr_code);
CREATE INDEX idx_bookings_verification_status ON bookings(is_verified);
CREATE INDEX idx_bookings_verification_token ON bookings(verification_token);
CREATE INDEX idx_bookings_cinema_date ON bookings(cinema_name, show_date);

-- Verify migration
SELECT COUNT(*) FROM bookings WHERE verification_token IS NOT NULL;
```

### 2. Backend Deployment
```bash
# Build the application
cd CineBook
./mvnw clean package -DskipTests

# Deploy the JAR file
java -jar target/CineBook.jar --spring.profiles.active=production

# Verify deployment
curl -H "Authorization: Bearer <admin_token>" \
     http://localhost:8080/api/bookings/admin/verification-summary
```

### 3. Frontend Dependencies
```bash
# Install required npm packages
npm install qrcode qr-scanner

# For vanilla JavaScript projects, include via CDN:
# <script src="https://cdn.jsdelivr.net/npm/qrcode@1.5.3/build/qrcode.min.js"></script>
# <script src="https://cdn.jsdelivr.net/npm/qr-scanner@1.4.2/qr-scanner.umd.min.js"></script>
```

### 4. Environment Configuration
```properties
# application.properties
# Ensure JWT configuration is properly set
jwt.secret=your-secret-key
jwt.expiration=86400000

# Database configuration
spring.datasource.url=jdbc:mysql://localhost:3306/movieDekho
spring.datasource.username=your-username
spring.datasource.password=your-password

# Logging configuration for verification tracking
logging.level.com.movieDekho.service.TicketVerificationService=INFO
```

## 🧪 Testing

### Unit Tests
```java
@Test
public void testQRCodeGeneration() {
    String token = ticketVerificationService.generateVerificationToken();
    String randomString = ticketVerificationService.generateRandomString();
    String qrCode = ticketVerificationService.generateQRCode(token, randomString);
    
    assertTrue(qrCode.contains(":"));
    assertEquals(token + ":" + randomString, qrCode);
}

@Test
public void testTicketVerification() {
    // Create test booking with QR code
    Booking booking = createTestBooking();
    booking.setQrCode("testToken:testRand");
    
    // Verify ticket
    VerifyTicketResponse response = ticketVerificationService.verifyTicket(
        new VerifyTicketRequest("testToken:testRand"), 
        "admin@test.com"
    );
    
    assertTrue(response.isValid());
    assertEquals("Ticket verified successfully", response.getMessage());
}
```

### Integration Tests
```java
@Test
@WithMockUser(roles = "ADMIN")
public void testVerifyTicketEndpoint() throws Exception {
    mockMvc.perform(post("/api/bookings/admin/verify-ticket")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"qrCode\":\"validQRCode:randomString\"}")
            .header("Authorization", "Bearer " + adminToken))
            .andExpected(status().isOk())
            .andExpected(jsonPath("$.valid").value(true))
            .andExpected(jsonPath("$.message").value("Ticket verified successfully"));
}
```

### Manual Testing Checklist
- [ ] QR code generation for new confirmed bookings
- [ ] User can view ticket with QR code
- [ ] Admin can verify valid QR codes
- [ ] Invalid QR codes are rejected
- [ ] Already verified tickets show previous verification
- [ ] Access control prevents unauthorized access
- [ ] Verification statistics are accurate

## 📊 Monitoring and Analytics

### Performance Metrics
- QR code generation time
- Verification API response time
- Database query performance
- Concurrent verification handling

### Business Metrics
- Daily verification rate
- Peak verification times
- Cinema-wise verification statistics
- Admin verification activity

### Monitoring Queries
```sql
-- Daily verification statistics
SELECT 
    DATE(verification_time) as verification_date,
    COUNT(*) as verified_count,
    COUNT(DISTINCT verified_by) as admin_count
FROM bookings 
WHERE is_verified = true 
GROUP BY DATE(verification_time)
ORDER BY verification_date DESC;

-- Cinema verification rates
SELECT 
    cinema_name,
    COUNT(*) as total_bookings,
    SUM(CASE WHEN is_verified THEN 1 ELSE 0 END) as verified_bookings,
    ROUND(100.0 * SUM(CASE WHEN is_verified THEN 1 ELSE 0 END) / COUNT(*), 2) as verification_rate
FROM bookings 
WHERE booking_status = 'CONFIRMED'
GROUP BY cinema_name;

-- Real-time verification status
SELECT 
    COUNT(CASE WHEN is_verified = false THEN 1 END) as pending_verifications,
    COUNT(CASE WHEN is_verified = true THEN 1 END) as completed_verifications,
    COUNT(*) as total_bookings
FROM bookings 
WHERE show_date = CURDATE() AND booking_status = 'CONFIRMED';
```

## 🔧 Troubleshooting

### Common Issues

#### QR Code Not Generated
**Problem**: New bookings don't have QR codes
**Solution**: 
1. Check if booking status is CONFIRMED
2. Verify TicketVerificationService is properly injected
3. Check database permissions for new columns

#### Verification Fails
**Problem**: Valid QR codes are rejected
**Solution**:
1. Verify QR code format includes ":"
2. Check if booking exists and is confirmed
3. Verify admin authentication token

#### Performance Issues
**Problem**: Slow verification response
**Solution**:
1. Check database indexes are created
2. Monitor database connection pool
3. Consider QR code caching for frequently accessed tickets

### Error Codes Reference
| Error Code | Description | Resolution |
|------------|-------------|------------|
| `INVALID_QR_CODE` | QR code format is invalid | Ensure QR contains ":" separator |
| `BOOKING_NOT_FOUND` | Booking doesn't exist | Verify booking ID exists in database |
| `BOOKING_NOT_CONFIRMED` | Booking not confirmed | Only confirmed bookings can be verified |
| `ALREADY_VERIFIED` | Ticket already verified | Show previous verification details |
| `UNAUTHORIZED_ACCESS` | Invalid or missing token | Verify JWT token is valid and has correct role |

## 🔮 Future Enhancements

### Planned Features
1. **QR Code Expiration**: Time-based QR code expiration for enhanced security
2. **Offline Verification**: Support for offline verification scenarios
3. **Bulk Operations**: Admin tools for bulk ticket verification
4. **Advanced Analytics**: Detailed verification reports and dashboards
5. **Mobile App Integration**: Native mobile QR scanner
6. **Real-time Notifications**: Push notifications for verification events

### Integration Opportunities
- **SMS Integration**: Send QR codes via SMS
- **Email Templates**: Enhanced email tickets with QR codes
- **Third-party Scanners**: Integration with external QR scanning devices
- **API Webhooks**: Real-time verification event notifications

---

## 📞 Support

For technical support or questions about the ticket verification system:
- **Documentation Issues**: Create an issue in the project repository
- **Bug Reports**: Include steps to reproduce and system details
- **Feature Requests**: Describe the use case and expected behavior

**System Status**: ✅ Production Ready  
**Last Updated**: August 16, 2025  
**Version**: 1.0.0
