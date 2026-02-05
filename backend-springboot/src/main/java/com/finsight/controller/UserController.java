package com.finsight.controller;

import com.finsight.dto.UserRegistrationDTO;
import com.finsight.entity.User;
import com.finsight.entity.UserRole;
import com.finsight.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User Controller
 * 
 * @author Mukund Kute
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Get user by NTID
     */
    @GetMapping("/{ntid}")
    public ResponseEntity<?> getUserByNtid(
            @PathVariable String ntid,
            @RequestHeader(value = "X-User-NTID", required = false) String requestedBy) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: GET /api/users/" + ntid);
        System.out.println("Request Header - X-User-NTID: " + requestedBy);
        
        try {
            if (requestedBy == null || requestedBy.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            User user = userService.getUserByNtid(ntid);
            
            Map<String, Object> response = new HashMap<>();
            response.put("ntid", user.getNtid());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("account", user.getAccount());
            response.put("accountId", user.getAccountId());
            response.put("active", user.getActive());
            
            System.out.println("Response: " + response);
            System.out.println("=========================================\n");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Delete user (ADMIN only)
     */
    @DeleteMapping("/{ntid}")
    public ResponseEntity<?> deleteUser(
            @PathVariable String ntid,
            @RequestHeader(value = "X-User-NTID", required = false) String requestedBy) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: DELETE /api/users/" + ntid);
        System.out.println("Request Header - X-User-NTID: " + requestedBy);
        
        try {
            if (requestedBy == null || requestedBy.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            userService.deleteUser(ntid, requestedBy);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            
            System.out.println("Response: " + response);
            System.out.println("=========================================\n");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Update user role (ADMIN only)
     */
    @PutMapping("/{ntid}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable String ntid,
            @RequestParam UserRole role,
            @RequestHeader(value = "X-User-NTID", required = false) String requestedBy) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: PUT /api/users/" + ntid + "/role");
        System.out.println("Request Header - X-User-NTID: " + requestedBy);
        System.out.println("New Role: " + role);
        
        try {
            if (requestedBy == null || requestedBy.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            User user = userService.updateUserRole(ntid, role, requestedBy);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User role updated successfully");
            response.put("ntid", user.getNtid());
            response.put("role", user.getRole());
            
            System.out.println("Response: " + response);
            System.out.println("=========================================\n");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get all users (for assignment dropdown)
     * Returns list of users that can be assigned tickets (DEVELOPER, ADMIN, or all users based on role)
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) UserRole role,
            @RequestHeader(value = "X-User-NTID", required = false) String requestedBy) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: GET /api/users");
        System.out.println("Request Header - X-User-NTID: " + requestedBy);
        System.out.println("Filter by role: " + role);
        
        try {
            if (requestedBy == null || requestedBy.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Get requesting user to check permissions
            User requester = userService.getUserByNtid(requestedBy);
            
            // Only SCRUM_MASTER and ADMIN can see user list for assignment
            if (requester.getRole() != UserRole.SCRUM_MASTER && requester.getRole() != UserRole.ADMIN) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only SCRUM_MASTER or ADMIN can view user list");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Get all active users, filter by role if specified
            List<User> allUsers = userService.getAllActiveUsers();
            
            List<User> filteredUsers = allUsers;
            if (role != null) {
                // If role filter is specified, filter by that role
                filteredUsers = allUsers.stream()
                    .filter(u -> u.getRole() == role)
                    .collect(Collectors.toList());
            } else {
                // No role filter: return ALL active users (for assignment dropdown)
                // SCRUM_MASTER and ADMIN can assign tickets to any user
                filteredUsers = allUsers;
            }
            
            List<Map<String, Object>> responseList = filteredUsers.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("ntid", user.getNtid());
                    userMap.put("email", user.getEmail());
                    userMap.put("role", user.getRole());
                    userMap.put("account", user.getAccount());
                    userMap.put("accountId", user.getAccountId());
                    return userMap;
                })
                .collect(Collectors.toList());
            
            System.out.println("Found " + responseList.size() + " users");
            System.out.println("=========================================\n");
            return ResponseEntity.ok(responseList);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Create admin user (ADMIN only)
     */
    @PostMapping("/admin")
    public ResponseEntity<?> createAdminUser(
            @RequestBody UserRegistrationDTO registrationDTO,
            @RequestHeader(value = "X-User-NTID", required = false) String requestedBy) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: POST /api/users/admin");
        System.out.println("Request Header - X-User-NTID: " + requestedBy);
        
        System.out.println("Admin Registration Data:");
        System.out.println("  - NTID: " + registrationDTO.getNtid());
        System.out.println("  - Email: " + registrationDTO.getEmail());
        System.out.println("  - Account: " + registrationDTO.getAccount());
        System.out.println("  - Password: ***");
        
        try {
            if (requestedBy == null || requestedBy.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            User admin = userService.createAdminUser(registrationDTO, requestedBy);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Admin user created successfully");
            response.put("ntid", admin.getNtid());
            response.put("email", admin.getEmail());
            response.put("role", admin.getRole());
            
            System.out.println("Response: " + response);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}
