package com.finsight.controller;

import com.finsight.dto.UserRegistrationDTO;
import com.finsight.entity.User;
import com.finsight.entity.UserRole;
import com.finsight.repository.UserAccountRepository;
import com.finsight.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @Autowired
    private UserAccountRepository userAccountRepository;

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
     * If accountId is provided, filters users to those belonging to that account (direct or via junction table)
     * Also includes the user specified by createdByNtid if provided
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String createdByNtid,
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

            // Get all users (both active and inactive) for user management, filter by role if specified
            List<User> allUsers = userService.getAllUsers();
            
            List<User> filteredUsers = allUsers;
            
            // Filter by accountId if provided (for ticket assignment)
            if (accountId != null) {
                System.out.println("Filtering users by accountId: " + accountId);
                Set<String> eligibleNtids = new HashSet<>();
                
                // 1. Add users with direct accountId match
                allUsers.stream()
                    .filter(u -> u.getAccountId() != null && u.getAccountId().equals(accountId))
                    .forEach(u -> eligibleNtids.add(u.getNtid()));
                
                // 2. Add users from junction table with this accountId
                List<String> junctionNtids = userAccountRepository.findByAccountIdAndActiveTrue(accountId)
                    .stream()
                    .map(ua -> ua.getNtid())
                    .collect(Collectors.toList());
                eligibleNtids.addAll(junctionNtids);
                
                // 3. Add the user who created the ticket (if provided)
                if (createdByNtid != null && !createdByNtid.trim().isEmpty()) {
                    eligibleNtids.add(createdByNtid.trim());
                }
                
                System.out.println("Eligible users for account " + accountId + ": " + eligibleNtids.size());
                
                // Filter users to only those eligible
                filteredUsers = allUsers.stream()
                    .filter(u -> eligibleNtids.contains(u.getNtid()))
                    .collect(Collectors.toList());
            }
            
            // Apply role filter if specified
            if (role != null) {
                filteredUsers = filteredUsers.stream()
                    .filter(u -> u.getRole() == role)
                    .collect(Collectors.toList());
            }
            
            List<Map<String, Object>> responseList = filteredUsers.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("ntid", user.getNtid());
                    userMap.put("email", user.getEmail());
                    userMap.put("role", user.getRole());
                    userMap.put("account", user.getAccount());
                    userMap.put("accountId", user.getAccountId());
                    userMap.put("active", user.getActive());
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

    /**
     * Update user details (ADMIN and SCRUM_MASTER only)
     * Can update: NTID, email, account, role, active status
     * Cannot update: password
     */
    @PutMapping("/{ntid}")
    public ResponseEntity<?> updateUser(
            @PathVariable String ntid,
            @RequestBody com.finsight.dto.UpdateUserDTO updateDTO,
            @RequestHeader(value = "X-User-NTID", required = false) String requestedBy) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: PUT /api/users/" + ntid);
        System.out.println("Request Header - X-User-NTID: " + requestedBy);
        System.out.println("Update Data:");
        System.out.println("  - NTID: " + updateDTO.getNtid());
        System.out.println("  - Email: " + updateDTO.getEmail());
        System.out.println("  - Account: " + updateDTO.getAccount());
        System.out.println("  - Role: " + updateDTO.getRole());
        System.out.println("  - Active: " + updateDTO.getActive());
        
        try {
            if (requestedBy == null || requestedBy.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            User updatedUser = userService.updateUserDetails(ntid, updateDTO, requestedBy);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User updated successfully");
            response.put("ntid", updatedUser.getNtid());
            response.put("email", updatedUser.getEmail());
            response.put("role", updatedUser.getRole());
            response.put("account", updatedUser.getAccount());
            response.put("accountId", updatedUser.getAccountId());
            response.put("active", updatedUser.getActive());
            
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
}
