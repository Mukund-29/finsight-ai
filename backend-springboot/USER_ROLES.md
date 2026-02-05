# User Roles and Unit System

## User Roles

The system now supports the following roles:

### 1. USER
- **Purpose**: Basic users who can create tickets
- **Permissions**:
  - Can create new tickets/requests
  - Can view own requests
  - Cannot assign tickets
  - Cannot mark tasks as completed/hold/delayed
  - Cannot pull/view all records

### 2. SCRUM_MASTER
- **Purpose**: Users who can assign tickets to developers
- **Permissions**:
  - Can view all tickets
  - Can assign tickets to DEVELOPER role users
  - Can view assignment history
  - Cannot create tickets (unless they also have USER role)
  - Cannot mark tasks as completed/hold/delayed
  - Cannot pull/view detailed records

### 3. DEVELOPER
- **Purpose**: Users who receive ticket assignments and work on them
- **Permissions**:
  - Can receive ticket assignments
  - Can mark tasks as: **Completed**, **Hold**, **Delayed**
  - Can view assigned tickets
  - Can update ticket status
  - Cannot assign tickets to others
  - Cannot create tickets (unless they also have USER role)

### 4. MANAGER
- **Purpose**: Managers who can view team tickets and reports
- **Permissions**:
  - Can view team tickets
  - Can view reports for their team/unit
  - Can view ticket history
  - Cannot create tickets (unless they also have USER role)
  - Cannot assign tickets (unless they also have SCRUM_MASTER role)
  - Cannot mark tasks as completed/hold/delayed (unless they also have DEVELOPER role)

### 5. VIEWER
- **Purpose**: Users who can pull/view records and generate reports
- **Permissions**:
  - Can pull/view all records
  - Can generate reports
  - Can view ticket history
  - Cannot create tickets
  - Cannot assign tickets
  - Cannot mark tasks as completed/hold/delayed

### 6. ADMIN
- **Purpose**: Full administrative access
- **Permissions**:
  - All permissions from all roles
  - Can manage users (create, delete, update roles)
  - Can create admin users
  - Can view all tickets and records
  - Can assign tickets
  - Can mark tasks as completed/hold/delayed
  - Can pull/view all records

## Database Schema

The `FLOWAI_USERS` table includes:
- `ROLE` column supports: USER, SCRUM_MASTER, DEVELOPER, MANAGER, VIEWER, ADMIN

## API Changes

### Registration Request:
```json
{
  "ntid": "user123",
  "email": "user@example.com",
  "account": "Account Name",
  "accountId": 12345
}
```

### Registration Response:
```json
{
  "message": "User registered successfully",
  "ntid": "user123",
  "email": "user@example.com",
  "role": "USER",
  "accountId": 12345
}
```

### Get User Response:
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

## Role Assignment

- **Default Role**: When users register, they get `USER` role by default
- **Role Updates**: Only ADMIN can update user roles
- **Admin Creation**: Only ADMIN can create new admin users

## Example Use Cases

1. **Ticket Creation Flow**:
   - USER creates a ticket
   - SCRUM_MASTER (or ADMIN) assigns it to a DEVELOPER
   - DEVELOPER marks it as Completed/Hold/Delayed
   - MANAGER can view team tickets and reports
   - VIEWER can pull records and generate reports

2. **Manager Oversight**:
   - MANAGER can view all tickets for their team
   - MANAGER can generate reports for their team
   - MANAGER can track team performance

## Role Permissions Summary

| Role | Create Tickets | Assign Tickets | Mark Status | View Records | Manage Users | View Team Reports |
|------|---------------|----------------|-------------|-------------|--------------|------------------|
| USER | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| SCRUM_MASTER | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| DEVELOPER | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ |
| MANAGER | ❌ | ❌ | ❌ | ✅ | ❌ | ✅ |
| VIEWER | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| ADMIN | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

## Migration Notes

If you have existing users in the database, you'll need to:
1. Update any existing roles if needed:
   - `ASSIGNER` → `SCRUM_MASTER`
   - `WORKER` → `DEVELOPER`
   - Remove `SUPER_USER` role
2. If you previously added the `UNIT` column, you can remove it:
   ```sql
   ALTER TABLE FLOWAI_USERS DROP COLUMN UNIT;
   ```
