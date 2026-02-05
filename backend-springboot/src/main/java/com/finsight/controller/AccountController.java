package com.finsight.controller;

import com.finsight.entity.Account;
import com.finsight.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Account Controller
 * 
 * @author Mukund Kute
 */
@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Test endpoint to check if accounts API is working
     */
    @GetMapping("/test")
    public ResponseEntity<?> testAccounts() {
        System.out.println("\n=========================================");
        System.out.println("API CALLED: GET /api/accounts/test");
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Accounts API is working");
        response.put("timestamp", System.currentTimeMillis());
        
        try {
            long count = accountRepository.count();
            response.put("totalAccounts", count);
            System.out.println("Total accounts in database: " + count);
        } catch (Exception e) {
            response.put("error", "Database error: " + e.getMessage());
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Response: " + response);
        System.out.println("=========================================\n");
        return ResponseEntity.ok(response);
    }

    /**
     * Get all accounts (for dropdown)
     * Returns all accounts from the table, ordered by account name
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveAccounts() {
        System.out.println("\n=========================================");
        System.out.println("API CALLED: GET /api/accounts/active");
        
        try {
            // First, try to get count
            long totalCount = accountRepository.count();
            System.out.println("Total accounts in database: " + totalCount);
            
            // Get all accounts from the table, ordered by account name
            List<Account> accounts = accountRepository.findAllByOrderByAccountNameAsc();
            System.out.println("Retrieved " + accounts.size() + " accounts from repository");
            
            if (accounts.isEmpty()) {
                System.out.println("WARNING: No accounts found in database");
                Map<String, Object> response = new HashMap<>();
                response.put("accounts", new java.util.ArrayList<>());
                response.put("message", "No accounts found");
                response.put("count", 0);
                System.out.println("Response: " + response);
                System.out.println("=========================================\n");
                return ResponseEntity.ok(response);
            }
            
            // Return simplified DTO for frontend
            List<Map<String, Object>> accountList = new java.util.ArrayList<>();
            for (Account account : accounts) {
                Map<String, Object> accountMap = new HashMap<>();
                accountMap.put("accountId", account.getAccountId());
                accountMap.put("accountName", account.getAccountName());
                accountList.add(accountMap);
                System.out.println("  - Account ID: " + account.getAccountId() + ", Name: " + account.getAccountName());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("accounts", accountList);
            response.put("count", accountList.size());
            
            System.out.println("Response: " + response);
            System.out.println("=========================================\n");
            return ResponseEntity.ok(accountList);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch accounts: " + e.getMessage());
            error.put("details", e.getClass().getSimpleName());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Get all accounts (alternative endpoint)
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllAccounts() {
        System.out.println("\n=========================================");
        System.out.println("API CALLED: GET /api/accounts/all");
        
        try {
            List<Account> accounts = accountRepository.findAll();
            System.out.println("Found " + accounts.size() + " accounts using findAll()");
            
            List<Map<String, Object>> accountList = new java.util.ArrayList<>();
            for (Account account : accounts) {
                Map<String, Object> accountMap = new HashMap<>();
                accountMap.put("accountId", account.getAccountId());
                accountMap.put("accountName", account.getAccountName());
                accountMap.put("active", account.getActive());
                accountList.add(accountMap);
            }
            
            System.out.println("Response: " + accountList);
            System.out.println("=========================================\n");
            return ResponseEntity.ok(accountList);
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to fetch accounts: " + e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(500).body(error);
        }
    }
}
