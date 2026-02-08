package com.finsight.dto;

import com.finsight.entity.UserRole;

/**
 * DTO for updating user details
 * Note: Password cannot be updated through this DTO
 * 
 * @author Mukund Kute
 */
public class UpdateUserDTO {
    private String ntid;
    private String email;
    private String account;
    private Long accountId;
    private UserRole role;
    private Boolean active;

    // Constructors
    public UpdateUserDTO() {
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

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
