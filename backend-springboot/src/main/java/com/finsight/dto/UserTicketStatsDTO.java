package com.finsight.dto;

/**
 * DTO for user ticket statistics
 * 
 * @author Mukund Kute
 */
public class UserTicketStatsDTO {
    private String ntid;
    private String email;
    private String role;
    private Long totalTickets;
    private Long resolvedTickets;
    private Long pendingTickets;
    private Long onHoldTickets;
    private Long unresolvedTickets;
    private Long crossedEtaTickets;

    // Constructors
    public UserTicketStatsDTO() {
    }

    public UserTicketStatsDTO(String ntid, String email, String role) {
        this.ntid = ntid;
        this.email = email;
        this.role = role;
        this.totalTickets = 0L;
        this.resolvedTickets = 0L;
        this.pendingTickets = 0L;
        this.onHoldTickets = 0L;
        this.unresolvedTickets = 0L;
        this.crossedEtaTickets = 0L;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public Long getUnresolvedTickets() {
        return unresolvedTickets;
    }

    public void setUnresolvedTickets(Long unresolvedTickets) {
        this.unresolvedTickets = unresolvedTickets;
    }

    public Long getCrossedEtaTickets() {
        return crossedEtaTickets;
    }

    public void setCrossedEtaTickets(Long crossedEtaTickets) {
        this.crossedEtaTickets = crossedEtaTickets;
    }
}
