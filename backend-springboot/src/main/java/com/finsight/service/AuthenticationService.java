package com.finsight.service;

import com.finsight.dto.AuthResponseDTO;
import com.finsight.dto.LoginRequestDTO;
import com.finsight.entity.User;
import com.finsight.entity.UserRole;
import com.finsight.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Authentication Service
 * 
 * @author Mukund Kute
 */
@Service
public class AuthenticationService {

    @Autowired
    private UserRepository userRepository;

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
            // Users with password stored - validate password
            // TODO: Add password hashing (BCrypt) in future
            if (!storedPassword.equals(providedPassword)) {
                System.out.println("  [AuthenticationService] ERROR: Invalid password");
                throw new RuntimeException("Invalid credentials");
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
     * Simple token generation (can be enhanced with JWT later)
     */
    private String generateToken(String ntid, UserRole role) {
        // Simple token for now - can be replaced with JWT
        return "token_" + ntid + "_" + role.name() + "_" + System.currentTimeMillis();
    }
}
