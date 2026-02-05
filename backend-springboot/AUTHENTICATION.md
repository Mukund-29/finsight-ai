# Authentication System Documentation

## Overview
The Finsight Request Management System includes a user authentication and management system with two user roles.

## User Roles

### 1. ADMIN
- **Created by**: ADMIN users only
- **Permissions**:
  - Can manage users
  - Can delete users
  - Can create admin users
  - Can update user roles
  - Can view all requests

### 2. USER (Basic User)
- **Created by**: Self-registration when raising ticket
- **Permissions**:
  - Can create requests
  - Can view own requests
  - Cannot assign requests
  - Cannot delete users

## API Endpoints

### 1. User Registration
**POST** `/api/auth/register`

**Request Body:**
```json
{
  "ntid": "user123",
  "email": "user@example.com",
  "account": "Account Name",
  "accountId": 12345
}
```

**Response:**
```json
{
  "message": "User registered successfully",
  "ntid": "user123",
  "email": "user@example.com",
  "role": "USER"
}
```

### 2. User Login
**POST** `/api/auth/login`

**Request Body:**
```json
{
  "ntid": "user123",
  "password": "user123"
}
```

**Response:**
```json
{
  "ntid": "user123",
  "email": "user@example.com",
  "role": "USER",
  "token": "token_user123_USER_1234567890",
  "message": "Authentication successful"
}
```

### 3. Get Current User
**GET** `/api/auth/me`

**Headers:**
```
X-User-NTID: user123
```

**Response:**
```json
{
  "ntid": "user123",
  "email": "user@example.com",
  "role": "USER",
  "account": "Account Name",
  "accountId": 12345
}
```

### 4. Create Admin User (ADMIN only)
**POST** `/api/users/admin`

**Headers:**
```
X-User-NTID: admin1
```

**Request Body:**
```json
{
  "ntid": "admin2",
  "email": "admin2@example.com",
  "account": "Account Name",
  "accountId": 12345
}
```

**Response:**
```json
{
  "message": "Admin user created successfully",
  "ntid": "admin2",
  "email": "admin2@example.com",
  "role": "ADMIN"
}
```

### 5. Delete User
**DELETE** `/api/users/{ntid}`

**Headers:**
```
X-User-NTID: admin1
```

**Response:**
```json
{
  "message": "User deleted successfully"
}
```

**Note**: 
- Only ADMIN can delete users

### 6. Update User Role
**PUT** `/api/users/{ntid}/role?role=ADMIN`

**Headers:**
```
X-User-NTID: admin1
```

**Response:**
```json
{
  "message": "User role updated successfully",
  "ntid": "user123",
  "role": "ADMIN"
}
```

**Note**: Only ADMIN can update user roles

### 7. Get User by NTID
**GET** `/api/users/{ntid}`

**Response:**
```json
{
  "ntid": "user123",
  "email": "user@example.com",
  "role": "USER",
  "account": "Account Name",
  "accountId": 12345,
  "active": true
}
```

## User Registration Flow

1. User wants to raise a ticket
2. User provides:
   - NTID (unique, primary key)
   - Email ID
   - Account
   - Account ID (numerical)
3. System creates user with USER role automatically
4. User can now login and create requests

## Security Notes

1. All authentication is currently header-based (X-User-NTID)
2. For production, implement JWT tokens or session management
3. Password hashing should be implemented for regular users
4. Currently, NTID is used as password for authentication (can be enhanced later)

## Testing

### Test User Registration:
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "ntid":"testuser",
    "email":"test@example.com",
    "account":"Test Account",
    "accountId":12345
  }'
```

### Test User Login:
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "ntid":"testuser",
    "password":"testuser"
  }'
```

### Test Create Admin (as admin):
```bash
curl -X POST http://localhost:8081/api/users/admin \
  -H "Content-Type: application/json" \
  -H "X-User-NTID: admin1" \
  -d '{
    "ntid":"admin2",
    "email":"admin2@example.com",
    "account":"Admin Account",
    "accountId":12345
  }'
```

### Test Delete User (as admin):
```bash
curl -X DELETE http://localhost:8081/api/users/testuser \
  -H "X-User-NTID: admin1"
```

### Test Update User Role (as admin):
```bash
curl -X PUT "http://localhost:8081/api/users/testuser/role?role=ADMIN" \
  -H "X-User-NTID: admin1"
```
