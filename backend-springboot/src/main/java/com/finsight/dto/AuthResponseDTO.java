package com.finsight.dto;

import com.finsight.entity.UserRole;

/**
 * Authentication Response DTO
 * 
 * @author Mukund Kute
 */
public class AuthResponseDTO {

    private String ntid;
    private String email;
    private UserRole role;
    private String token;
    private String message;

    // Constructors
    public AuthResponseDTO() {
    }

    public AuthResponseDTO(String ntid, String email, UserRole role, String token, String message) {
        this.ntid = ntid;
        this.email = email;
        this.role = role;
        this.token = token;
        this.message = message;
    }

    // Getters and Setters
    public String getNtid() {
        return ntid;
    }

    public void setNtid(String ntid) {
        this.ntid = ntid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
