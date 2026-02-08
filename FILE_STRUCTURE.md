# File Structure Map - Where Everything Is Located

## 📁 Complete Project Structure

```
finsight-ai/
│
├── 📄 PROJECT_OVERVIEW.md          # Complete project explanation (THIS IS THE MAIN GUIDE)
├── 📄 API_REFERENCE.md             # Quick API reference
├── 📄 FILE_STRUCTURE.md            # This file - file locations
├── 📄 TABLE_STRUCTURE.sql          # Database table definitions
│
├── 📂 backend-springboot/          # Backend Server (Java/Spring Boot)
│   │
│   ├── 📂 src/main/java/com/finsight/
│   │   │
│   │   ├── 📂 controller/          # API Endpoints (The Doors)
│   │   │   ├── AuthController.java              # Login/Register APIs
│   │   │   ├── RequestController.java           # All Ticket APIs
│   │   │   ├── UserController.java              # User Management APIs
│   │   │   ├── AccountController.java            # Account APIs
│   │   │   └── UserAccountController.java       # User-Account Link APIs
│   │   │
│   │   ├── 📂 service/              # Business Logic (The Brain)
│   │   │   ├── AuthenticationService.java       # Login/Register logic
│   │   │   ├── RequestService.java               # Ticket operations
│   │   │   ├── RequestCommentService.java        # Comment operations
│   │   │   ├── UserService.java                  # User operations
│   │   │   ├── UserAccountService.java           # User-Account operations
│   │   │   └── TimerService.java                 # Time calculations
│   │   │
│   │   ├── 📂 repository/           # Database Access (The Database Talker)
│   │   │   ├── RequestRepository.java            # Ticket database queries
│   │   │   ├── UserRepository.java               # User database queries
│   │   │   ├── AccountRepository.java            # Account database queries
│   │   │   ├── UserAccountRepository.java        # Junction table queries
│   │   │   └── CommentRepository.java            # Comment queries
│   │   │
│   │   ├── 📂 entity/               # Database Table Structures
│   │   │   ├── Request.java                     # Ticket table structure
│   │   │   ├── User.java                        # User table structure
│   │   │   ├── Account.java                     # Account table structure
│   │   │   ├── UserAccount.java                 # Junction table structure
│   │   │   ├── Comment.java                     # Comment table structure
│   │   │   ├── RequestStatus.java               # Status enum
│   │   │   ├── RequestPriority.java             # Priority enum
│   │   │   ├── RequestType.java                 # Type enum
│   │   │   └── UserRole.java                    # Role enum
│   │   │
│   │   ├── 📂 dto/                  # Data Transfer Objects (Packages)
│   │   │   ├── CreateRequestDTO.java            # Package for creating ticket
│   │   │   ├── UpdateRequestDTO.java            # Package for updating ticket
│   │   │   ├── AssignRequestDTO.java            # Package for assigning ticket
│   │   │   ├── UpdateStatusDTO.java             # Package for status update
│   │   │   ├── UpdateEtaDTO.java                 # Package for ETA update
│   │   │   ├── LoginRequestDTO.java             # Package for login
│   │   │   ├── UserRegistrationDTO.java         # Package for registration
│   │   │   ├── UpdateUserDTO.java               # Package for user update
│   │   │   ├── AssignAccountDTO.java            # Package for account assignment
│   │   │   ├── CreateCommentDTO.java            # Package for creating comment
│   │   │   ├── CommentDTO.java                  # Package for comment response
│   │   │   ├── AuthResponseDTO.java            # Package for auth response
│   │   │   ├── AccountStatsDTO.java             # Package for account statistics
│   │   │   └── UserTicketStatsDTO.java          # Package for user statistics
│   │   │
│   │   ├── 📂 config/               # Configuration
│   │   │   └── (Configuration files if any)
│   │   │
│   │   └── FinsightApplication.java # Main application entry point
│   │
│   └── 📂 src/main/resources/
│       ├── application.properties   # Database & server configuration
│       └── (Other config files)
│
├── 📂 frontend-angular/             # Frontend Client (Angular)
│   │
│   └── 📂 src/app/
│       │
│       ├── 📂 components/            # UI Screens/Pages
│       │   │
│       │   ├── 📂 login/
│       │   │   ├── login.component.ts           # Login logic
│       │   │   ├── login.component.html         # Login UI template
│       │   │   └── login.component.scss         # Login styles
│       │   │
│       │   ├── 📂 register/
│       │   │   ├── register.component.ts        # Registration logic
│       │   │   ├── register.component.html     # Registration UI
│       │   │   └── register.component.scss     # Registration styles
│       │   │
│       │   ├── 📂 dashboard/
│       │   │   ├── dashboard.component.ts      # Dashboard logic
│       │   │   ├── dashboard.component.html     # Dashboard UI (main screen)
│       │   │   └── dashboard.component.scss     # Dashboard styles
│       │   │
│       │   ├── 📂 create-request/
│       │   │   ├── create-request.component.ts  # Create ticket logic
│       │   │   ├── create-request.component.html # Create ticket form
│       │   │   └── create-request.component.scss # Form styles
│       │   │
│       │   ├── 📂 request-detail/
│       │   │   ├── request-detail.component.ts  # Ticket detail logic
│       │   │   ├── request-detail.component.html # Ticket detail UI
│       │   │   └── request-detail.component.scss # Detail styles
│       │   │
│       │   ├── 📂 user-management/
│       │   │   ├── user-management.component.ts # User management logic
│       │   │   ├── user-management.component.html # User management UI
│       │   │   └── user-management.component.scss # Management styles
│       │   │
│       │   ├── 📂 user-statistics/
│       │   │   ├── user-statistics.component.ts # Statistics logic
│       │   │   ├── user-statistics.component.html # Statistics UI
│       │   │   └── user-statistics.component.scss # Statistics styles
│       │   │
│       │   └── 📂 view-all-tickets/
│       │       ├── view-all-tickets.component.ts # Filtered tickets logic
│       │       ├── view-all-tickets.component.html # Filtered tickets UI
│       │       └── view-all-tickets.component.scss # Tickets styles
│       │
│       ├── 📂 services/              # API Call Functions
│       │   ├── auth.service.ts                  # Login/Register API calls
│       │   ├── request.service.ts               # Ticket API calls
│       │   └── user.service.ts                  # User API calls
│       │
│       ├── 📂 guards/                # Security Checks
│       │   └── can-deactivate.guard.ts          # Prevents leaving with unsaved changes
│       │
│       ├── 📂 error-handler.ts       # Global error handling
│       ├── 📄 app.routes.ts          # Website navigation/routing
│       ├── 📄 app.config.ts         # App configuration
│       ├── 📄 app.ts                # Main app component
│       ├── 📄 app.html              # Main app template
│       └── 📄 app.scss              # Global styles
│
└── 📄 docker-compose.yml            # Docker configuration
```

---

## 🗂️ File Locations by Function

### 🔐 Authentication Files
- **Backend**: `backend-springboot/.../controller/AuthController.java`
- **Backend Service**: `backend-springboot/.../service/AuthenticationService.java`
- **Frontend**: `frontend-angular/src/app/components/login/`
- **Frontend Service**: `frontend-angular/src/app/services/auth.service.ts`

### 📋 Ticket/Request Files
- **Backend Controller**: `backend-springboot/.../controller/RequestController.java`
- **Backend Service**: `backend-springboot/.../service/RequestService.java`
- **Backend Repository**: `backend-springboot/.../repository/RequestRepository.java`
- **Backend Entity**: `backend-springboot/.../entity/Request.java`
- **Frontend Service**: `frontend-angular/src/app/services/request.service.ts`
- **Frontend Components**:
  - Dashboard: `frontend-angular/src/app/components/dashboard/`
  - Create: `frontend-angular/src/app/components/create-request/`
  - Detail: `frontend-angular/src/app/components/request-detail/`
  - View All: `frontend-angular/src/app/components/view-all-tickets/`

### 👥 User Management Files
- **Backend Controller**: `backend-springboot/.../controller/UserController.java`
- **Backend Service**: `backend-springboot/.../service/UserService.java`
- **Backend Repository**: `backend-springboot/.../repository/UserRepository.java`
- **Backend Entity**: `backend-springboot/.../entity/User.java`
- **Frontend Service**: `frontend-angular/src/app/services/user.service.ts`
- **Frontend Component**: `frontend-angular/src/app/components/user-management/`

### 🏢 Account Files
- **Backend Controller**: `backend-springboot/.../controller/AccountController.java`
- **Backend Repository**: `backend-springboot/.../repository/AccountRepository.java`
- **Backend Entity**: `backend-springboot/.../entity/Account.java`

### 📊 Statistics Files
- **Backend**: Methods in `RequestService.java`
- **Frontend Component**: `frontend-angular/src/app/components/user-statistics/`
- **DTOs**: 
  - `AccountStatsDTO.java`
  - `UserTicketStatsDTO.java`

### 💬 Comment Files
- **Backend Service**: `backend-springboot/.../service/RequestCommentService.java`
- **Backend Repository**: `backend-springboot/.../repository/CommentRepository.java`
- **Backend Entity**: `backend-springboot/.../entity/Comment.java`
- **DTOs**: `CreateCommentDTO.java`, `CommentDTO.java`

### 🔗 User-Account Relationship Files
- **Backend Controller**: `backend-springboot/.../controller/UserAccountController.java`
- **Backend Service**: `backend-springboot/.../service/UserAccountService.java`
- **Backend Repository**: `backend-springboot/.../repository/UserAccountRepository.java`
- **Backend Entity**: `backend-springboot/.../entity/UserAccount.java`

---

## 📝 Configuration Files

### Backend Configuration
- **Database Config**: `backend-springboot/src/main/resources/application.properties`
- **Main App**: `backend-springboot/src/main/java/com/finsight/FinsightApplication.java`

### Frontend Configuration
- **Routes**: `frontend-angular/src/app/app.routes.ts`
- **App Config**: `frontend-angular/src/app/app.config.ts`
- **Environment**: `frontend-angular/src/environments/environment.ts`

---

## 🎯 Quick File Finder

### Need to find where tickets are created?
→ `RequestController.java` → `createRequest()` method
→ `RequestService.java` → `createRequest()` method
→ `CreateRequestComponent` (frontend)

### Need to find where users are assigned to tickets?
→ `RequestController.java` → `assignRequest()` method
→ `RequestService.java` → `assignRequest()` method
→ `RequestDetailComponent` (frontend) → "Assign Ticket" button

### Need to find where statistics are calculated?
→ `RequestService.java` → `getAccountStatistics()` method
→ `RequestService.java` → `getUserStatisticsByAccount()` method
→ `UserStatisticsComponent` (frontend)

### Need to find where permissions are checked?
→ `RequestService.java` → Various methods check `user.getRole()`
→ Look for `UserRole.ADMIN`, `UserRole.SCRUM_MASTER` checks

### Need to find where accounts are filtered in user dropdown?
→ `UserController.java` → `getAllUsers()` method
→ Filters by `accountId` parameter
→ Excludes ADMIN and ticket creator

---

## 🔍 Search Tips

### To find all API endpoints:
Search for `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` in:
- `backend-springboot/.../controller/` folder

### To find all database queries:
Search for `findBy`, `findAll`, `save`, `delete` in:
- `backend-springboot/.../repository/` folder

### To find all business logic:
Look in:
- `backend-springboot/.../service/` folder

### To find all UI components:
Look in:
- `frontend-angular/src/app/components/` folder

### To find all API calls from frontend:
Look in:
- `frontend-angular/src/app/services/` folder

---

## 📚 Documentation Files

- **PROJECT_OVERVIEW.md** - Complete project explanation (START HERE!)
- **API_REFERENCE.md** - Quick API reference guide
- **FILE_STRUCTURE.md** - This file - file locations
- **TABLE_STRUCTURE.sql** - Database schema

---

*Use this file to quickly locate any file or functionality in the project*
