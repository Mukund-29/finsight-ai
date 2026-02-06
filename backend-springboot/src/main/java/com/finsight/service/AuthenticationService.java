package com.finsight.service;

import com.finsight.dto.AuthResponseDTO;
import com.finsight.dto.LoginRequestDTO;
import com.finsight.entity.User;
import com.finsight.entity.UserRole;
import com.finsight.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication Service
 * 
 * @author Mukund Kute
 */
@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Authenticate user
     */
    public AuthResponseDTO authenticate(LoginRequestDTO loginRequest) {
        System.out.println("  [AuthenticationService] authenticate() called");
        String ntid = loginRequest.getNtid().toLowerCase();
        // Password validation can be added here in the future
        System.out.println("  [AuthenticationService] Authenticating NTID: " + ntid);

        // Check database users
        System.out.println("  [AuthenticationService] Checking database for user...");
        User user = userRepository.findByNtid(ntid)
            .orElseThrow(() -> {
                System.out.println("  [AuthenticationService] ERROR: User not found in database");
                return new RuntimeException("User not found");
            });

        System.out.println("  [AuthenticationService] User found in database:");
        System.out.println("    - NTID: " + user.getNtid());
        System.out.println("    - Email: " + user.getEmail());
        System.out.println("    - Role: " + user.getRole());
        System.out.println("    - Active: " + user.getActive());

        if (!user.getActive()) {
            System.out.println("  [AuthenticationService] ERROR: User account is inactive");
            throw new RuntimeException("User account is inactive");
        }

        System.out.println("  [AuthenticationService] User account is active");
        
        // Validate password
        String providedPassword = loginRequest.getPassword();
        String storedPassword = user.getPassword();
        
        if (storedPassword == null || storedPassword.isEmpty()) {
            // Legacy users without password - use NTID as password
            if (!ntid.equals(providedPassword)) {
                System.out.println("  [AuthenticationService] ERROR: Invalid password");
                throw new RuntimeException("Invalid credentials");
            }
        } else {
            // Check if password is hashed (starts with $2a$, $2b$, or $2y$ for BCrypt)
            if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
                // Password is hashed - use BCrypt to verify
                if (!passwordEncoder.matches(providedPassword, storedPassword)) {
                    System.out.println("  [AuthenticationService] ERROR: Invalid password (hashed)");
                    throw new RuntimeException("Invalid credentials");
                }
            } else {
                // Legacy plain text password - verify directly, then update to hashed
                if (!storedPassword.equals(providedPassword)) {
                    System.out.println("  [AuthenticationService] ERROR: Invalid password (plain text)");
                    throw new RuntimeException("Invalid credentials");
                }
                // Migrate to hashed password
                System.out.println("  [AuthenticationService] Migrating plain text password to hashed");
                updatePasswordToHashed(user, providedPassword);
            }
        }
        
        System.out.println("  [AuthenticationService] Password validated");
        System.out.println("  [AuthenticationService] User authentication SUCCESS");
        
        return new AuthResponseDTO(
            user.getNtid(),
            user.getEmail(),
            user.getRole(),
            generateToken(user.getNtid(), user.getRole()),
            "Authentication successful"
        );
    }

    /**
     * Update user password from plain text to hashed (migration helper)
     */
    @Transactional
    private void updatePasswordToHashed(User user, String plainPassword) {
        String hashedPassword = passwordEncoder.encode(plainPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);
        System.out.println("  [AuthenticationService] Password migrated to hashed format");
    }

    /**
     * Simple token generation (can be enhanced with JWT later)
     */
    private String generateToken(String ntid, UserRole role) {
        // Simple token for now - can be replaced with JWT
        return "token_" + ntid + "_" + role.name() + "_" + System.currentTimeMillis();
    }
}
