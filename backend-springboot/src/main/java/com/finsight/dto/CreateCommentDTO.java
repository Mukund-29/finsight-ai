package com.finsight.dto;

/**
 * Create Comment DTO
 * 
 * @author Mukund Kute
 */
public class CreateCommentDTO {
    private String commentText;
    private Boolean isEtaChange;
    private String changeReason;

    // Constructors
    public CreateCommentDTO() {
    }

    public CreateCommentDTO(String commentText, Boolean isEtaChange, String changeReason) {
        this.commentText = commentText;
        this.isEtaChange = isEtaChange;
        this.changeReason = changeReason;
    }

    // Getters and Setters
    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Boolean getIsEtaChange() {
        return isEtaChange;
    }

    public void setIsEtaChange(Boolean isEtaChange) {
        this.isEtaChange = isEtaChange;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }
}
