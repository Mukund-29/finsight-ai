# Postman Collection - Finsight Request Management API

## Base URL
```
http://localhost:8081
```

---

## 1. User Registration

**POST** `/api/auth/register`

**Headers:**
```
Content-Type: application/json
```

**IMPORTANT:** Make sure Content-Type is set to `application/json` in Postman, not `text/plain`

**Request Body:**
```json
{
  "ntid": "testuser",
  "email": "test@example.com",
  "account": "Test Account",
  "accountId": 12345
}
```

**Expected Response (201 Created):**
```json
{
  "message": "User registered successfully",
  "ntid": "testuser",
  "email": "test@example.com",
  "role": "USER"
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "User with this NTID already exists"
}
```

---

## 2. User Login

**POST** `/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "ntid": "testuser",
  "password": "testuser"
}
```

**Expected Response (200 OK):**
```json
{
  "ntid": "testuser",
  "email": "test@example.com",
  "role": "USER",
  "token": "token_testuser_USER_1234567890",
  "message": "Authentication successful"
}
```

**Error Response (401 Unauthorized):**
```json
{
  "error": "User not found"
}
```

---

## 4. Get Current User Info

**GET** `/api/auth/me`

**Headers:**
```
X-User-NTID: testuser
```

**Expected Response (200 OK):**
```json
{
  "ntid": "testuser",
  "email": "test@example.com",
  "role": "USER",
  "account": "Test Account",
  "accountId": 12345
}
```

**Error Response (401 Unauthorized):**
```json
{
  "error": "NTID not provided"
}
```

---

## 5. Create Admin User (ADMIN Only)

**POST** `/api/users/admin`

**Headers:**
```
Content-Type: application/json
X-User-NTID: admin1
```

**Request Body:**
```json
{
  "ntid": "admin1",
  "email": "admin@example.com",
  "account": "Admin Account",
  "accountId": 12345
}
```

**Expected Response (201 Created):**
```json
{
  "message": "Admin user created successfully",
  "ntid": "admin1",
  "email": "admin@example.com",
  "role": "ADMIN"
}
```

**Error Response (403 Forbidden):**
```json
{
  "error": "Only ADMIN can create admin users"
}
```

---

## 6. Get User by NTID

**GET** `/api/users/{ntid}`

**Example:** `/api/users/testuser`

**Headers:**
```
(Optional) X-User-NTID: admin1
```

**Expected Response (200 OK):**
```json
{
  "ntid": "testuser",
  "email": "test@example.com",
  "role": "USER",
  "account": "Test Account",
  "accountId": 12345,
  "active": true
}
```

**Error Response (404 Not Found):**
```json
{
  "error": "User not found"
}
```

---

## 7. Update User Role (ADMIN Only)

**PUT** `/api/users/{ntid}/role?role=ADMIN`

**Example:** `/api/users/testuser/role?role=ADMIN`

**Headers:**
```
X-User-NTID: admin1
```

**Query Parameters:**
- `role`: ADMIN, USER

**Expected Response (200 OK):**
```json
{
  "message": "User role updated successfully",
  "ntid": "testuser",
  "role": "ADMIN"
}
```

**Error Response (403 Forbidden):**
```json
{
  "error": "Only ADMIN can update user roles"
}
```

---

## 8. Delete User

**DELETE** `/api/users/{ntid}`

**Example:** `/api/users/testuser`

**Headers:**
```
X-User-NTID: admin1
```

**Expected Response (200 OK):**
```json
{
  "message": "User deleted successfully"
}
```

**Error Response (403 Forbidden):**
```json
{
  "error": "Only ADMIN can delete users"
}
```

**Note:**
- Only ADMIN can delete users
- Regular users cannot delete anyone

---

## Postman Collection JSON

You can import this into Postman:

```json
{
  "info": {
    "name": "Finsight Request Management API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Auth",
      "item": [
        {
          "name": "Register User",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"ntid\": \"testuser\",\n  \"email\": \"test@example.com\",\n  \"account\": \"Test Account\",\n  \"accountId\": 12345\n}"
            },
            "url": {
              "raw": "http://localhost:8081/api/auth/register",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8081",
              "path": ["api", "auth", "register"]
            }
          }
        },
        {
          "name": "Login",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"ntid\": \"testuser\",\n  \"password\": \"testuser\"\n}"
            },
            "url": {
              "raw": "http://localhost:8081/api/auth/login",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8081",
              "path": ["api", "auth", "login"]
            }
          }
        },
        {
          "name": "Get Current User",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "X-User-NTID",
                "value": "admin1"
              }
            ],
            "url": {
              "raw": "http://localhost:8081/api/auth/me",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8081",
              "path": ["api", "auth", "me"]
            }
          }
        }
      ]
    },
    {
      "name": "Users",
      "item": [
        {
          "name": "Create Admin User",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "X-User-NTID",
                "value": "admin1"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"ntid\": \"admin1\",\n  \"email\": \"admin@example.com\",\n  \"account\": \"Admin Account\",\n  \"accountId\": 12345\n}"
            },
            "url": {
              "raw": "http://localhost:8081/api/users/admin",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8081",
              "path": ["api", "users", "admin"]
            }
          }
        },
        {
          "name": "Get User by NTID",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "X-User-NTID",
                "value": "admin1"
              }
            ],
            "url": {
              "raw": "http://localhost:8081/api/users/testuser",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8081",
              "path": ["api", "users", "testuser"]
            }
          }
        },
        {
          "name": "Update User Role",
          "request": {
            "method": "PUT",
            "header": [
              {
                "key": "X-User-NTID",
                "value": "admin1"
              }
            ],
            "url": {
              "raw": "http://localhost:8081/api/users/testuser/role?role=ADMIN",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8081",
              "path": ["api", "users", "testuser", "role"],
              "query": [
                {
                  "key": "role",
                  "value": "ADMIN"
                }
              ]
            }
          }
        },
        {
          "name": "Delete User",
          "request": {
            "method": "DELETE",
            "header": [
              {
                "key": "X-User-NTID",
                "value": "admin1"
              }
            ],
            "url": {
              "raw": "http://localhost:8081/api/users/testuser",
              "protocol": "http",
              "host": ["localhost"],
              "port": "8081",
              "path": ["api", "users", "testuser"]
            }
          }
        }
      ]
    }
  ]
}
```

---

## Quick Test Sequence

### Step 1: Register a User
```
POST http://localhost:8081/api/auth/register
Body: {"ntid":"testuser","email":"test@example.com","account":"Test","accountId":12345}
```

### Step 2: Login as User
```
POST http://localhost:8081/api/auth/login
Body: {"ntid":"testuser","password":"testuser"}
```

### Step 3: Create Admin User (as Admin)
```
POST http://localhost:8081/api/users/admin
Header: X-User-NTID: admin1
Body: {"ntid":"admin2","email":"admin2@example.com","account":"Admin","accountId":12345}
```

### Step 4: Get User Info
```
GET http://localhost:8081/api/users/testuser
Header: X-User-NTID: admin1
```

### Step 5: Delete User (as Admin)
```
DELETE http://localhost:8081/api/users/testuser
Header: X-User-NTID: admin1
```

---

## Notes

1. **Authentication:**
   - Currently using header-based: `X-User-NTID`
   - For production, implement JWT tokens

2. **Table Names:**
   - All tables prefixed with `FLOWAI_`
   - Example: `FLOWAI_USERS`

3. **Roles:**
   - `USER`: Basic user, can create/view own requests
   - `ADMIN`: Can manage users and requests
   - `SUPER_USER`: Reserved for future use

4. **Admin Creation:**
   - Only existing ADMIN users can create new admin users
   - First admin must be created manually in the database or through a setup script
