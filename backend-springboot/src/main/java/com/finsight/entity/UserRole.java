package com.finsight.entity;

/**
 * User Role Enumeration
 * 
 * @author Mukund Kute
 */
public enum UserRole {
    USER,          // Basic user - can create tickets and view own requests
    SCRUM_MASTER,  // Can assign tickets to developers
    DEVELOPER,     // Can receive ticket assignments and mark tasks as completed/hold/delayed
    MANAGER,       // Manager - can view team tickets and reports
    VIEWER,        // Can pull/view records and generate reports
    ADMIN          // Admin user - can manage users, requests, and has all permissions
}
