package com.finsight.service;

import com.finsight.dto.UserRegistrationDTO;
import com.finsight.entity.Account;
import com.finsight.entity.User;
import com.finsight.entity.UserRole;
import com.finsight.repository.AccountRepository;
import com.finsight.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

        // Create new user with USER role
        System.out.println("  [UserService] Creating new User object...");
        User user = new User(
            ntid,
            registrationDTO.getEmail(),
            accountName,
            accountId,
            registrationDTO.getPassword()
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

        // Create admin user
        User user = new User(
            ntid,
            registrationDTO.getEmail(),
            accountName,
            accountId,
            registrationDTO.getPassword()
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
}
