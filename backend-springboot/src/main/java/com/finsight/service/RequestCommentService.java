package com.finsight.service;

import com.finsight.dto.CommentDTO;
import com.finsight.dto.CreateCommentDTO;
import com.finsight.entity.RequestComment;
import com.finsight.repository.RequestCommentRepository;
import com.finsight.repository.RequestRepository;
import com.finsight.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Request Comment Service
 * 
 * @author Mukund Kute
 */
@Service
public class RequestCommentService {

    @Autowired
    private RequestCommentRepository commentRepository;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all comments for a request
     */
    public List<CommentDTO> getCommentsByRequestId(Long requestId) {
        List<RequestComment> comments = commentRepository.findByRequestIdAndActiveTrueOrderByCommentedAtAsc(requestId);
        return comments.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Add a comment to a request
     */
    @Transactional
    public CommentDTO addComment(Long requestId, CreateCommentDTO createDTO, String commentedBy) {
        // Verify request exists
        requestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found"));

        // Verify user exists
        userRepository.findByNtid(commentedBy)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // All users can comment on all tickets (view restriction removed)
        // No permission check needed - all authenticated users can comment

        RequestComment comment = new RequestComment();
        comment.setRequestId(requestId);
        comment.setCommentText(createDTO.getCommentText());
        comment.setCommentedBy(commentedBy);
        comment.setIsEtaChange(createDTO.getIsEtaChange() != null && createDTO.getIsEtaChange());
        comment.setChangeReason(createDTO.getChangeReason());
        comment.setActive(true);

        RequestComment savedComment = commentRepository.save(comment);
        return convertToDTO(savedComment);
    }

    /**
     * Add ETA change comment
     */
    @Transactional
    public CommentDTO addEtaChangeComment(Long requestId, LocalDateTime oldEta, LocalDateTime newEta, 
                                         String changeReason, String commentedBy) {
        RequestComment comment = new RequestComment();
        comment.setRequestId(requestId);
        comment.setCommentedBy(commentedBy);
        comment.setIsEtaChange(true);
        comment.setOldEta(oldEta);
        comment.setNewEta(newEta);
        comment.setChangeReason(changeReason);
        
        // Create comment text
        String commentText = "ETA changed";
        if (oldEta != null && newEta != null) {
            commentText = String.format("ETA changed from %s to %s", 
                oldEta.toString(), newEta.toString());
        } else if (newEta != null) {
            commentText = String.format("ETA set to %s", newEta.toString());
        }
        if (changeReason != null && !changeReason.trim().isEmpty()) {
            commentText += ". Reason: " + changeReason;
        }
        comment.setCommentText(commentText);
        comment.setActive(true);

        RequestComment savedComment = commentRepository.save(comment);
        return convertToDTO(savedComment);
    }

    /**
     * Convert entity to DTO
     */
    private CommentDTO convertToDTO(RequestComment comment) {
        return new CommentDTO(
            comment.getCommentId(),
            comment.getRequestId(),
            comment.getCommentText(),
            comment.getCommentedBy(),
            comment.getCommentedAt(),
            comment.getIsEtaChange(),
            comment.getOldEta(),
            comment.getNewEta(),
            comment.getChangeReason()
        );
    }
}
