# Finsight-AI Project - Complete Guide (Simple Explanation)

## 📖 What is This Project?

**Finsight-AI** is a **ticket management system** (like a help desk or support system) where:
- Users can **create tickets** (requests for help/work)
- Managers can **assign tickets** to developers
- Developers can **work on tickets** and update their status
- Everyone can **track progress** and see statistics

Think of it like a **digital to-do list** for a team, where tasks are tickets and people can see who's working on what.

---

## 🏗️ Project Structure (Where Everything Lives)

```
finsight-ai/
├── backend-springboot/          # Server-side code (Java/Spring Boot)
│   ├── src/main/java/com/finsight/
│   │   ├── controller/         # API endpoints (the doors to your server)
│   │   ├── service/            # Business logic (the brain)
│   │   ├── repository/         # Database access (talking to database)
│   │   ├── entity/            # Database table structures
│   │   └── dto/               # Data transfer objects (packages for sending data)
│   └── src/main/resources/
│       └── application.properties  # Configuration file
│
├── frontend-angular/           # Client-side code (User Interface)
│   └── src/app/
│       ├── components/        # UI screens/pages
│       ├── services/          # Functions to call backend APIs
│       ├── guards/           # Security checks
│       └── app.routes.ts     # Website navigation/routing
│
└── TABLE_STRUCTURE.sql        # Database table definitions
```

---

## 🗄️ Database Structure (Simple Explanation)

The system uses **4 main tables**:

### 1. **FLOWAI_USERS** - Stores all users
- **NTID**: User's unique ID (like username)
- **EMAIL**: User's email address
- **ACCOUNT**: Which department/account they belong to (SRE, Billing, QA, etc.)
- **ACCOUNT_ID**: Number linking to the account
- **PASSWORD**: Encrypted password
- **ROLE**: What they can do (USER, DEVELOPER, MANAGER, SCRUM_MASTER, ADMIN)
- **ACTIVE**: Whether account is enabled (1) or disabled (0)

### 2. **FLOWAI_ACCOUNTS** - Stores departments/accounts
- **ACCOUNT_ID**: Unique number
- **ACCOUNT_NAME**: Name like "SRE", "Billing", "QA", "BOPS", "Sched"
- **ACTIVE**: Whether account is active

### 3. **FLOWAI_REQUESTS** - Stores all tickets
- **REQUEST_ID**: Unique ticket number
- **TITLE**: What the ticket is about
- **DESCRIPTION**: Details
- **REQUEST_TYPE**: Type (Bug, Feature, etc.)
- **PRIORITY**: How urgent (URGENT, HIGH, MEDIUM, LOW)
- **STATUS**: Current state (OPEN, ASSIGNED, IN_PROGRESS, COMPLETED, etc.)
- **CREATED_BY**: Who created it (NTID)
- **ASSIGNED_TO**: Who is working on it (NTID)
- **ACCOUNT_ID**: Which account/department it belongs to
- **ETA**: Expected completion time
- **ACTIVE**: Whether ticket is deleted (soft delete)

### 4. **FLOWAI_USER_ACCOUNTS** - Links users to multiple accounts
- **NTID**: User ID
- **ACCOUNT_ID**: Account they can access
- Used for SCRUM_MASTER who can handle multiple accounts

### 5. **FLOWAI_COMMENTS** - Stores comments on tickets
- **COMMENT_ID**: Unique comment number
- **REQUEST_ID**: Which ticket it belongs to
- **COMMENT_TEXT**: The comment message
- **COMMENTED_BY**: Who wrote it (NTID)
- **IS_ETA_CHANGE**: Whether it's an ETA change notification

---

## 🔌 Backend APIs (All Endpoints Explained)

### Base URL: `http://localhost:8080/api`

---

### 🔐 Authentication APIs (`/api/auth`)

#### 1. **POST /api/auth/register** - Create New User Account
**What it does**: When someone first uses the system, this creates their account.

**What you send**:
```json
{
  "ntid": "john.doe",
  "email": "john@example.com",
  "account": "SRE",
  "password": "mypassword"
}
```

**What you get back**: Success message or error

**Who can use**: Anyone (public endpoint)

---

#### 2. **POST /api/auth/login** - Login to System
**What it does**: Verifies username/password and gives you access.

**What you send**:
```json
{
  "ntid": "john.doe",
  "password": "mypassword"
}
```

**What you get back**: User info (NTID, email, role) if login successful

**Who can use**: Anyone (public endpoint)

---

### 📋 Request/Ticket APIs (`/api/requests`)

#### 3. **POST /api/requests** - Create New Ticket
**What it does**: Creates a new ticket/request for work.

**What you send** (in header: `X-User-NTID: john.doe`):
```json
{
  "title": "Fix login bug",
  "description": "Users can't login",
  "requestType": "BUG",
  "priority": "HIGH",
  "accountId": 105
}
```

**What you get back**: Ticket ID and confirmation

**Who can use**: Any logged-in user

---

#### 4. **GET /api/requests** - Get All Tickets (Filtered by Role)
**What it does**: Gets list of tickets based on who you are:
- **ADMIN**: Sees all tickets
- **SCRUM_MASTER**: Sees tickets from their accounts
- **DEVELOPER**: Sees tickets they created, assigned to them, or OPEN tickets
- **USER**: Sees tickets they created

**Query Parameters** (optional):
- `status=OPEN` - Filter by status
- `priority=HIGH` - Filter by priority
- `requestType=BUG` - Filter by type
- `accountId=105` - Filter by account

**What you get back**: List of tickets with all details

**Who can use**: Any logged-in user

---

#### 5. **GET /api/requests/{id}** - Get Single Ticket Details
**What it does**: Gets full details of one specific ticket.

**Example**: `GET /api/requests/123`

**What you get back**: Complete ticket information

**Who can use**: Any logged-in user

---

#### 6. **PUT /api/requests/{id}** - Update Ticket
**What it does**: Updates ticket title, description, type, or priority.

**What you send**:
```json
{
  "title": "Updated title",
  "description": "New description",
  "requestType": "FEATURE",
  "priority": "MEDIUM"
}
```

**Who can update**:
- Ticket creator
- Assigned developer
- ADMIN
- SCRUM_MASTER (for their accounts)

---

#### 7. **POST /api/requests/{id}/assign** - Assign Ticket to Developer
**What it does**: Assigns a ticket to a developer and sets ETA (expected completion time).

**What you send**:
```json
{
  "assignedTo": "dev001",
  "eta": "2024-12-31T10:00:00"
}
```

**Who can assign**: Only SCRUM_MASTER or ADMIN

**Note**: Only shows users from the same account as the ticket (excludes ADMIN and ticket creator)

---

#### 8. **PUT /api/requests/{id}/status** - Update Ticket Status
**What it does**: Changes ticket status (IN_PROGRESS, COMPLETED, ON_HOLD, etc.).

**What you send**:
```json
{
  "status": "IN_PROGRESS",
  "comment": "Started working on it"
}
```

**Who can update**:
- Assigned developer
- ADMIN
- SCRUM_MASTER (for their accounts)

---

#### 9. **PUT /api/requests/{id}/eta** - Update ETA
**What it does**: Changes the expected completion time.

**What you send**:
```json
{
  "newEta": "2024-12-31T15:00:00",
  "changeReason": "Need more time",
  "commentText": "Additional testing required"
}
```

**Who can update**: Assigned developer, ADMIN, or SCRUM_MASTER

---

#### 10. **DELETE /api/requests/{id}** - Delete Ticket (Soft Delete)
**What it does**: Marks ticket as deleted (doesn't actually remove from database).

**Who can delete**: Ticket creator, ADMIN, or SCRUM_MASTER

---

#### 11. **GET /api/requests/stats** - Get Dashboard Statistics
**What it does**: Gets counts of tickets for dashboard display.

**What you get back**:
```json
{
  "openTickets": 5,
  "assignedToMe": 3,
  "createdByMe": 10,
  "totalTickets": 50
}
```

**Who can use**: Any logged-in user

---

#### 12. **GET /api/requests/account-statistics** - Get Account Statistics
**What it does**: Gets statistics per account (how many tickets per account).

**What you get back**: List of accounts with ticket counts:
```json
[
  {
    "accountId": 105,
    "accountName": "Sched",
    "totalTickets": 20,
    "resolvedTickets": 15,
    "pendingTickets": 3,
    "onHoldTickets": 2,
    "openTickets": 5,
    "crossedEtaTickets": 1
  }
]
```

**Who can use**: Any logged-in user

---

#### 13. **GET /api/requests/user-statistics-by-account/{accountId}** - Get User Stats for Account
**What it does**: Gets statistics for all users in a specific account.

**Example**: `GET /api/requests/user-statistics-by-account/105`

**What you get back**: List of users with their ticket counts:
```json
[
  {
    "ntid": "sayanc",
    "email": "sayanc@amdocs.com",
    "role": "DEVELOPER",
    "totalTickets": 10,
    "resolvedTickets": 8,
    "pendingTickets": 2,
    "onHoldTickets": 0,
    "crossedEtaTickets": 1
  }
]
```

**Who can use**: Any logged-in user

---

#### 14. **GET /api/requests/user-statistics** - Get All User Statistics
**What it does**: Gets statistics for all users across all accounts.

**What you get back**: Same format as above, but for all users

**Who can use**: Any logged-in user

---

#### 15. **GET /api/requests/eta-alerts** - Get ETA Alerts
**What it does**: Gets tickets where ETA is approaching or exceeded.

**Query Parameter**: `thresholdMinutes=30` (default 30)

**What you get back**: List of tickets with approaching/exceeded ETAs

**Who can use**: Any logged-in user

---

#### 16. **GET /api/requests/{id}/comments** - Get Comments on Ticket
**What it does**: Gets all comments for a specific ticket.

**What you get back**: List of comments with who wrote them and when

**Who can use**: Any logged-in user

---

#### 17. **POST /api/requests/{id}/comments** - Add Comment to Ticket
**What it does**: Adds a comment to a ticket.

**What you send**:
```json
{
  "commentText": "This is my comment",
  "isEtaChange": false
}
```

**Who can comment**: Any logged-in user

---

### 👥 User Management APIs (`/api/users`)

#### 18. **GET /api/users** - Get All Users (for Assignment Dropdown)
**What it does**: Gets list of users that can be assigned tickets.

**Query Parameters**:
- `role=DEVELOPER` - Filter by role
- `accountId=105` - Filter by account (only shows users from that account)
- `createdByNtid=john.doe` - Exclude this user from results

**What you get back**: List of users (excludes ADMIN and ticket creator if provided)

**Who can use**: Only SCRUM_MASTER or ADMIN

---

#### 19. **GET /api/users/{ntid}** - Get Single User Details
**What it does**: Gets details of one specific user.

**Example**: `GET /api/users/john.doe`

**Who can use**: Any logged-in user

---

#### 20. **PUT /api/users/{ntid}** - Update User Details
**What it does**: Updates user information (email, account, role, active status).

**What you send**:
```json
{
  "email": "newemail@example.com",
  "account": "QA",
  "role": "DEVELOPER",
  "active": true
}
```

**Who can update**: Only ADMIN or SCRUM_MASTER

---

#### 21. **DELETE /api/users/{ntid}** - Delete User
**What it does**: Deletes a user account.

**Who can delete**: Only ADMIN

---

#### 22. **POST /api/users/admin** - Create Admin User
**What it does**: Creates a new admin user.

**Who can create**: Only ADMIN

---

### 🏢 Account APIs (`/api/accounts`)

#### 23. **GET /api/accounts/active** - Get All Active Accounts
**What it does**: Gets list of all active accounts (for dropdowns).

**What you get back**: List of accounts:
```json
[
  {
    "accountId": 101,
    "accountName": "SRE"
  },
  {
    "accountId": 105,
    "accountName": "Sched"
  }
]
```

**Who can use**: Any logged-in user

---

#### 24. **GET /api/accounts/all** - Get All Accounts (Including Inactive)
**What it does**: Gets all accounts including inactive ones.

**Who can use**: Any logged-in user

---

#### 25. **GET /api/accounts/test** - Test Accounts API
**What it does**: Just a test endpoint to check if accounts API is working.

**Who can use**: Anyone

---

### 🔗 User-Account Relationship APIs (`/api/user-accounts`)

#### 26. **POST /api/user-accounts/assign** - Assign Account to SCRUM_MASTER
**What it does**: Links a SCRUM_MASTER to an account (allows them to see/manage tickets from that account).

**What you send**:
```json
{
  "ntid": "scrum001",
  "accountId": 105
}
```

**Who can assign**: Only ADMIN

---

#### 27. **GET /api/user-accounts/user/{ntid}** - Get Accounts for User
**What it does**: Gets all accounts a user is linked to.

**Example**: `GET /api/user-accounts/user/scrum001`

**What you get back**: List of accounts the user handles

**Who can use**: Any logged-in user

---

#### 28. **GET /api/user-accounts/account/{accountId}** - Get Users for Account
**What it does**: Gets all users linked to a specific account.

**Example**: `GET /api/user-accounts/account/105`

**What you get back**: List of users who handle this account

**Who can use**: Any logged-in user

---

#### 29. **DELETE /api/user-accounts/user/{ntid}/account/{accountId}** - Remove Account Assignment
**What it does**: Removes the link between a user and an account.

**Who can remove**: Only ADMIN

---

## 🖥️ Frontend Components (UI Screens)

### 📍 Routes (Website URLs)

| URL | Component | What It Shows |
|-----|-----------|---------------|
| `/login` | LoginComponent | Login page |
| `/register` | RegisterComponent | User registration page |
| `/dashboard` | DashboardComponent | Main dashboard with tickets |
| `/dashboard/create` | CreateRequestComponent | Form to create new ticket |
| `/dashboard/request/:id` | RequestDetailComponent | View/edit single ticket |
| `/dashboard/users` | UserManagementComponent | Manage users (ADMIN/SCRUM_MASTER) |
| `/dashboard/statistics` | UserStatisticsComponent | View statistics |
| `/dashboard/tickets/:type` | ViewAllTicketsComponent | View filtered tickets (open/assigned/my) |

---

### 🎨 Component Details

#### 1. **LoginComponent** (`/login`)
**Location**: `frontend-angular/src/app/components/login/`

**What it does**:
- Shows login form (NTID and password)
- Calls `POST /api/auth/login`
- Stores user info in browser
- Redirects to dashboard after login

**Files**:
- `login.component.ts` - Logic
- `login.component.html` - UI template
- `login.component.scss` - Styling

---

#### 2. **RegisterComponent** (`/register`)
**Location**: `frontend-angular/src/app/components/register/`

**What it does**:
- Shows registration form
- Calls `POST /api/auth/register`
- Creates new user account
- Redirects to login after registration

---

#### 3. **DashboardComponent** (`/dashboard`)
**Location**: `frontend-angular/src/app/components/dashboard/`

**What it does**:
- Main screen after login
- Shows 3 sections:
  - **OPEN Tickets**: Unassigned tickets
  - **ASSIGNED Tickets**: Tickets assigned to you
  - **My Tickets**: Tickets you created
- Calls `GET /api/requests/stats` for counts
- Calls `GET /api/requests` to get tickets
- Shows ticket cards with status, priority, ETA
- Clicking a ticket opens RequestDetailComponent

**Key Features**:
- Filters tickets by status
- Shows ETA alerts (approaching/exceeded)
- "View All" buttons to see more tickets
- Refresh button to reload data

---

#### 4. **CreateRequestComponent** (`/dashboard/create`)
**Location**: `frontend-angular/src/app/components/create-request/`

**What it does**:
- Form to create new ticket
- Fields: Title, Description, Type, Priority, Account
- Calls `POST /api/requests`
- Gets accounts from `GET /api/accounts/active`
- Redirects to dashboard after creation

---

#### 5. **RequestDetailComponent** (`/dashboard/request/:id`)
**Location**: `frontend-angular/src/app/components/request-detail/`

**What it does**:
- Shows full ticket details
- Calls `GET /api/requests/{id}`
- Allows:
  - **Update ticket** (if you have permission)
  - **Assign ticket** (SCRUM_MASTER/ADMIN only)
  - **Update status** (assigned developer/ADMIN/SCRUM_MASTER)
  - **Update ETA** (assigned developer/ADMIN/SCRUM_MASTER)
  - **Add comments**
  - **Delete ticket** (creator/ADMIN/SCRUM_MASTER)

**Key Features**:
- Shows all comments
- Shows ETA alerts
- Shows timer information (time in queue)
- Permission-based buttons (only shows what you can do)

---

#### 6. **UserManagementComponent** (`/dashboard/users`)
**Location**: `frontend-angular/src/app/components/user-management/`

**What it does**:
- Lists all users (ADMIN/SCRUM_MASTER only)
- Calls `GET /api/users`
- Allows:
  - Edit user details
  - Delete users (ADMIN only)
  - Create admin users (ADMIN only)
  - Assign accounts to SCRUM_MASTER (ADMIN only)

---

#### 7. **UserStatisticsComponent** (`/dashboard/statistics`)
**Location**: `frontend-angular/src/app/components/user-statistics/`

**What it does**:
- Shows statistics in 3 views:
  1. **Account Statistics**: Tickets per account
  2. **User Statistics**: Tickets per user (when you click an account)
  3. **Filtered Tickets**: When you click a number, shows those tickets

**Calls**:
- `GET /api/requests/account-statistics` - For account stats
- `GET /api/requests/user-statistics-by-account/{id}` - For user stats
- `GET /api/requests` - For filtered tickets

**Key Features**:
- Click account → see users in that account
- Click number (Total/Resolved/etc.) → see those specific tickets
- Shows: Total, Resolved, In Progress, On Hold, Open, Crossed ETA

---

#### 8. **ViewAllTicketsComponent** (`/dashboard/tickets/:type`)
**Location**: `frontend-angular/src/app/components/view-all-tickets/`

**What it does**:
- Shows filtered list of tickets
- Types: `open`, `assigned`, `my`, `all`
- Calls `GET /api/requests` with filters
- Shows tickets in card grid layout
- Click ticket → opens RequestDetailComponent

---

## 🔄 How Data Flows

### Example: Creating a Ticket

1. **User fills form** in `CreateRequestComponent`
2. **Frontend calls** `POST /api/requests` with ticket data
3. **Backend `RequestController`** receives request
4. **Controller calls** `RequestService.createRequest()`
5. **Service validates** data and saves to database
6. **Database** stores in `FLOWAI_REQUESTS` table
7. **Service returns** saved ticket
8. **Controller sends** response to frontend
9. **Frontend shows** success message and redirects

### Example: Assigning a Ticket

1. **SCRUM_MASTER opens** ticket in `RequestDetailComponent`
2. **Clicks "Assign Ticket"** button
3. **Frontend calls** `GET /api/users?accountId=105` (gets users from same account)
4. **Backend filters** users (excludes ADMIN and ticket creator)
5. **Dropdown shows** only eligible users
6. **SCRUM_MASTER selects** user and sets ETA
7. **Frontend calls** `POST /api/requests/{id}/assign`
8. **Backend updates** ticket in database
9. **Ticket status** changes to ASSIGNED
10. **Frontend refreshes** to show updated ticket

---

## 🔐 User Roles & Permissions

### **USER**
- Can create tickets
- Can view tickets they created
- Can update tickets they created
- Can delete tickets they created
- Can comment on tickets

### **DEVELOPER**
- Everything USER can do, PLUS:
- Can see tickets assigned to them
- Can see all OPEN tickets
- Can update status of assigned tickets
- Can update ETA of assigned tickets
- Can comment on assigned tickets

### **MANAGER**
- Same as DEVELOPER (currently)

### **SCRUM_MASTER**
- Everything DEVELOPER can do, PLUS:
- Can see all tickets from their assigned accounts
- Can assign tickets to developers (from same account)
- Can update tickets in their accounts
- Can manage users (view/edit)
- Can assign accounts to themselves (via ADMIN)

### **ADMIN**
- Can do EVERYTHING
- Can see all tickets
- Can assign tickets to anyone
- Can manage all users
- Can assign accounts to SCRUM_MASTERS
- Can delete any ticket
- Can update any ticket

---

## 🗂️ File Organization

### Backend Files

```
backend-springboot/src/main/java/com/finsight/
│
├── controller/              # API Endpoints (The Doors)
│   ├── AuthController.java           # Login/Register APIs
│   ├── RequestController.java        # Ticket APIs
│   ├── UserController.java            # User Management APIs
│   ├── AccountController.java        # Account APIs
│   └── UserAccountController.java    # User-Account Link APIs
│
├── service/                 # Business Logic (The Brain)
│   ├── AuthenticationService.java    # Login/Register logic
│   ├── RequestService.java           # Ticket operations
│   ├── RequestCommentService.java    # Comment operations
│   ├── UserService.java              # User operations
│   ├── UserAccountService.java       # User-Account operations
│   └── TimerService.java             # Time calculations
│
├── repository/              # Database Access (The Database Talker)
│   ├── RequestRepository.java        # Ticket database queries
│   ├── UserRepository.java           # User database queries
│   ├── AccountRepository.java        # Account database queries
│   └── UserAccountRepository.java    # Junction table queries
│
├── entity/                  # Database Table Structures
│   ├── Request.java                  # Ticket table structure
│   ├── User.java                     # User table structure
│   ├── Account.java                  # Account table structure
│   ├── UserAccount.java              # Junction table structure
│   ├── Comment.java                  # Comment table structure
│   └── UserRole.java                 # Role enum
│
└── dto/                     # Data Transfer Objects (Packages)
    ├── CreateRequestDTO.java         # Package for creating ticket
    ├── UpdateRequestDTO.java         # Package for updating ticket
    ├── AssignRequestDTO.java         # Package for assigning ticket
    ├── LoginRequestDTO.java          # Package for login
    ├── UserRegistrationDTO.java       # Package for registration
    └── ... (more DTOs)
```

### Frontend Files

```
frontend-angular/src/app/
│
├── components/              # UI Screens
│   ├── login/                      # Login page
│   ├── register/                   # Registration page
│   ├── dashboard/                  # Main dashboard
│   ├── create-request/             # Create ticket form
│   ├── request-detail/             # View/edit ticket
│   ├── user-management/            # Manage users
│   ├── user-statistics/            # Statistics view
│   └── view-all-tickets/           # Filtered tickets view
│
├── services/               # API Call Functions
│   ├── auth.service.ts            # Login/Register API calls
│   ├── request.service.ts         # Ticket API calls
│   └── user.service.ts            # User API calls
│
├── guards/                 # Security Checks
│   └── can-deactivate.guard.ts    # Prevents leaving page with unsaved changes
│
└── app.routes.ts          # Website navigation/routing
```

---

## 🔧 Key Concepts Explained Simply

### **DTO (Data Transfer Object)**
Think of it as a **package** for sending data. Instead of sending raw data, you put it in a DTO "box" with labels.

**Example**: `CreateRequestDTO` is a box containing:
- Title
- Description
- RequestType
- Priority
- AccountId

### **Entity**
Think of it as a **blueprint** for a database table. It defines what columns exist.

**Example**: `Request` entity defines:
- requestId (number)
- title (text)
- status (text)
- etc.

### **Repository**
Think of it as a **translator** between your code and database. You ask it to "find all tickets" and it translates that to SQL.

**Example**: `RequestRepository.findByStatus()` → SQL: `SELECT * FROM FLOWAI_REQUESTS WHERE STATUS = ?`

### **Service**
Think of it as the **brain** that does the thinking. It contains all the business logic.

**Example**: `RequestService.createRequest()`:
1. Validates the data
2. Checks permissions
3. Saves to database
4. Returns result

### **Controller**
Think of it as the **door** to your server. It receives requests and sends responses.

**Example**: `RequestController` has doors like:
- `/api/requests` (POST) - Create ticket door
- `/api/requests/{id}` (GET) - Get ticket door
- `/api/requests/{id}` (PUT) - Update ticket door

---

## 🚀 How to Use This System

### For a Regular User:
1. **Register** at `/register`
2. **Login** at `/login`
3. **Create tickets** from dashboard
4. **View your tickets** in "My Tickets" section
5. **Comment** on tickets

### For a Developer:
1. **Login**
2. **See assigned tickets** in "ASSIGNED Tickets" section
3. **Click ticket** to view details
4. **Update status** (IN_PROGRESS, COMPLETED, etc.)
5. **Update ETA** if needed
6. **Add comments** as you work

### For a SCRUM_MASTER:
1. **Login**
2. **See all tickets** from your accounts
3. **Assign tickets** to developers
4. **View statistics** to track progress
5. **Manage users** in your accounts

### For an ADMIN:
1. **Login**
2. **Do everything** - full access
3. **Manage all users**
4. **Assign accounts** to SCRUM_MASTERS
5. **View all statistics**

---

## 📝 Common Operations

### Creating a Ticket:
1. Go to Dashboard
2. Click "Create Request"
3. Fill form (Title, Description, Type, Priority, Account)
4. Click "Submit"
5. Ticket appears in OPEN section

### Assigning a Ticket:
1. Open ticket details
2. Click "Assign Ticket" (SCRUM_MASTER/ADMIN only)
3. Select developer from dropdown (only shows users from same account)
4. Set ETA (expected completion time)
5. Click "Assign"
6. Ticket moves to ASSIGNED status

### Updating Ticket Status:
1. Open ticket details
2. Click "Update Status"
3. Select new status (IN_PROGRESS, COMPLETED, ON_HOLD, etc.)
4. Add optional comment
5. Click "Update"
6. Status changes

### Viewing Statistics:
1. Go to Statistics page
2. See account-level statistics
3. Click account → see user statistics
4. Click number → see filtered tickets

---

## 🎯 Summary

**Finsight-AI** is a ticket management system with:
- **Backend**: Java/Spring Boot APIs
- **Frontend**: Angular web interface
- **Database**: Oracle (4 main tables)
- **Features**: Create, assign, track, and manage tickets
- **Roles**: USER, DEVELOPER, MANAGER, SCRUM_MASTER, ADMIN
- **Accounts**: SRE, Billing, QA, BOPS, Sched, etc.

Everything is organized in clear folders, and each component has a specific job. The APIs are reusable, and the code follows standard patterns.

---

## 📚 Quick Reference

**Backend API Base**: `http://localhost:8080/api`

**Frontend URL**: `http://localhost:4200`

**Main Database Tables**:
- FLOWAI_USERS
- FLOWAI_ACCOUNTS
- FLOWAI_REQUESTS
- FLOWAI_USER_ACCOUNTS
- FLOWAI_COMMENTS

**Key Services**:
- RequestService - Ticket operations
- UserService - User operations
- AuthenticationService - Login/Register
- PermissionService - Access control (to be created)

**Key Components**:
- Dashboard - Main screen
- RequestDetail - Ticket view/edit
- UserStatistics - Statistics view
- UserManagement - User admin

---

*This document explains the entire Finsight-AI project in simple terms. Everything is organized, reusable, and follows standard software patterns.*
