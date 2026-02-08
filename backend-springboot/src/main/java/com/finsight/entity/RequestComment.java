package com.finsight.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Request Comment Entity
 * Stores comments on tickets, including ETA change comments
 * 
 * @author Mukund Kute
 */
@Entity
@Table(name = "FLOWAI_REQUEST_COMMENTS")
public class RequestComment {

    @Id
    @Column(name = "COMMENT_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_seq")
    @SequenceGenerator(name = "comment_seq", sequenceName = "FLOWAI_REQUEST_COMMENTS_SEQ", allocationSize = 1)
    private Long commentId;

    @Column(name = "REQUEST_ID", nullable = false)
    private Long requestId;

    @Column(name = "COMMENT_TEXT", columnDefinition = "CLOB", nullable = false)
    private String commentText;

    @Column(name = "COMMENTED_BY", nullable = false, length = 50)
    private String commentedBy;

    @Column(name = "COMMENTED_AT", nullable = false)
    private LocalDateTime commentedAt;

    @Column(name = "IS_ETA_CHANGE", nullable = false)
    private Boolean isEtaChange = false;

    @Column(name = "OLD_ETA")
    private LocalDateTime oldEta;

    @Column(name = "NEW_ETA")
    private LocalDateTime newEta;

    @Column(name = "CHANGE_REASON", length = 500)
    private String changeReason;

    @Column(name = "ACTIVE", nullable = false)
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        if (commentedAt == null) {
            commentedAt = LocalDateTime.now();
        }
        if (isEtaChange == null) {
            isEtaChange = false;
        }
        if (active == null) {
            active = true;
        }
    }

    // Constructors
    public RequestComment() {
    }

    public RequestComment(Long requestId, String commentText, String commentedBy) {
        this.requestId = requestId;
        this.commentText = commentText;
        this.commentedBy = commentedBy;
        this.isEtaChange = false;
        this.active = true;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
