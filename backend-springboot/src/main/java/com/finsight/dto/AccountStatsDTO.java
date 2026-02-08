package com.finsight.dto;

/**
 * Account Statistics DTO
 * 
 * @author Mukund Kute
 */
public class AccountStatsDTO {
    private Long accountId;
    private String accountName;
    private Long openTickets;
    private Long totalTickets;
    private Long resolvedTickets;
    private Long pendingTickets;
    private Long onHoldTickets;
    private Long crossedEtaTickets;

    // Constructors
    public AccountStatsDTO() {
    }

    public AccountStatsDTO(Long accountId, String accountName, Long openTickets) {
        this.accountId = accountId;
        this.accountName = accountName;
        this.openTickets = openTickets;
        this.totalTickets = 0L;
        this.resolvedTickets = 0L;
        this.pendingTickets = 0L;
        this.onHoldTickets = 0L;
        this.crossedEtaTickets = 0L;
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

    public Long getOpenTickets() {
        return openTickets;
    }

    public void setOpenTickets(Long openTickets) {
        this.openTickets = openTickets;
    }

    public Long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(Long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public Long getResolvedTickets() {
        return resolvedTickets;
    }

    public void setResolvedTickets(Long resolvedTickets) {
        this.resolvedTickets = resolvedTickets;
    }

    public Long getPendingTickets() {
        return pendingTickets;
    }

    public void setPendingTickets(Long pendingTickets) {
        this.pendingTickets = pendingTickets;
    }

    public Long getOnHoldTickets() {
        return onHoldTickets;
    }

    public void setOnHoldTickets(Long onHoldTickets) {
        this.onHoldTickets = onHoldTickets;
    }

    public Long getCrossedEtaTickets() {
        return crossedEtaTickets;
    }

    public void setCrossedEtaTickets(Long crossedEtaTickets) {
        this.crossedEtaTickets = crossedEtaTickets;
    }
}
