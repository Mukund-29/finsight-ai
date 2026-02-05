package com.finsight.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Assign Request DTO
 * 
 * @author Mukund Kute
 */
public class AssignRequestDTO {

    @NotBlank(message = "Assigned to (NTID) is required")
    private String assignedTo;

    @NotNull(message = "ETA is required")
    private LocalDateTime eta;

    // Constructors
    public AssignRequestDTO() {
    }

    public AssignRequestDTO(String assignedTo, LocalDateTime eta) {
        this.assignedTo = assignedTo;
        this.eta = eta;
    }

    // Getters and Setters
    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getEta() {
        return eta;
    }

    public void setEta(LocalDateTime eta) {
        this.eta = eta;
    }
}
