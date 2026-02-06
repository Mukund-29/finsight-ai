# Password Encryption Implementation

## Overview
Passwords are now encrypted using BCrypt before being stored in the database. This provides secure password storage and protects user credentials.

## Changes Made

### 1. SecurityConfig.java
- Added `PasswordEncoder` bean using BCrypt
- Added CORS configuration to allow frontend connections
- BCrypt is the industry-standard password hashing algorithm

### 2. UserService.java
- **Registration**: Passwords are now hashed using BCrypt before saving to database
- **Admin User Creation**: Passwords are hashed before storing
- Injected `PasswordEncoder` dependency

### 3. AuthenticationService.java
- **Login**: Verifies passwords using BCrypt for hashed passwords
- **Legacy Support**: 
  - Still supports users without passwords (uses NTID as password)
  - Automatically migrates plain text passwords to hashed format on login
  - Detects hashed passwords (starts with `$2a$`, `$2b$`, or `$2y$`)

## How It Works

### Password Storage
- New users: Passwords are hashed using BCrypt before saving
- Format: `$2a$10$...` (BCrypt hash format)
- Example: `password123` → `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`

### Password Verification
1. Check if password exists
2. If hashed (starts with `$2a$`, `$2b$`, or `$2y$`):
   - Use BCrypt to verify
3. If plain text (legacy):
   - Verify directly
   - Automatically migrate to hashed format
4. If no password:
   - Use NTID as password (legacy support)

## Migration Strategy

### Automatic Migration
- Existing users with plain text passwords will be automatically migrated to hashed format on their next login
- No manual intervention required
- Seamless transition for users

### Legacy Users
- Users without passwords continue to use NTID as password
- This maintains backward compatibility

## Security Benefits

1. **Password Protection**: Even if database is compromised, passwords cannot be easily recovered
2. **BCrypt Strength**: BCrypt is computationally expensive, making brute-force attacks difficult
3. **Salt Included**: BCrypt automatically includes salt in the hash
4. **Industry Standard**: BCrypt is widely used and trusted

## Testing

### New User Registration
```bash
# Register a new user - password will be hashed
POST /api/auth/register
{
  "ntid": "testuser",
  "email": "test@example.com",
  "password": "mypassword123",
  "account": "Billing"
}
```

### Login
```bash
# Login with plain text password - will verify against hash
POST /api/auth/login
{
  "ntid": "testuser",
  "password": "mypassword123"
}
```

### Verify in Database
```sql
-- Check password is hashed (should start with $2a$)
SELECT ntid, password FROM FLOWAI_USERS WHERE ntid = 'testuser';
```

## Notes

- **Password Length**: BCrypt can handle passwords up to 72 bytes
- **Performance**: BCrypt is intentionally slow (10 rounds by default) to prevent brute-force attacks
- **No Password Recovery**: Hashed passwords cannot be decrypted - password reset functionality would need to be implemented separately
- **Database Column**: Ensure `PASSWORD` column in `FLOWAI_USERS` table is at least 255 characters to accommodate BCrypt hashes

## Future Enhancements

1. Password strength requirements
2. Password reset functionality
3. Password expiration policies
4. Account lockout after failed attempts
