# User Account Management API Documentation

## Overview
API endpoints for managing account assignments to SCRUM_MASTER users. This allows SCRUM_MASTER to handle multiple accounts and see only tickets from those accounts.

**Author:** Mukund Kute

## Base URL
```
http://localhost:8081/api/user-accounts
```

## Authentication
All endpoints require `X-User-NTID` header with ADMIN user's NTID (only ADMIN can manage account assignments).

## API Endpoints

### 1. Assign Account to SCRUM_MASTER
**POST** `/api/user-accounts`

**Headers:**
```
X-User-NTID: admin1
Content-Type: application/json
```

**Request Body:**
```json
{
  "ntid": "scrummaster1",
  "accountId": 1
}
```

**Response (201 Created):**
```json
{
  "message": "Account assigned successfully",
  "userAccountId": 1,
  "ntid": "scrummaster1",
  "accountId": 1
}
```

**Permissions:** Only ADMIN can assign accounts
**Note:** Only SCRUM_MASTER role users can have account assignments

---

### 2. Get Accounts Handled by User
**GET** `/api/user-accounts/user/{ntid}`

**Example:**
```
GET /api/user-accounts/user/scrummaster1
```

**Response (200 OK):**
```json
[
  {
    "userAccountId": 1,
    "ntid": "scrummaster1",
    "accountId": 1,
    "createdAt": "2026-01-30T10:00:00"
  },
  {
    "userAccountId": 2,
    "ntid": "scrummaster1",
    "accountId": 2,
    "createdAt": "2026-01-30T10:00:00"
  }
]
```

---

### 3. Get Account IDs Handled by User (Simplified)
**GET** `/api/user-accounts/user/{ntid}/account-ids`

**Example:**
```
GET /api/user-accounts/user/scrummaster1/account-ids
```

**Response (200 OK):**
```json
{
  "ntid": "scrummaster1",
  "accountIds": [1, 2],
  "count": 2
}
```

---

### 4. Get Users Handling an Account
**GET** `/api/user-accounts/account/{accountId}`

**Example:**
```
GET /api/user-accounts/account/1
```

**Response (200 OK):**
```json
[
  {
    "userAccountId": 1,
    "ntid": "scrummaster1",
    "accountId": 1,
    "createdAt": "2026-01-30T10:00:00"
  },
  {
    "userAccountId": 3,
    "ntid": "scrummaster2",
    "accountId": 1,
    "createdAt": "2026-01-30T11:00:00"
  }
]
```

---

### 5. Remove Account Assignment
**DELETE** `/api/user-accounts/user/{ntid}/account/{accountId}`

**Headers:**
```
X-User-NTID: admin1
```

**Example:**
```
DELETE /api/user-accounts/user/scrummaster1/account/1
```

**Response (200 OK):**
```json
{
  "message": "Account assignment removed successfully"
}
```

**Permissions:** Only ADMIN can remove assignments
**Note:** Soft delete (sets active=false)

---

## How It Works

### For SCRUM_MASTER:
1. **Account Assignment**: ADMIN assigns accounts to SCRUM_MASTER via POST endpoint
2. **Dashboard View**: SCRUM_MASTER sees only OPEN queue tickets from assigned accounts
3. **Assignment**: SCRUM_MASTER can assign tickets to DEVELOPERs (from their assigned accounts only)

### For ADMIN:
- Can see all tickets (no filtering)
- Can assign/remove account assignments to SCRUM_MASTERs
- Can handle all accounts

### Example Workflow:

1. **Admin assigns accounts to SCRUM_MASTER:**
   ```bash
   POST /api/user-accounts
   {
     "ntid": "scrummaster1",
     "accountId": 1  # billing
   }
   
   POST /api/user-accounts
   {
     "ntid": "scrummaster1",
     "accountId": 2  # ocb
   }
   ```

2. **SCRUM_MASTER logs in:**
   - Dashboard shows only OPEN tickets from account 1 and 2
   - Can assign these tickets to DEVELOPERs

3. **Different SCRUM_MASTER with different accounts:**
   ```bash
   POST /api/user-accounts
   {
     "ntid": "scrummaster2",
     "accountId": 3  # different account
   }
   ```
   - This SCRUM_MASTER sees only tickets from account 3

## Testing with Postman

1. **Assign Account:**
   ```
   POST http://localhost:8081/api/user-accounts
   Headers: X-User-NTID: admin1
   Body: {
     "ntid": "scrummaster1",
     "accountId": 1
   }
   ```

2. **Get Accounts for User:**
   ```
   GET http://localhost:8081/api/user-accounts/user/scrummaster1
   ```

3. **Get Account IDs (Simplified):**
   ```
   GET http://localhost:8081/api/user-accounts/user/scrummaster1/account-ids
   ```

4. **Remove Assignment:**
   ```
   DELETE http://localhost:8081/api/user-accounts/user/scrummaster1/account/1
   Headers: X-User-NTID: admin1
   ```

## Notes

- Only SCRUM_MASTER role users can have account assignments
- ADMIN can see all tickets regardless of account assignments
- SCRUM_MASTER dashboard automatically filters by assigned accounts
- Multiple SCRUM_MASTERs can handle the same account
- Assignments are soft-deleted (active=false) when removed
