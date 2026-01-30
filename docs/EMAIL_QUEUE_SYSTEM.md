# Email Queue System - CineBook

## Overview

The CineBook application now features a robust, DB-backed email queue system that provides resilient email delivery with automatic retry mechanisms. This system prevents registration failures due to email service limits and ensures that important emails are eventually delivered.

## Features

### ✅ **Resilient Email Delivery**
- **DB-backed queue**: Emails are stored in database before sending
- **Automatic retries**: Failed emails are automatically retried with exponential backoff
- **Priority-based processing**: Critical emails (OTP, password reset) get higher priority
- **Daily email limits**: Configurable daily sending limits to prevent service exhaustion
- **Graceful fallback**: Immediate sending with queue fallback for failures

### ✅ **Smart Retry Logic**
- **Exponential backoff**: Retry delays increase exponentially (1h, 2h, 4h, max 24h)
- **Error-based retry decisions**: Different retry behavior based on error types
- **Maximum retry attempts**: Configurable retry limits (default: 3 attempts)
- **Permanent failure handling**: Emails that can't be retried are marked as permanently failed

### ✅ **Email Types & Priorities**
| Email Type | Priority | Behavior |
|------------|----------|----------|
| OTP | 10 (Highest) | Immediate sending, critical for user flow |
| Password Reset | 8 | High priority, time-sensitive |
| Admin Notifications | 6 | Medium priority |
| Welcome Emails | 5 | Medium priority, enhances UX |
| General | 1 (Lowest) | Standard priority |

### ✅ **Monitoring & Management**
- **Real-time statistics**: Track pending, sent, failed email counts
- **Health monitoring**: System health checks with recommendations
- **Manual controls**: Reset stuck emails, trigger cleanup, view stats
- **Detailed logging**: Comprehensive logging for troubleshooting

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   User Action   │ ─▶ │ ResilientEmail  │ ─▶ │   Email Queue   │
│  (Registration) │    │     Service     │    │   (Database)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │                         │
                              ▼                         ▼
                    ┌─────────────────┐    ┌─────────────────┐
                    │ Immediate Send  │    │  Queue Processor│
                    │   (Try First)   │    │   (Scheduled)   │
                    └─────────────────┘    └─────────────────┘
                              │                         │
                              ▼                         ▼
                    ┌─────────────────┐    ┌─────────────────┐
                    │ Brevo Email API │ ◀─ │  Retry Logic    │
                    │   (External)    │    │ (Exponential)   │
                    └─────────────────┘    └─────────────────┘
```

## Configuration

### Application Properties

```properties
# Email Queue Configuration
app.email.queue.enabled=true
app.email.queue.batch-size=10
app.email.queue.processing-interval=30000
app.email.queue.max-retries=3
app.email.queue.base-retry-delay-minutes=60
app.email.queue.exponential-backoff=true
app.email.queue.max-retry-delay-minutes=1440
app.email.queue.daily-email-limit=300
app.email.queue.pause-on-daily-limit=true

# Email Priorities
app.email.queue.otp-email-priority=10
app.email.queue.password-reset-email-priority=8
app.email.queue.admin-notification-priority=6
app.email.queue.welcome-email-priority=5
app.email.queue.general-email-priority=1
```

## Database Schema

### EmailQueue Table

```sql
CREATE TABLE email_queue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    html_content TEXT NOT NULL,
    email_type ENUM('WELCOME', 'OTP', 'PASSWORD_RESET', 'ADMIN_REGISTRATION_NOTIFICATION', 
                   'ADMIN_APPROVAL_NOTIFICATION', 'BOOKING_CONFIRMATION', 'GENERAL') NOT NULL,
    status ENUM('PENDING', 'PROCESSING', 'SENT', 'FAILED', 'CANCELLED', 'SCHEDULED') NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    max_retries INT NOT NULL DEFAULT 3,
    priority INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL,
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP,
    last_attempt_at TIMESTAMP,
    error_message TEXT,
    related_entity_id BIGINT,
    INDEX idx_status_scheduled (status, scheduled_at),
    INDEX idx_priority_created (priority DESC, created_at ASC)
);
```

## API Endpoints

### Admin Monitoring Endpoints

```bash
# Get queue statistics
GET /api/admin/email-queue/stats

# Get health status
GET /api/admin/email-queue/health

# Manual cleanup
POST /api/admin/email-queue/cleanup

# Reset stuck emails
POST /api/admin/email-queue/reset-stuck

# Test email queue
POST /api/admin/test-email/test-queue?recipientEmail=test@example.com

# Test immediate email
POST /api/admin/test-email/test-immediate?recipientEmail=test@example.com
```

## Usage Examples

### 1. Send Welcome Email (with Resilience)

```java
@Autowired
private ResilientEmailService resilientEmailService;

// Automatically handles immediate sending with queue fallback
resilientEmailService.sendWelcomeEmail(user);
```

### 2. Send OTP Email (High Priority)

```java
// OTP emails are sent immediately due to time sensitivity
resilientEmailService.sendOtpEmail(email, otpCode);
```

### 3. Schedule Email for Later

```java
// Schedule an email for future sending
resilientEmailService.scheduleEmail(
    recipientEmail, 
    subject, 
    content, 
    EmailQueue.EmailType.GENERAL,
    LocalDateTime.now().plusHours(1), // Send in 1 hour
    relatedEntityId
);
```

### 4. Get Queue Statistics

```java
@Autowired
private EmailQueueService emailQueueService;

EmailQueueService.EmailQueueStats stats = emailQueueService.getQueueStats();
System.out.println("Pending: " + stats.getPendingCount());
System.out.println("Sent today: " + stats.getSentTodayCount());
```

## Error Handling

### Retry-able Errors
- **402**: Payment/limit issues (Brevo daily limit exceeded)
- **429**: Rate limiting
- **5xx**: Server errors
- **Timeout/Connection errors**

### Non-Retry-able Errors
- **401**: Authentication errors (invalid API key)
- **403**: Forbidden
- **Invalid email addresses**
- **Blacklisted/Unsubscribed emails**

## Monitoring

### Key Metrics to Monitor

1. **Pending Count**: Should be low under normal operation
2. **Failed Count**: High numbers indicate configuration issues
3. **Processing Count**: Many stuck emails indicate processing issues
4. **Sent Today Count**: Track against daily limits
5. **Retry Count**: Monitor retry frequency

### Health Status Levels

- **HEALTHY**: Normal operation
- **WARNING**: Minor issues (high pending, many processing)
- **CRITICAL**: Major issues (high failure rate)

### Logs to Monitor

```
# Successful processing
INFO: Email sent successfully - ID: 123, Type: WELCOME, Recipient: user@example.com

# Retry scenarios  
WARN: Email failed but will retry - ID: 123, Attempt: 1/3, Next retry: 2025-01-15T14:30:00

# Permanent failures
ERROR: Email permanently failed - ID: 123, Type: WELCOME, Recipient: user@example.com

# Daily limit reached
WARN: Daily email limit reached (300/300). Pausing email processing.
```

## Benefits

### 🚀 **Improved User Experience**
- **Faster registrations**: No waiting for email confirmation
- **No registration failures**: Users can complete registration even if email fails
- **Automatic email recovery**: Failed emails are automatically retried

### 🛡️ **System Resilience**
- **Service limit protection**: Respects Brevo daily limits
- **Graceful degradation**: System continues working even with email issues
- **Transaction safety**: Database transactions are not affected by email failures

### 📊 **Better Monitoring**
- **Real-time visibility**: Know exactly what's happening with emails
- **Proactive issue detection**: Get warnings before problems become critical
- **Historical tracking**: Track email patterns and performance over time

### 🔧 **Operational Benefits**
- **Easy troubleshooting**: Detailed logs and statistics
- **Manual recovery**: Reset stuck emails, trigger retries
- **Configuration flexibility**: Adjust retry behavior, priorities, limits

## Migration Notes

### For Existing Code

The old email sending methods still work but are now deprecated:

```java
// OLD (still works, but deprecated)
userService.sendWelcomeEmail(user);

// NEW (recommended)
resilientEmailService.sendWelcomeEmail(user);
```

### Database Migration

The application will automatically create the `email_queue` table on startup. No manual migration needed.

### Configuration Migration

Add the new email queue properties to your `application.properties`. The system will use sensible defaults if not specified.

## Troubleshooting

### Common Issues

1. **High Failed Count**
   - Check Brevo API key validity
   - Verify sender email is verified in Brevo
   - Check daily limits in Brevo account

2. **Emails Stuck in Processing**
   - Use reset stuck emails endpoint
   - Check for application crashes during email sending

3. **Daily Limit Reached**
   - Increase daily limit in configuration
   - Upgrade Brevo plan for higher limits
   - Implement email prioritization

4. **Slow Email Delivery**
   - Decrease processing interval
   - Increase batch size
   - Check database performance

### Support Commands

```bash
# Check queue status
curl -X GET /api/admin/email-queue/stats

# Reset stuck emails
curl -X POST /api/admin/email-queue/reset-stuck

# Force cleanup
curl -X POST /api/admin/email-queue/cleanup
```

---

## Conclusion

This email queue system transforms CineBook from a fragile email-dependent application to a resilient system that gracefully handles email service limitations. Users get faster registrations, administrators get better monitoring, and the system becomes more reliable overall.

The queue-based approach ensures that temporary email service issues don't break critical user flows like registration, while the retry mechanism ensures that important communications eventually reach users.