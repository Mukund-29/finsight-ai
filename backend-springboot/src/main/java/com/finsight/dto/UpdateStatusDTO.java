package com.finsight.dto;

import com.finsight.entity.RequestStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Update Status DTO
 * 
 * @author Mukund Kute
 */
public class UpdateStatusDTO {

    @NotNull(message = "Status is required")
    private RequestStatus status;

    private String comment;

    // Constructors
    public UpdateStatusDTO() {
    }

    public UpdateStatusDTO(RequestStatus status, String comment) {
        this.status = status;
        this.comment = comment;
    }

    // Getters and Setters
    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
