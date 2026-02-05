package com.finsight.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * User Entity
 * 
 * @author Mukund Kute
 */
@Entity
@Table(name = "FLOWAI_USERS", uniqueConstraints = {
    @UniqueConstraint(columnNames = "ntid")
})
public class User {

    @Id
    @Column(name = "NTID", length = 50)
    @NotBlank(message = "NTID is required")
    private String ntid;

    @Column(name = "EMAIL", nullable = false, length = 100)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @Column(name = "ACCOUNT", length = 100)
    private String account;

    @Column(name = "ACCOUNT_ID", nullable = true)
    private Long accountId;

    @Column(name = "PASSWORD", length = 255)
    private String password;

    @Column(name = "ROLE", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "ACTIVE", nullable = false)
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructors
    public User() {
    }

    public User(String ntid, String email, String account, Long accountId, String password) {
        this.ntid = ntid;
        this.email = email;
        this.account = account;
        this.accountId = accountId;
        this.password = password;
        this.role = UserRole.USER;
        this.active = true;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
