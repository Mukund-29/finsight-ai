package com.finsight.entity;

/**
 * Request Status Enumeration
 * 
 * @author Mukund Kute
 */
public enum RequestStatus {
    OPEN,           // Request is in open queue, not assigned
    ASSIGNED,       // Request is assigned to a developer
    IN_PROGRESS,    // Developer is working on it
    ON_HOLD,        // Request is on hold
    COMPLETED,      // Request is completed
    DELAYED,        // Request is delayed
    CANCELLED       // Request is cancelled
}
