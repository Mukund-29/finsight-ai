package com.finsight.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Assign Account DTO
 * 
 * @author Mukund Kute
 */
public class AssignAccountDTO {

    @NotBlank(message = "NTID is required")
    private String ntid;

    @NotNull(message = "Account ID is required")
    private Long accountId;

    // Constructors
    public AssignAccountDTO() {
    }

    public AssignAccountDTO(String ntid, Long accountId) {
        this.ntid = ntid;
        this.accountId = accountId;
    }

    // Getters and Setters
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
}
