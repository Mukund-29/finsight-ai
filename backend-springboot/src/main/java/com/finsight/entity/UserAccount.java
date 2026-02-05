package com.finsight.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * User Account Junction Entity
 * Represents many-to-many relationship between Users and Accounts
 * Used for SCRUM_MASTER to handle multiple accounts
 * 
 * @author Mukund Kute
 */
@Entity
@Table(name = "FLOWAI_USER_ACCOUNTS", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ntid", "accountId"})
})
public class UserAccount {

    @Id
    @Column(name = "USER_ACCOUNT_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_account_seq")
    @SequenceGenerator(name = "user_account_seq", sequenceName = "FLOWAI_USER_ACCOUNTS_SEQ", allocationSize = 1)
    private Long userAccountId;

    @Column(name = "NTID", nullable = false, length = 50)
    private String ntid;

    @Column(name = "ACCOUNT_ID", nullable = false)
    private Long accountId;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "ACTIVE", nullable = false)
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
    }

    // Constructors
    public UserAccount() {
    }

    public UserAccount(String ntid, Long accountId) {
        this.ntid = ntid;
        this.accountId = accountId;
        this.active = true;
    }

    // Getters and Setters
    public Long getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(Long userAccountId) {
        this.userAccountId = userAccountId;
    }

    public String getNtid() {
        return ntid;
    }

    public void setNtid(String ntid) {
        this.ntid = ntid;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
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
