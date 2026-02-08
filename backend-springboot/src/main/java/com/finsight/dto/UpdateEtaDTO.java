package com.finsight.dto;

import java.time.LocalDateTime;

/**
 * Update ETA DTO
 * Includes new ETA and reason for change
 * 
 * @author Mukund Kute
 */
public class UpdateEtaDTO {
    private LocalDateTime newEta;
    private String changeReason;
    private String commentText;

    // Constructors
    public UpdateEtaDTO() {
    }

    public UpdateEtaDTO(LocalDateTime newEta, String changeReason, String commentText) {
        this.newEta = newEta;
        this.changeReason = changeReason;
        this.commentText = commentText;
    }

    // Getters and Setters
    public LocalDateTime getNewEta() {
        return newEta;
    }

    public void setNewEta(LocalDateTime newEta) {
        this.newEta = newEta;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }
}
