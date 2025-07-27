# User and Admin Panel API Documentation

This document describes the User and Admin Panel API endpoints for the MovieDekho application.

## Authentication

All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## User Registration and Login

### Register User
- **POST** `/api/auth/register`
- **Body:**
```json
{
    "username": "john_doe",
    "password": "password123",
    "email": "john@example.com",
    "phone": "1234567890",
    "gender": "Male"
}
```

### Register Admin
- **POST** `/api/auth/register-admin`
- **Body:** Same as user registration
- **Note:** Creates a user with ROLE_ADMIN

### Login
- **POST** `/api/auth/login`
- **Body:**
```json
{
    "username": "john_doe",
    "password": "password123"
}
```

## User Panel Endpoints

All user endpoints require `ROLE_USER` and are prefixed with `/api/user`

### User Dashboard
- **GET** `/api/user/dashboard`
- **Headers:** Authorization: Bearer <token>
- **Response:**
```json
{
    "user": {
        "id": 1,
        "username": "john_doe",
        "email": "john@example.com",
        "phone": "1234567890",
        "gender": "Male",
        "role": "ROLE_USER"
    },
    "recentMovies": [....]
}
```

### Get User Profile
- **GET** `/api/user/profile`
- **Headers:** Authorization: Bearer <token>

### Update Profile
- **PUT** `/api/user/profile`
- **Headers:** Authorization: Bearer <token>
- **Body:**
```json
{
    "email": "newemail@example.com",
    "phone": "9876543210",
    "gender": "Female"
}
```

### Change Password
- **PUT** `/api/user/change-password`
- **Headers:** Authorization: Bearer <token>
- **Body:**
```json
{
    "currentPassword": "oldpassword",
    "newPassword": "newpassword123"
}
```

### Get Favorites (Coming Soon)
- **GET** `/api/user/favorites`
- **Headers:** Authorization: Bearer <token>

### Get Booking History (Coming Soon)
- **GET** `/api/user/bookings`
- **Headers:** Authorization: Bearer <token>

## Admin Panel Endpoints

All admin endpoints require `ROLE_ADMIN` and are prefixed with `/api/admin`

### Admin Dashboard Statistics
- **GET** `/api/admin/dashboard/stats`
- **Headers:** Authorization: Bearer <token>
- **Response:**
```json
{
    "totalUsers": 150,
    "totalMovies": 25,
    "totalBookings": 500,
    "totalRevenue": 15000.50
}
```

### Movie Management

#### Create Movie
- **POST** `/api/admin/movies`
- **Headers:** Authorization: Bearer <token>
- **Body:** MovieCreateRequest object

#### Update Movie
- **PUT** `/api/admin/movies/{movieId}`
- **Headers:** Authorization: Bearer <token>
- **Body:** MovieUpdateRequest object

#### Delete Movie
- **DELETE** `/api/admin/movies/{movieId}`
- **Headers:** Authorization: Bearer <token>

#### Get All Movies (Admin View)
- **GET** `/api/admin/movies?page=0&size=10`
- **Headers:** Authorization: Bearer <token>

### User Management

#### Get All Users
- **GET** `/api/admin/users?page=0&size=10`
- **Headers:** Authorization: Bearer <token>
- **Response:**
```json
{
    "users": [...],
    "totalElements": 150,
    "totalPages": 15,
    "currentPage": 0
}
```

#### Get User by ID
- **GET** `/api/admin/users/{userId}`
- **Headers:** Authorization: Bearer <token>

#### Update User Role
- **PUT** `/api/admin/users/{userId}/role?role=ROLE_ADMIN`
- **Headers:** Authorization: Bearer <token>

#### Delete User
- **DELETE** `/api/admin/users/{userId}`
- **Headers:** Authorization: Bearer <token>

#### Search Users
- **GET** `/api/admin/users/search?query=john`
- **Headers:** Authorization: Bearer <token>

## Security Configuration

The application now uses role-based access control:

### Public Endpoints:
- `/api/auth/**` - Authentication endpoints
- `/movies/**` - Public movie browsing

### User Endpoints:
- `/api/user/**` - User panel (requires ROLE_USER)
- `/api/seats/slot/**` - Seat viewing (USER or ADMIN)
- `/api/seats/{seatId}` - Seat details (USER or ADMIN)
- `/api/seats/{seatId}/availability` - Seat availability (USER or ADMIN)

### Admin Endpoints:
- `/api/admin/**` - Admin panel (requires ROLE_ADMIN)
- `/api/seats/admin/**` - Seat management (ADMIN only)
- `/api/movie-slots/admin/**` - Movie slot management (ADMIN only)

## Error Responses

All endpoints return appropriate HTTP status codes:
- `200` - Success
- `201` - Created (for resource creation)
- `400` - Bad Request (validation errors)
- `401` - Unauthorized (invalid/missing token)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found
- `409` - Conflict (duplicate data)
- `500` - Internal Server Error

## Usage Examples

### User Workflow:
1. Register as user: `POST /api/auth/register`
2. Login: `POST /api/auth/login`
3. Access dashboard: `GET /api/user/dashboard`
4. Update profile: `PUT /api/user/profile`
5. Browse movies and book seats

### Admin Workflow:
1. Register as admin: `POST /api/auth/register-admin`
2. Login: `POST /api/auth/login`
3. Access admin dashboard: `GET /api/admin/dashboard/stats`
4. Manage movies: `POST/PUT/DELETE /api/admin/movies/*`
5. Manage users: `GET/PUT/DELETE /api/admin/users/*`
6. Manage seats and slots through existing admin endpoints

## Notes

- All passwords are encrypted using BCrypt
- JWT tokens include user role information
- Role-based access is enforced at both method and URL levels
- User roles are: `ROLE_USER` and `ROLE_ADMIN`
- Admin users have access to both admin and user functionalities
