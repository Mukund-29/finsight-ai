package com.finsight.repository;

import com.finsight.entity.RequestComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Request Comment Repository
 * 
 * @author Mukund Kute
 */
@Repository
public interface RequestCommentRepository extends JpaRepository<RequestComment, Long> {
    
    // Find all active comments for a request, ordered by date (oldest first)
    @Query("SELECT c FROM RequestComment c WHERE c.requestId = :requestId AND c.active = true ORDER BY c.commentedAt ASC")
    List<RequestComment> findByRequestIdAndActiveTrueOrderByCommentedAtAsc(@Param("requestId") Long requestId);
    
    // Find all comments for a request (including inactive)
    List<RequestComment> findByRequestIdOrderByCommentedAtAsc(Long requestId);
}
