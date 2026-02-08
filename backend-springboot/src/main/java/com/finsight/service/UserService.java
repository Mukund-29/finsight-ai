package com.finsight.service;

import com.finsight.dto.UserRegistrationDTO;
import com.finsight.entity.Account;
import com.finsight.entity.User;
import com.finsight.entity.UserRole;
import com.finsight.repository.AccountRepository;
import com.finsight.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User Service
 * 
 * @author Mukund Kute
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register new user (creates USER role by default)
     */
    @Transactional
    public User registerUser(UserRegistrationDTO registrationDTO) {
        System.out.println("  [UserService] registerUser() called");
        String ntid = registrationDTO.getNtid().toLowerCase();
        System.out.println("  [UserService] Processing NTID: " + ntid);

        // Check if user already exists
        System.out.println("  [UserService] Checking if NTID exists...");
        if (userRepository.existsByNtid(ntid)) {
            System.out.println("  [UserService] ERROR: NTID already exists");
            throw new RuntimeException("User with this NTID already exists");
        }
        System.out.println("  [UserService] NTID is available");

        // Check if email already exists
        System.out.println("  [UserService] Checking if email exists...");
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            System.out.println("  [UserService] ERROR: Email already exists");
            throw new RuntimeException("User with this email already exists");
        }
        System.out.println("  [UserService] Email is available");

        // Look up account and get accountId (account is now mandatory)
        String accountName = registrationDTO.getAccount();
        if (accountName == null || accountName.trim().isEmpty()) {
            System.out.println("  [UserService] ERROR: Account is required");
            throw new RuntimeException("Account is required");
        }
        
        System.out.println("  [UserService] Looking up account: " + accountName);
        Account account = accountRepository.findByAccountName(accountName)
            .orElseThrow(() -> {
                System.out.println("  [UserService] ERROR: Account not found: " + accountName);
                return new RuntimeException("Account not found: " + accountName);
            });
        Long accountId = account.getAccountId();
        System.out.println("  [UserService] Found account ID: " + accountId);

        // Hash password before storing
        String hashedPassword = passwordEncoder.encode(registrationDTO.getPassword());
        System.out.println("  [UserService] Password hashed successfully");

        // Create new user with USER role
        System.out.println("  [UserService] Creating new User object...");
        User user = new User(
            ntid,
            registrationDTO.getEmail(),
            accountName,
            accountId,
            hashedPassword
        );
        user.setRole(UserRole.USER); // Default role is USER
        System.out.println("  [UserService] User object created with role: " + user.getRole());

        System.out.println("  [UserService] Saving user to database...");
        User savedUser = userRepository.save(user);
        System.out.println("  [UserService] User saved successfully to database");
        System.out.println("  [UserService] Saved User Details:");
        System.out.println("    - NTID: " + savedUser.getNtid());
        System.out.println("    - Email: " + savedUser.getEmail());
        System.out.println("    - Role: " + savedUser.getRole());
        System.out.println("    - Account: " + savedUser.getAccount());
        System.out.println("    - Account ID: " + savedUser.getAccountId());
        System.out.println("    - Password: ***");
        
        return savedUser;
    }

    /**
     * Delete user (only by ADMIN)
     */
    @Transactional
    public void deleteUser(String ntid, String requestedBy) {
        // Check permissions - only ADMIN can delete users
        User requester = userRepository.findByNtid(requestedBy)
            .orElseThrow(() -> new RuntimeException("Requester not found"));

        if (requester.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only ADMIN can delete users");
        }

        userRepository.deleteById(ntid);
    }

    /**
     * Create admin user (only by ADMIN)
     */
    @Transactional
    public User createAdminUser(UserRegistrationDTO registrationDTO, String requestedBy) {
        // Only ADMIN can create admin users
        User requester = userRepository.findByNtid(requestedBy)
            .orElseThrow(() -> new RuntimeException("Requester not found"));

        if (requester.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only ADMIN can create admin users");
        }

        String ntid = registrationDTO.getNtid().toLowerCase();

        // Check if user already exists
        if (userRepository.existsByNtid(ntid)) {
            throw new RuntimeException("User with this NTID already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }

        // Look up account and get accountId (account is now mandatory)
        String accountName = registrationDTO.getAccount();
        if (accountName == null || accountName.trim().isEmpty()) {
            throw new RuntimeException("Account is required");
        }
        
        Account account = accountRepository.findByAccountName(accountName)
            .orElseThrow(() -> new RuntimeException("Account not found: " + accountName));
        Long accountId = account.getAccountId();

        // Hash password before storing
        String hashedPassword = passwordEncoder.encode(registrationDTO.getPassword());

        // Create admin user
        User user = new User(
            ntid,
            registrationDTO.getEmail(),
            accountName,
            accountId,
            hashedPassword
        );
        user.setRole(UserRole.ADMIN);

        return userRepository.save(user);
    }

    /**
     * Get user by NTID
     */
    public User getUserByNtid(String ntid) {
        return userRepository.findByNtid(ntid)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Get all active users
     */
    public List<User> getAllActiveUsers() {
        return userRepository.findByActiveTrueOrderByNtidAsc();
    }

    /**
     * Get all users (both active and inactive) - for user management
     */
    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByNtidAsc();
    }

    /**
     * Get active users by role
     */
    public List<User> getActiveUsersByRole(UserRole role) {
        return userRepository.findByRoleAndActiveTrueOrderByNtidAsc(role);
    }

    /**
     * Update user role (only by ADMIN)
     */
    @Transactional
    public User updateUserRole(String ntid, UserRole newRole, String requestedBy) {
        // Check permissions
        User requester = userRepository.findByNtid(requestedBy)
            .orElseThrow(() -> new RuntimeException("Requester not found"));

        if (requester.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only ADMIN can update user roles");
        }

        User user = userRepository.findByNtid(ntid)
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(newRole);
        return userRepository.save(user);
    }

    /**
     * Update user details (ADMIN and SCRUM_MASTER only)
     * Can update: NTID, email, account, role, active status
     * Cannot update: password (must be done separately)
     */
    @Transactional
    public User updateUserDetails(String ntid, com.finsight.dto.UpdateUserDTO updateDTO, String requestedBy) {
        System.out.println("  [UserService] updateUserDetails() called");
        System.out.println("  [UserService] Updating user: " + ntid);
        System.out.println("  [UserService] Requested by: " + requestedBy);

        // Check permissions - only ADMIN or SCRUM_MASTER can update users
        User requester = userRepository.findByNtid(requestedBy)
            .orElseThrow(() -> new RuntimeException("Requester not found"));

        if (requester.getRole() != UserRole.ADMIN && requester.getRole() != UserRole.SCRUM_MASTER) {
            throw new RuntimeException("Only ADMIN or SCRUM_MASTER can update user details");
        }

        // Get the user to update
        User user = userRepository.findByNtid(ntid)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Update NTID if provided (and different)
        // Note: NTID is the primary key, so we need to handle this carefully
        if (updateDTO.getNtid() != null && !updateDTO.getNtid().trim().isEmpty() && 
            !updateDTO.getNtid().trim().toLowerCase().equals(user.getNtid().toLowerCase())) {
            String newNtid = updateDTO.getNtid().trim().toLowerCase();
            
            // Check if new NTID already exists
            if (userRepository.existsByNtid(newNtid)) {
                throw new RuntimeException("User with this NTID already exists");
            }
            
            // Since NTID is the primary key, we need to create a new user with the new NTID
            // and delete the old one. This is a complex operation.
            System.out.println("  [UserService] NTID change requested from " + user.getNtid() + " to " + newNtid);
            System.out.println("  [UserService] Creating new user with new NTID...");
            
            // Create new user with new NTID
            User newUser = new User();
            newUser.setNtid(newNtid);
            newUser.setEmail(user.getEmail());
            newUser.setAccount(user.getAccount());
            newUser.setAccountId(user.getAccountId());
            newUser.setRole(user.getRole());
            newUser.setActive(user.getActive());
            newUser.setPassword(user.getPassword()); // Keep the same password
            newUser.setCreatedAt(user.getCreatedAt()); // Keep original creation date
            
            // Save new user first
            User savedNewUser = userRepository.save(newUser);
            
            // Delete old user
            userRepository.deleteById(user.getNtid());
            
            System.out.println("  [UserService] NTID updated successfully");
            // Update the user reference to the new user
            user = savedNewUser;
        }

        // Update email if provided (and different)
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().trim().isEmpty() && 
            !updateDTO.getEmail().trim().equalsIgnoreCase(user.getEmail())) {
            String newEmail = updateDTO.getEmail().trim();
            
            // Validate email format
            if (!newEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new RuntimeException("Invalid email format");
            }
            
            // Check if new email already exists
            if (userRepository.existsByEmail(newEmail)) {
                throw new RuntimeException("User with this email already exists");
            }
            
            System.out.println("  [UserService] Updating email from " + user.getEmail() + " to " + newEmail);
            user.setEmail(newEmail);
        }

        // Update account if provided
        if (updateDTO.getAccount() != null && !updateDTO.getAccount().trim().isEmpty()) {
            String accountName = updateDTO.getAccount().trim();
            
            // Look up account and get accountId
            Account account = accountRepository.findByAccountName(accountName)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountName));
            
            System.out.println("  [UserService] Updating account to " + accountName + " (ID: " + account.getAccountId() + ")");
            user.setAccount(accountName);
            user.setAccountId(account.getAccountId());
        } else if (updateDTO.getAccountId() != null) {
            // If accountId is provided directly, look up the account name
            Account account = accountRepository.findById(updateDTO.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + updateDTO.getAccountId()));
            
            System.out.println("  [UserService] Updating account to " + account.getAccountName() + " (ID: " + updateDTO.getAccountId() + ")");
            user.setAccount(account.getAccountName());
            user.setAccountId(updateDTO.getAccountId());
        }

        // Update role if provided (only ADMIN can change roles)
        if (updateDTO.getRole() != null && updateDTO.getRole() != user.getRole()) {
            if (requester.getRole() != UserRole.ADMIN) {
                throw new RuntimeException("Only ADMIN can update user roles");
            }
            System.out.println("  [UserService] Updating role from " + user.getRole() + " to " + updateDTO.getRole());
            user.setRole(updateDTO.getRole());
        }

        // Update active status if provided
        if (updateDTO.getActive() != null && updateDTO.getActive() != user.getActive()) {
            System.out.println("  [UserService] Updating active status from " + user.getActive() + " to " + updateDTO.getActive());
            user.setActive(updateDTO.getActive());
        }

        System.out.println("  [UserService] Saving updated user...");
        // If NTID was changed, user is already the new user, otherwise save the updated user
        User updatedUser = user;
        if (updateDTO.getNtid() == null || 
            updateDTO.getNtid().trim().toLowerCase().equals(user.getNtid().toLowerCase())) {
            // NTID wasn't changed, so save normally
            updatedUser = userRepository.save(user);
        }
        // If NTID was changed, the new user was already saved above
        
        System.out.println("  [UserService] User updated successfully");
        
        return updatedUser;
    }
}
