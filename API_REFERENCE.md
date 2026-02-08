# API Quick Reference Guide

## 🔐 Authentication APIs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Create new user account | No |
| POST | `/api/auth/login` | Login to system | No |

---

## 📋 Request/Ticket APIs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/requests` | Create new ticket | Yes |
| GET | `/api/requests` | Get all tickets (filtered by role) | Yes |
| GET | `/api/requests/{id}` | Get single ticket details | Yes |
| PUT | `/api/requests/{id}` | Update ticket | Yes |
| POST | `/api/requests/{id}/assign` | Assign ticket to developer | Yes (SCRUM_MASTER/ADMIN) |
| PUT | `/api/requests/{id}/status` | Update ticket status | Yes |
| PUT | `/api/requests/{id}/eta` | Update ETA | Yes |
| DELETE | `/api/requests/{id}` | Delete ticket (soft delete) | Yes |
| GET | `/api/requests/stats` | Get dashboard statistics | Yes |
| GET | `/api/requests/account-statistics` | Get account-level statistics | Yes |
| GET | `/api/requests/user-statistics-by-account/{accountId}` | Get user stats for account | Yes |
| GET | `/api/requests/user-statistics` | Get all user statistics | Yes |
| GET | `/api/requests/eta-alerts` | Get ETA alerts | Yes |
| GET | `/api/requests/{id}/comments` | Get comments on ticket | Yes |
| POST | `/api/requests/{id}/comments` | Add comment to ticket | Yes |

---

## 👥 User Management APIs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/users` | Get all users (for assignment) | Yes (SCRUM_MASTER/ADMIN) |
| GET | `/api/users/{ntid}` | Get single user details | Yes |
| PUT | `/api/users/{ntid}` | Update user details | Yes (ADMIN/SCRUM_MASTER) |
| DELETE | `/api/users/{ntid}` | Delete user | Yes (ADMIN) |
| POST | `/api/users/admin` | Create admin user | Yes (ADMIN) |

---

## 🏢 Account APIs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/accounts/active` | Get all active accounts | Yes |
| GET | `/api/accounts/all` | Get all accounts (including inactive) | Yes |
| GET | `/api/accounts/test` | Test accounts API | No |

---

## 🔗 User-Account Relationship APIs

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/user-accounts/assign` | Assign account to SCRUM_MASTER | Yes (ADMIN) |
| GET | `/api/user-accounts/user/{ntid}` | Get accounts for user | Yes |
| GET | `/api/user-accounts/account/{accountId}` | Get users for account | Yes |
| DELETE | `/api/user-accounts/user/{ntid}/account/{accountId}` | Remove account assignment | Yes (ADMIN) |

---

## 📊 Query Parameters

### GET /api/requests
- `status` - Filter by status (OPEN, ASSIGNED, IN_PROGRESS, etc.)
- `priority` - Filter by priority (URGENT, HIGH, MEDIUM, LOW)
- `requestType` - Filter by type (BUG, FEATURE, etc.)
- `accountId` - Filter by account ID

### GET /api/users
- `role` - Filter by role (USER, DEVELOPER, MANAGER, SCRUM_MASTER, ADMIN)
- `accountId` - Filter by account (only shows users from that account)
- `createdByNtid` - Exclude this user from results

### GET /api/requests/eta-alerts
- `thresholdMinutes` - Minutes before ETA to alert (default: 30)

---

## 🔑 Request Headers

All authenticated requests require:
```
X-User-NTID: your-ntid-here
```

Example:
```bash
curl -H "X-User-NTID: john.doe" http://localhost:8080/api/requests
```

---

## 📦 Request/Response Examples

### Create Ticket
**Request:**
```json
POST /api/requests
Headers: X-User-NTID: john.doe
Body:
{
  "title": "Fix login bug",
  "description": "Users can't login",
  "requestType": "BUG",
  "priority": "HIGH",
  "accountId": 105
}
```

**Response:**
```json
{
  "message": "Request created successfully",
  "requestId": 123,
  "title": "Fix login bug",
  "status": "OPEN"
}
```

### Assign Ticket
**Request:**
```json
POST /api/requests/123/assign
Headers: X-User-NTID: scrum001
Body:
{
  "assignedTo": "dev001",
  "eta": "2024-12-31T10:00:00"
}
```

**Response:**
```json
{
  "message": "Request assigned successfully",
  "requestId": 123,
  "assignedTo": "dev001",
  "eta": "2024-12-31T10:00:00"
}
```

---

## 🎯 Frontend Routes

| Route | Component | Description |
|-------|-----------|-------------|
| `/login` | LoginComponent | Login page |
| `/register` | RegisterComponent | Registration page |
| `/dashboard` | DashboardComponent | Main dashboard |
| `/dashboard/create` | CreateRequestComponent | Create ticket form |
| `/dashboard/request/:id` | RequestDetailComponent | View/edit ticket |
| `/dashboard/users` | UserManagementComponent | Manage users |
| `/dashboard/statistics` | UserStatisticsComponent | View statistics |
| `/dashboard/tickets/:type` | ViewAllTicketsComponent | Filtered tickets view |

---

## 🔐 Role-Based Access

| Action | USER | DEVELOPER | MANAGER | SCRUM_MASTER | ADMIN |
|--------|------|-----------|---------|--------------|-------|
| Create Ticket | ✅ | ✅ | ✅ | ✅ | ✅ |
| View Own Tickets | ✅ | ✅ | ✅ | ✅ | ✅ |
| View Assigned Tickets | ❌ | ✅ | ✅ | ✅ | ✅ |
| View All Tickets | ❌ | ❌ | ❌ | ✅* | ✅ |
| Assign Tickets | ❌ | ❌ | ❌ | ✅* | ✅ |
| Update Any Ticket | ❌ | ❌ | ❌ | ✅* | ✅ |
| Delete Any Ticket | ❌ | ❌ | ❌ | ✅* | ✅ |
| Manage Users | ❌ | ❌ | ❌ | ✅* | ✅ |
| Create Admin | ❌ | ❌ | ❌ | ❌ | ✅ |

*SCRUM_MASTER can only do this for tickets in their assigned accounts

---

## 📝 Status Flow

```
OPEN → ASSIGNED → IN_PROGRESS → COMPLETED
                ↓
            ON_HOLD
                ↓
            DELAYED
```

- **OPEN**: New ticket, not assigned yet
- **ASSIGNED**: Assigned to a developer, not started
- **IN_PROGRESS**: Developer is working on it
- **ON_HOLD**: Temporarily paused
- **DELAYED**: Past ETA, not completed
- **COMPLETED**: Finished
- **CANCELLED**: Cancelled (soft deleted)

---

## 🎨 Priority Levels

- **URGENT**: Critical, needs immediate attention
- **HIGH**: Important, should be done soon
- **MEDIUM**: Normal priority
- **LOW**: Can wait

---

## 📊 Statistics Fields

### Account Statistics:
- `totalTickets` - Total tickets for this account
- `resolvedTickets` - Completed tickets
- `pendingTickets` - In Progress tickets
- `onHoldTickets` - On Hold tickets
- `openTickets` - Open (unassigned) tickets
- `crossedEtaTickets` - Tickets past their ETA

### User Statistics:
- `totalTickets` - Total tickets assigned to user
- `resolvedTickets` - Completed tickets
- `pendingTickets` - In Progress tickets
- `onHoldTickets` - On Hold tickets
- `crossedEtaTickets` - Tickets past their ETA

---

## 🔍 Common Filters

### Get tickets by status:
```
GET /api/requests?status=OPEN
GET /api/requests?status=ASSIGNED
GET /api/requests?status=IN_PROGRESS
GET /api/requests?status=COMPLETED
```

### Get tickets by priority:
```
GET /api/requests?priority=URGENT
GET /api/requests?priority=HIGH
```

### Get tickets by account:
```
GET /api/requests?accountId=105
```

### Get users by account (for assignment):
```
GET /api/users?accountId=105
```

---

## ⚠️ Important Notes

1. **All authenticated endpoints** require `X-User-NTID` header
2. **Account filtering** in user assignment only shows users from same account
3. **ADMIN users** are excluded from assignment dropdown
4. **Ticket creator** is excluded from assignment dropdown
5. **Soft delete** means tickets are marked inactive, not actually deleted
6. **ETA alerts** show tickets approaching or past their ETA
7. **Statistics** are calculated in real-time from database

---

*Quick reference for all APIs and endpoints in Finsight-AI*
