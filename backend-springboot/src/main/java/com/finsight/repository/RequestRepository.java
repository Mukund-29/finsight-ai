package com.finsight.repository;

import com.finsight.entity.Request;
import com.finsight.entity.RequestPriority;
import com.finsight.entity.RequestStatus;
import com.finsight.entity.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request Repository
 * 
 * @author Mukund Kute
 */
@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    // Find by created by
    List<Request> findByCreatedByAndActiveTrueOrderByCreatedAtDesc(String createdBy);

    // Find by assigned to (case-insensitive for Oracle)
    @Query("SELECT r FROM Request r WHERE UPPER(r.assignedTo) = UPPER(:assignedTo) AND r.active = true ORDER BY r.createdAt DESC")
    List<Request> findByAssignedToAndActiveTrueOrderByCreatedAtDesc(@Param("assignedTo") String assignedTo);
    
    // Find all assigned tickets (assignedTo is not null) regardless of status
    @Query("SELECT r FROM Request r WHERE r.assignedTo IS NOT NULL AND r.active = true ORDER BY r.createdAt DESC")
    List<Request> findAllAssignedTicketsOrderByCreatedAtDesc();

    // Find by status
    List<Request> findByStatusAndActiveTrueOrderByCreatedAtDesc(RequestStatus status);

    // Find by account
    List<Request> findByAccountIdAndActiveTrueOrderByCreatedAtDesc(Long accountId);

    // Find by priority
    List<Request> findByPriorityAndActiveTrueOrderByCreatedAtDesc(RequestPriority priority);

    // Find by request type
    List<Request> findByRequestTypeAndActiveTrueOrderByCreatedAtDesc(RequestType requestType);

    // Find active requests
    List<Request> findByActiveTrueOrderByCreatedAtDesc();

    // Find requests approaching ETA (within threshold minutes)
    @Query("SELECT r FROM Request r WHERE r.active = true AND r.eta IS NOT NULL AND r.eta <= :thresholdTime AND r.status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY r.eta ASC")
    List<Request> findRequestsApproachingEta(@Param("thresholdTime") LocalDateTime thresholdTime);

    // Find requests by multiple filters
    @Query("SELECT r FROM Request r WHERE r.active = true " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:priority IS NULL OR r.priority = :priority) " +
           "AND (:requestType IS NULL OR r.requestType = :requestType) " +
           "AND (:accountId IS NULL OR r.accountId = :accountId) " +
           "ORDER BY r.createdAt DESC")
    List<Request> findWithFilters(
        @Param("status") RequestStatus status,
        @Param("priority") RequestPriority priority,
        @Param("requestType") RequestType requestType,
        @Param("accountId") Long accountId
    );

    // Find requests by multiple account IDs (for SCRUM_MASTER)
    @Query("SELECT r FROM Request r WHERE r.active = true " +
           "AND r.accountId IN :accountIds " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:priority IS NULL OR r.priority = :priority) " +
           "AND (:requestType IS NULL OR r.requestType = :requestType) " +
           "ORDER BY r.createdAt DESC")
    List<Request> findByAccountIds(
        @Param("accountIds") List<Long> accountIds,
        @Param("status") RequestStatus status,
        @Param("priority") RequestPriority priority,
        @Param("requestType") RequestType requestType
    );

    // Count by status
    long countByStatusAndActiveTrue(RequestStatus status);

    // Count by assigned to
    long countByAssignedToAndActiveTrue(String assignedTo);
}
