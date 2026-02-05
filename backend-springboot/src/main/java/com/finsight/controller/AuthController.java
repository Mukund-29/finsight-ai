package com.finsight.controller;

import com.finsight.dto.AuthResponseDTO;
import com.finsight.dto.LoginRequestDTO;
import com.finsight.dto.UserRegistrationDTO;
import com.finsight.entity.User;
import com.finsight.service.AuthenticationService;
import com.finsight.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller
 * 
 * @author Mukund Kute
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * User Registration - Creates user when raising ticket
     * Handles both application/json and text/plain content types
     */
    @PostMapping(value = "/register", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<?> registerUser(@RequestBody String requestBody) {
        System.out.println("\n=========================================");
        System.out.println("API CALLED: POST /api/auth/register");
        System.out.println("Request Body (Raw): " + requestBody);
        
        try {
            // Parse JSON from request body (handles both application/json and text/plain)
            UserRegistrationDTO registrationDTO = objectMapper.readValue(requestBody, UserRegistrationDTO.class);
            
            System.out.println("Parsed Registration Data:");
            System.out.println("  - NTID: " + registrationDTO.getNtid());
            System.out.println("  - Email: " + registrationDTO.getEmail());
            System.out.println("  - Account: " + registrationDTO.getAccount());
            System.out.println("  - Password: ***");
            
            // Validate required fields
            if (registrationDTO.getNtid() == null || registrationDTO.getNtid().trim().isEmpty()) {
                System.out.println("ERROR: NTID is required");
                Map<String, String> error = new HashMap<>();
                error.put("error", "NTID is required");
                System.out.println("Response: " + error);
                System.out.println("=========================================\n");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            if (registrationDTO.getEmail() == null || registrationDTO.getEmail().trim().isEmpty()) {
                System.out.println("ERROR: Email is required");
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email is required");
                System.out.println("Response: " + error);
                System.out.println("=========================================\n");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            if (registrationDTO.getPassword() == null || registrationDTO.getPassword().trim().isEmpty()) {
                System.out.println("ERROR: Password is required");
                Map<String, String> error = new HashMap<>();
                error.put("error", "Password is required");
                System.out.println("Response: " + error);
                System.out.println("=========================================\n");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            if (registrationDTO.getAccount() == null || registrationDTO.getAccount().trim().isEmpty()) {
                System.out.println("ERROR: Account is required");
                Map<String, String> error = new HashMap<>();
                error.put("error", "Account is required");
                System.out.println("Response: " + error);
                System.out.println("=========================================\n");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            System.out.println("Calling UserService.registerUser()...");
            User user = userService.registerUser(registrationDTO);
            
            System.out.println("User Registration SUCCESS:");
            System.out.println("  - Created User NTID: " + user.getNtid());
            System.out.println("  - Created User Email: " + user.getEmail());
            System.out.println("  - Created User Role: " + user.getRole());
            System.out.println("  - Created User Account: " + user.getAccount());
            System.out.println("  - Created User Account ID: " + user.getAccountId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("ntid", user.getNtid());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("account", user.getAccount());
            response.put("accountId", user.getAccountId());
            
            System.out.println("Response: " + response);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            System.out.println("ERROR: JSON Parsing Failed - " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid JSON format: " + e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (RuntimeException e) {
            System.out.println("ERROR: Registration Failed - " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            System.out.println("ERROR: Unexpected Exception - " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Registration failed: " + e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * User Login
     * Handles both application/json and text/plain content types
     */
    @PostMapping(value = "/login", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<?> login(@RequestBody String requestBody) {
        System.out.println("\n=========================================");
        System.out.println("API CALLED: POST /api/auth/login");
        System.out.println("Request Body (Raw): " + requestBody);
        
        try {
            // Parse JSON from request body (handles both application/json and text/plain)
            LoginRequestDTO loginRequest = objectMapper.readValue(requestBody, LoginRequestDTO.class);
            
            System.out.println("Parsed Login Data:");
            System.out.println("  - NTID: " + loginRequest.getNtid());
            System.out.println("  - Password: " + (loginRequest.getPassword() != null ? "***" : "null"));
            
            // Validate required fields
            if (loginRequest.getNtid() == null || loginRequest.getNtid().trim().isEmpty()) {
                System.out.println("ERROR: NTID is required");
                Map<String, String> error = new HashMap<>();
                error.put("error", "NTID is required");
                System.out.println("Response: " + error);
                System.out.println("=========================================\n");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                System.out.println("ERROR: Password is required");
                Map<String, String> error = new HashMap<>();
                error.put("error", "Password is required");
                System.out.println("Response: " + error);
                System.out.println("=========================================\n");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            System.out.println("Calling AuthenticationService.authenticate()...");
            AuthResponseDTO response = authenticationService.authenticate(loginRequest);
            
            System.out.println("Login SUCCESS:");
            System.out.println("  - Authenticated NTID: " + response.getNtid());
            System.out.println("  - Authenticated Email: " + response.getEmail());
            System.out.println("  - Authenticated Role: " + response.getRole());
            System.out.println("  - Token: " + response.getToken());
            System.out.println("Response: " + response);
            System.out.println("=========================================\n");
            return ResponseEntity.ok(response);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            System.out.println("ERROR: JSON Parsing Failed - " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid JSON format: " + e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (RuntimeException e) {
            System.out.println("ERROR: Authentication Failed - " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            System.out.println("ERROR: Unexpected Exception - " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Login failed: " + e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get current user info
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "X-User-NTID", required = false) String ntid) {
        System.out.println("\n=========================================");
        System.out.println("API CALLED: GET /api/auth/me");
        System.out.println("Request Header - X-User-NTID: " + ntid);
        
        if (ntid == null || ntid.isEmpty()) {
            System.out.println("ERROR: NTID not provided in header");
            Map<String, String> error = new HashMap<>();
            error.put("error", "NTID not provided");
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        try {
            System.out.println("Calling UserService.getUserByNtid() for NTID: " + ntid);
            User user = userService.getUserByNtid(ntid);
            
            System.out.println("User Found:");
            System.out.println("  - NTID: " + user.getNtid());
            System.out.println("  - Email: " + user.getEmail());
            System.out.println("  - Role: " + user.getRole());
            System.out.println("  - Account: " + user.getAccount());
            System.out.println("  - Account ID: " + user.getAccountId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("ntid", user.getNtid());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("account", user.getAccount());
            response.put("accountId", user.getAccountId());
            
            System.out.println("Response: " + response);
            System.out.println("=========================================\n");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.out.println("ERROR: User Not Found - " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
