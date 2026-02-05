package com.finsight.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Account Entity
 * 
 * @author Mukund Kute
 */
@Entity
@Table(name = "FLOWAI_ACCOUNTS")
public class Account {

    @Id
    @Column(name = "ACCOUNT_ID")
    private Long accountId;

    @Column(name = "ACCOUNT_NAME", nullable = false, length = 100, unique = true)
    private String accountName;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "ACTIVE", nullable = false)
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Constructors
    public Account() {
    }

    public Account(Long accountId, String accountName) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.active = true;
    }

    // Getters and Setters
    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
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
}
