# Request Management API Documentation

## Overview
Complete API endpoints for Jira-like ticket/request management system with role-based access control, timer tracking, and ETA management.

**Author:** Mukund Kute

## Base URL
```
http://localhost:8081/api/requests
```

## Authentication
All endpoints require `X-User-NTID` header with the logged-in user's NTID.

## API Endpoints

### 1. Create Request
**POST** `/api/requests`

**Headers:**
```
X-User-NTID: user123
Content-Type: application/json
```

**Request Body:**
```json
{
  "title": "Fix login issue",
  "description": "Users are unable to login with special characters",
  "requestType": "BUG_FIX",
  "priority": "HIGH",
  "accountId": 1
}
```

**Response (201 Created):**
```json
{
  "message": "Request created successfully",
  "requestId": 1,
  "title": "Fix login issue",
  "status": "OPEN"
}
```

**Request Types:** `TOOL_ENHANCEMENT`, `ADHOC`, `BUG_FIX`, `FEATURE_REQUEST`, `OTHER`
**Priorities:** `LOW`, `MEDIUM`, `HIGH`, `URGENT`

---

### 2. Get All Requests
**GET** `/api/requests`

**Headers:**
```
X-User-NTID: user123
```

**Query Parameters (optional):**
- `status` - Filter by status (OPEN, ASSIGNED, IN_PROGRESS, ON_HOLD, COMPLETED, DELAYED, CANCELLED)
- `priority` - Filter by priority (LOW, MEDIUM, HIGH, URGENT)
- `requestType` - Filter by type (TOOL_ENHANCEMENT, ADHOC, BUG_FIX, FEATURE_REQUEST, OTHER)
- `accountId` - Filter by account ID

**Example:**
```
GET /api/requests?status=OPEN&priority=HIGH
```

**Response (200 OK):**
```json
[
  {
    "requestId": 1,
    "title": "Fix login issue",
    "description": "Users are unable to login...",
    "requestType": "BUG_FIX",
    "priority": "HIGH",
    "status": "OPEN",
    "createdBy": "user123",
    "assignedTo": null,
    "accountId": 1,
    "createdAt": "2026-01-30T10:00:00",
    "assignedAt": null,
    "eta": null,
    "timeInOpenQueue": "2 hours 30 minutes",
    "timeInDeveloperQueue": "0 minutes",
    "timeUntilEta": null,
    "etaApproaching": false,
    "etaExceeded": false
  }
]
```

**Role-based Access:**
- **USER**: Only own requests
- **DEVELOPER**: Only assigned requests
- **MANAGER**: Requests from their account
- **SCRUM_MASTER/ADMIN**: All requests

---

### 3. Get Single Request
**GET** `/api/requests/{id}`

**Headers:**
```
X-User-NTID: user123
```

**Response (200 OK):**
```json
{
  "requestId": 1,
  "title": "Fix login issue",
  "description": "Users are unable to login...",
  "requestType": "BUG_FIX",
  "priority": "HIGH",
  "status": "ASSIGNED",
  "createdBy": "user123",
  "assignedTo": "dev001",
  "accountId": 1,
  "createdAt": "2026-01-30T10:00:00",
  "updatedAt": "2026-01-30T11:00:00",
  "assignedAt": "2026-01-30T11:00:00",
  "eta": "2026-01-30T15:00:00",
  "timeInOpenQueue": "1 hour",
  "timeInDeveloperQueue": "30 minutes",
  "timeUntilEta": "3 hours 30 minutes",
  "etaApproaching": false,
  "etaExceeded": false
}
```

---

### 4. Update Request
**PUT** `/api/requests/{id}`

**Headers:**
```
X-User-NTID: user123
Content-Type: application/json
```

**Request Body:**
```json
{
  "title": "Fix login issue - Updated",
  "description": "Updated description",
  "requestType": "BUG_FIX",
  "priority": "URGENT"
}
```

**Response (200 OK):**
```json
{
  "message": "Request updated successfully",
  "requestId": 1,
  "title": "Fix login issue - Updated"
}
```

**Permissions:** Only creator or ADMIN can update

---

### 5. Assign Request
**POST** `/api/requests/{id}/assign`

**Headers:**
```
X-User-NTID: scrummaster001
Content-Type: application/json
```

**Request Body:**
```json
{
  "assignedTo": "dev001",
  "eta": "2026-01-30T15:00:00"
}
```

**Response (200 OK):**
```json
{
  "message": "Request assigned successfully",
  "requestId": 1,
  "assignedTo": "dev001",
  "eta": "2026-01-30T15:00:00"
}
```

**Permissions:** Only SCRUM_MASTER or ADMIN can assign
**Note:** Assigned user must have DEVELOPER role

---

### 6. Update Request Status
**PUT** `/api/requests/{id}/status`

**Headers:**
```
X-User-NTID: dev001
Content-Type: application/json
```

**Request Body:**
```json
{
  "status": "COMPLETED",
  "comment": "Fixed the login issue"
}
```

**Valid Status Values:**
- `IN_PROGRESS` - Can only be set from ASSIGNED
- `COMPLETED` - Task is completed
- `ON_HOLD` - Task is on hold
- `DELAYED` - Task is delayed

**Response (200 OK):**
```json
{
  "message": "Request status updated successfully",
  "requestId": 1,
  "status": "COMPLETED"
}
```

**Permissions:** Only DEVELOPER or ADMIN can update status
**Note:** DEVELOPER can only update assigned requests

---

### 7. Delete Request
**DELETE** `/api/requests/{id}`

**Headers:**
```
X-User-NTID: user123
```

**Response (200 OK):**
```json
{
  "message": "Request deleted successfully"
}
```

**Permissions:** Only creator or ADMIN can delete
**Note:** Soft delete (sets active=false)

---

### 8. Get Dashboard Statistics
**GET** `/api/requests/stats`

**Headers:**
```
X-User-NTID: user123
```

**Response (200 OK):**
```json
{
  "totalRequests": 50,
  "openRequests": 10,
  "assignedRequests": 5,
  "inProgressRequests": 8,
  "completedRequests": 27
}
```

**Role-based Stats:**
- **USER**: `myRequests` count
- **DEVELOPER**: `assignedToMe` count
- **ADMIN/SCRUM_MASTER**: Full statistics

---

### 9. Get ETA Alerts
**GET** `/api/requests/eta-alerts?thresholdMinutes=30`

**Headers:**
```
X-User-NTID: user123
```

**Query Parameters:**
- `thresholdMinutes` - Minutes before ETA to alert (default: 30)

**Response (200 OK):**
```json
[
  {
    "requestId": 1,
    "title": "Fix login issue",
    "assignedTo": "dev001",
    "eta": "2026-01-30T15:00:00",
    "timeUntilEta": "25 minutes",
    "etaExceeded": false
  }
]
```

---

## Request Status Flow

```
OPEN → ASSIGNED → IN_PROGRESS → COMPLETED
                ↓
            ON_HOLD
            DELAYED
```

## Timer Information

All request responses include timer information:
- **timeInOpenQueue**: Time from creation to assignment (or current time if not assigned)
- **timeInDeveloperQueue**: Time from assignment to now
- **timeUntilEta**: Time remaining until ETA
- **etaApproaching**: Boolean - true if ETA is within threshold (30 min default)
- **etaExceeded**: Boolean - true if ETA has passed

## Error Responses

All endpoints return error in this format:
```json
{
  "error": "Error message here"
}
```

**Common HTTP Status Codes:**
- `200 OK` - Success
- `201 Created` - Resource created
- `400 Bad Request` - Invalid input
- `401 Unauthorized` - Missing or invalid user NTID
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Testing with Postman

1. **Set Headers:**
   - `X-User-NTID: your_ntid`
   - `Content-Type: application/json`

2. **Test Create Request:**
   ```
   POST http://localhost:8081/api/requests
   Body: {
     "title": "Test Request",
     "description": "Test description",
     "requestType": "ADHOC",
     "priority": "MEDIUM"
   }
   ```

3. **Test Get Requests:**
   ```
   GET http://localhost:8081/api/requests
   ```

4. **Test Assign Request:**
   ```
   POST http://localhost:8081/api/requests/1/assign
   Body: {
     "assignedTo": "dev001",
     "eta": "2026-01-30T15:00:00"
   }
   ```

## Notes

- All timestamps are in ISO 8601 format
- Account ID is automatically set from user's account if not provided
- ETA alerts are calculated based on threshold (default 30 minutes)
- Timer calculations are done in real-time
- All requests are soft-deleted (active=false)
