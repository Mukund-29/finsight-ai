package com.finsight.controller;

import com.finsight.dto.AssignAccountDTO;
import com.finsight.entity.UserAccount;
import com.finsight.service.UserAccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User Account Controller
 * 
 * @author Mukund Kute
 */
@RestController
@RequestMapping("/api/user-accounts")
@CrossOrigin(origins = "*")
public class UserAccountController {

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Assign account to SCRUM_MASTER (ADMIN only)
     */
    @PostMapping
    public ResponseEntity<?> assignAccountToUser(
            @RequestBody String requestBody,
            @RequestHeader(value = "X-User-NTID", required = false) String adminNtid) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: POST /api/user-accounts");
        System.out.println("Request Header - X-User-NTID: " + adminNtid);
        
        try {
            if (adminNtid == null || adminNtid.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            AssignAccountDTO assignDTO = objectMapper.readValue(requestBody, AssignAccountDTO.class);
            System.out.println("Assignment Data:");
            System.out.println("  - NTID: " + assignDTO.getNtid());
            System.out.println("  - Account ID: " + assignDTO.getAccountId());

            UserAccount userAccount = userAccountService.assignAccountToUser(assignDTO, adminNtid);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Account assigned successfully");
            response.put("userAccountId", userAccount.getUserAccountId());
            response.put("ntid", userAccount.getNtid());
            response.put("accountId", userAccount.getAccountId());
            
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
     * Get all accounts handled by a user
     */
    @GetMapping("/user/{ntid}")
    public ResponseEntity<?> getAccountsByUser(@PathVariable String ntid) {
        System.out.println("\n=========================================");
        System.out.println("API CALLED: GET /api/user-accounts/user/" + ntid);
        
        try {
            List<UserAccount> userAccounts = userAccountService.getAccountsByUser(ntid);
            
            List<Map<String, Object>> responseList = userAccounts.stream()
                .map(ua -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userAccountId", ua.getUserAccountId());
                    map.put("ntid", ua.getNtid());
                    map.put("accountId", ua.getAccountId());
                    map.put("createdAt", ua.getCreatedAt());
                    return map;
                })
                .toList();
            
            System.out.println("Found " + responseList.size() + " account assignments");
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
     * Get account IDs handled by user (simplified response)
     */
    @GetMapping("/user/{ntid}/account-ids")
    public ResponseEntity<?> getAccountIdsByUser(@PathVariable String ntid) {
        System.out.println("\n=========================================");
        System.out.println("API CALLED: GET /api/user-accounts/user/" + ntid + "/account-ids");
        
        try {
            List<Long> accountIds = userAccountService.getAccountIdsByUser(ntid);
            
            Map<String, Object> response = new HashMap<>();
            response.put("ntid", ntid);
            response.put("accountIds", accountIds);
            response.put("count", accountIds.size());
            
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get all users handling an account
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<?> getUsersByAccount(@PathVariable Long accountId) {
        System.out.println("\n=========================================");
        System.out.println("API CALLED: GET /api/user-accounts/account/" + accountId);
        
        try {
            List<UserAccount> userAccounts = userAccountService.getUsersByAccount(accountId);
            
            List<Map<String, Object>> responseList = userAccounts.stream()
                .map(ua -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("userAccountId", ua.getUserAccountId());
                    map.put("ntid", ua.getNtid());
                    map.put("accountId", ua.getAccountId());
                    map.put("createdAt", ua.getCreatedAt());
                    return map;
                })
                .toList();
            
            System.out.println("Found " + responseList.size() + " users handling this account");
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
     * Remove account assignment (ADMIN only)
     */
    @DeleteMapping("/user/{ntid}/account/{accountId}")
    public ResponseEntity<?> removeAccountAssignment(
            @PathVariable String ntid,
            @PathVariable Long accountId,
            @RequestHeader(value = "X-User-NTID", required = false) String adminNtid) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: DELETE /api/user-accounts/user/" + ntid + "/account/" + accountId);
        
        try {
            if (adminNtid == null || adminNtid.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            userAccountService.removeAccountAssignment(ntid, accountId, adminNtid);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Account assignment removed successfully");
            
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
