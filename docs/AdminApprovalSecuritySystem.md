# Admin Approval Security System - MovieDekho

## 🔒 Enhanced Security Overview

The MovieDekho application now includes a robust admin approval system that ensures only authorized administrators can access admin functionalities. This system prevents unauthorized admin registration and provides comprehensive approval workflow management.

## 🚀 Key Features

### ✅ Implemented Security Enhancements:

1. **Admin Registration Approval System**
   - New admin registrations go to "PENDING_ADMIN" status
   - Super admin email notification for every admin registration attempt
   - Manual approval/rejection process with email notifications

2. **Super Admin Panel**
   - Dedicated super admin dashboard and management endpoints
   - View pending, approved, and rejected admin requests
   - Approve or reject admin applications with optional reason

3. **Enhanced Security Configuration**
   - Pending admins cannot access any admin functionalities
   - Rejected admins are permanently blocked
   - Only approved admins can access admin endpoints

4. **Email Notification System**
   - Instant notification to super admin on admin registration attempts
   - Approval/rejection notifications to applicants
   - Professional email templates with security alerts

## 📧 Email Configuration

The system uses your existing Brevo email service configuration:
- **Super Admin Email**: `moni618919@gmail.com` (configured in application.properties)
- **Sender Email**: `moni618919@gmail.com`
- **Sender Name**: Movie Dekho

## 🛡️ Security Workflow

### 1. Admin Registration Process

```bash
# When someone attempts to register as admin
curl -X POST http://localhost:8080/api/auth/register-admin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "potential_admin",
    "password": "securepass123",
    "email": "admin@example.com",
    "phone": "1234567890",
    "gender": "Male"
  }'
```

**What happens:**
1. User account created with `PENDING_ADMIN` status
2. `isApproved` set to `false`
3. `requestedAt` timestamp recorded
4. **Email sent to you (super admin)** with registration details
5. Applicant receives response: "Admin registration request submitted. Please wait for approval from the super admin."

### 2. Super Admin Notification Email

You'll receive an email like this:

```
Subject: New Admin Registration Request - MovieDekho

🚨 Admin Registration Alert

New Admin Registration Request
Someone has registered as an admin and requires your approval.

Registration Details:
- User ID: 123
- Username: potential_admin
- Email: admin@example.com
- Phone: 1234567890
- Request Time: 2025-07-29T22:30:15

Action Required:
• Use the admin approval endpoint: /api/super-admin/approve-admin
• Send POST request with userId: 123
• Use action: "APPROVE" or "REJECT"

Security Notice:
This user will NOT have admin access until you manually approve their request.
```

### 3. Super Admin Management

#### View Pending Requests
```bash
curl -X GET http://localhost:8080/api/super-admin/pending-admins \
  -H "Authorization: Bearer YOUR_SUPER_ADMIN_JWT_TOKEN"
```

#### View Dashboard
```bash
curl -X GET http://localhost:8080/api/super-admin/dashboard \
  -H "Authorization: Bearer YOUR_SUPER_ADMIN_JWT_TOKEN"
```

#### Approve Admin
```bash
curl -X POST http://localhost:8080/api/super-admin/approve-admin \
  -H "Authorization: Bearer YOUR_SUPER_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 123,
    "action": "APPROVE"
  }'
```

#### Reject Admin
```bash
curl -X POST http://localhost:8080/api/super-admin/approve-admin \
  -H "Authorization: Bearer YOUR_SUPER_ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 123,
    "action": "REJECT",
    "reason": "Suspicious registration pattern"
  }'
```

## 🔐 Authentication Requirements

### Super Admin Access
- Must be authenticated (have valid JWT token)
- Token must be for the super admin email: `moni618919@gmail.com`
- Email verification is done in the controller layer

### Regular Admin Access
- Must have `ROLE_ADMIN` role
- Must have `isApproved = true`
- Pending or rejected admins are automatically blocked

### User Access
- Regular users (`ROLE_USER`) are unaffected
- Continue to work normally with immediate registration

## 📊 User Status Types

1. **ROLE_USER** - Regular users (immediate approval)
2. **PENDING_ADMIN** - Admin registration pending approval
3. **ROLE_ADMIN** - Approved admin (full access)
4. **REJECTED_ADMIN** - Rejected admin application

## 🚫 Security Blocks

### What Pending Admins Cannot Do:
- Access any `/api/admin/**` endpoints
- Access any admin functionalities
- Login successfully (authentication will fail)
- Escalate privileges

### What Rejected Admins Cannot Do:
- Everything that pending admins cannot do
- Re-apply for admin status (without manual intervention)

## 🎯 API Endpoints Summary

### Super Admin Endpoints (`/api/super-admin/`)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/pending-admins` | Get all pending admin requests |
| GET | `/all-admin-requests` | Get all admin requests (pending, approved, rejected) |
| GET | `/dashboard` | Get super admin dashboard with statistics |
| POST | `/approve-admin` | Approve or reject admin registration |

### Modified Endpoints
| Endpoint | Change |
|----------|---------|
| `POST /api/auth/register-admin` | Now creates pending admin + sends notification |
| All admin endpoints | Now require approved admin status |

## 🔧 Database Changes

New fields added to `User` model:
- `isApproved` (Boolean) - Approval status
- `approvedBy` (String) - Who approved the admin
- `requestedAt` (LocalDateTime) - When admin registration was requested
- `approvedAt` (LocalDateTime) - When admin was approved

## 🚀 Getting Started

### 1. Super Admin Setup
Your email `moni618919@gmail.com` is configured as the super admin. Simply:
1. Register as a regular user with your email
2. Manually change your role to `ROLE_ADMIN` in the database (one-time setup)
3. Set `isApproved = true` for your account

### 2. Testing the System

```bash
# 1. Have someone register as admin
curl -X POST http://localhost:8080/api/auth/register-admin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_admin",
    "password": "testpass123",
    "email": "test@example.com",
    "phone": "9876543210",
    "gender": "Male"
  }'

# 2. Check your email for notification

# 3. Login as super admin and view pending requests
curl -X GET http://localhost:8080/api/super-admin/pending-admins \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# 4. Approve the admin
curl -X POST http://localhost:8080/api/super-admin/approve-admin \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": [USER_ID_FROM_STEP_3],
    "action": "APPROVE"
  }'
```

## 📧 Email Templates

### Admin Registration Notification (to Super Admin)
- 🚨 Security alert styling
- Complete registration details
- Action required instructions
- Security reminder about pending status

### Approval Notification (to Applicant)
- ✅ Welcome message
- Admin panel access instructions
- Responsibility reminder

### Rejection Notification (to Applicant)
- ❌ Rejection notice
- Reason for rejection (if provided)
- Contact information for appeals

## 🔍 Monitoring & Security

### Security Logs
- All admin registration attempts are logged
- Approval/rejection actions are logged with super admin email
- Failed super admin access attempts are logged

### Recommended Monitoring
1. Monitor email notifications for suspicious registration patterns
2. Regularly review pending admin requests
3. Keep track of approved admins and their activities
4. Set up alerts for multiple failed super admin access attempts

## ⚠️ Important Security Notes

1. **Super Admin Protection**: Only your email can approve admins
2. **No Backdoors**: No way to bypass the approval process
3. **Pending Admins Blocked**: Pending admins have zero admin access
4. **Email Notifications**: You'll be notified of every admin registration attempt
5. **Audit Trail**: Complete record of who approved/rejected each admin

## 🎉 Benefits

1. **Enhanced Security**: No unauthorized admin access possible
2. **Real-time Alerts**: Immediate notification of admin registration attempts
3. **Complete Control**: You approve every single admin manually
4. **Professional Communication**: Automated professional email notifications
5. **Audit Trail**: Complete history of admin approvals/rejections
6. **Zero Downtime**: Regular users unaffected by changes

Your MovieDekho application is now significantly more secure! 🛡️
