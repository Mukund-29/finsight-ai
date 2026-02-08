package com.finsight.dto;

import java.time.LocalDateTime;

/**
 * Comment DTO
 * 
 * @author Mukund Kute
 */
public class CommentDTO {
    private Long commentId;
    private Long requestId;
    private String commentText;
    private String commentedBy;
    private LocalDateTime commentedAt;
    private Boolean isEtaChange;
    private LocalDateTime oldEta;
    private LocalDateTime newEta;
    private String changeReason;

    // Constructors
    public CommentDTO() {
    }

    public CommentDTO(Long commentId, Long requestId, String commentText, String commentedBy, 
                     LocalDateTime commentedAt, Boolean isEtaChange, LocalDateTime oldEta, 
                     LocalDateTime newEta, String changeReason) {
        this.commentId = commentId;
        this.requestId = requestId;
        this.commentText = commentText;
        this.commentedBy = commentedBy;
        this.commentedAt = commentedAt;
        this.isEtaChange = isEtaChange;
        this.oldEta = oldEta;
        this.newEta = newEta;
        this.changeReason = changeReason;
    }

    // Getters and Setters
    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getCommentedBy() {
        return commentedBy;
    }

    public void setCommentedBy(String commentedBy) {
        this.commentedBy = commentedBy;
    }

    public LocalDateTime getCommentedAt() {
        return commentedAt;
    }

    public void setCommentedAt(LocalDateTime commentedAt) {
        this.commentedAt = commentedAt;
    }

    public Boolean getIsEtaChange() {
        return isEtaChange;
    }

    public void setIsEtaChange(Boolean isEtaChange) {
        this.isEtaChange = isEtaChange;
    }

    public LocalDateTime getOldEta() {
        return oldEta;
    }

    public void setOldEta(LocalDateTime oldEta) {
        this.oldEta = oldEta;
    }

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
}
