package com.finsight.service;

import com.finsight.dto.AssignRequestDTO;
import com.finsight.dto.CreateRequestDTO;
import com.finsight.dto.UpdateRequestDTO;
import com.finsight.dto.UpdateStatusDTO;
import com.finsight.entity.Request;
import com.finsight.entity.RequestPriority;
import com.finsight.entity.RequestStatus;
import com.finsight.entity.RequestType;
import com.finsight.entity.User;
import com.finsight.entity.UserRole;
import com.finsight.repository.RequestRepository;
import com.finsight.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Request Service
 * 
 * @author Mukund Kute
 */
@Service
public class RequestService {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.finsight.repository.UserAccountRepository userAccountRepository;

    /**
     * Create new request
     */
    @Transactional
    public Request createRequest(CreateRequestDTO createDTO, String createdBy) {
        System.out.println("  [RequestService] createRequest() called");
        System.out.println("  [RequestService] Created by: " + createdBy);

        // Validate accountId is provided
        if (createDTO.getAccountId() == null) {
            throw new RuntimeException("Account ID is required");
        }

        // Verify user exists
        userRepository.findByNtid(createdBy)
            .orElseThrow(() -> new RuntimeException("User not found: " + createdBy));

        Long accountId = createDTO.getAccountId();

        Request request = new Request(
            createDTO.getTitle(),
            createDTO.getDescription(),
            createDTO.getRequestType(),
            createDTO.getPriority(),
            createdBy,
            accountId
        );

        Request savedRequest = requestRepository.save(request);
        System.out.println("  [RequestService] Request created with ID: " + savedRequest.getRequestId());
        return savedRequest;
    }

    /**
     * Get requests based on user role
     */
    public List<Request> getRequests(String userNtid, RequestStatus status, RequestPriority priority, RequestType requestType, Long accountId) {
        System.out.println("  [RequestService] getRequests() called");
        System.out.println("  [RequestService] User NTID: " + userNtid);

        User user = userRepository.findByNtid(userNtid)
            .orElseThrow(() -> new RuntimeException("User not found: " + userNtid));

        UserRole role = user.getRole();
        System.out.println("  [RequestService] User role: " + role);

        List<Request> requests;

        if (role == UserRole.ADMIN) {
            // ADMIN can see all requests
            if (status == RequestStatus.ASSIGNED) {
                // When requesting ASSIGNED, return all tickets that have been assigned (regardless of current status)
                requests = requestRepository.findAllAssignedTicketsOrderByCreatedAtDesc();
            } else if (status != null || priority != null || requestType != null || accountId != null) {
                requests = requestRepository.findWithFilters(status, priority, requestType, accountId);
            } else {
                requests = requestRepository.findByActiveTrueOrderByCreatedAtDesc();
            }
        } else if (role == UserRole.SCRUM_MASTER) {
            // SCRUM_MASTER can see requests for accounts they handle
            // Check both FLOWAI_USER_ACCOUNTS junction table and user's accountId from FLOWAI_USERS
            List<Long> junctionAccountIds = userAccountRepository.findAccountIdsByNtid(userNtid);
            List<Long> handledAccountIds;
            
            // If no accounts in junction table, check user's accountId from FLOWAI_USERS
            if (junctionAccountIds.isEmpty() && user.getAccountId() != null) {
                System.out.println("  [RequestService] SCRUM_MASTER has no accounts in junction table, using accountId from user table: " + user.getAccountId());
                handledAccountIds = List.of(user.getAccountId());
            } else {
                handledAccountIds = junctionAccountIds;
            }
            
            if (handledAccountIds.isEmpty()) {
                System.out.println("  [RequestService] SCRUM_MASTER has no assigned accounts");
                System.out.println("  [RequestService] ACTION REQUIRED: Assign accounts to SCRUM_MASTER using POST /api/user-accounts or set accountId in user table");
                requests = List.of();
            } else {
                System.out.println("  [RequestService] SCRUM_MASTER handles " + handledAccountIds.size() + " accounts: " + handledAccountIds);
                
                // Get requests for handled accounts
                if (status == null) {
                    // No status filter - get ALL requests from handled accounts (for "Recent Tickets")
                    List<Request> allRequests = requestRepository.findByActiveTrueOrderByCreatedAtDesc();
                    System.out.println("  [RequestService] Total active requests in system: " + allRequests.size());
                    requests = allRequests.stream()
                        .filter(r -> {
                            boolean matches = r.getAccountId() != null && handledAccountIds.contains(r.getAccountId());
                            if (!matches && r.getAccountId() != null) {
                                System.out.println("  [RequestService] Request #" + r.getRequestId() + " has accountId " + r.getAccountId() + " (not in handled accounts)");
                            }
                            return matches;
                        })
                        .collect(Collectors.toList());
                    System.out.println("  [RequestService] Filtered to " + requests.size() + " requests from handled accounts");
                } else if (status == RequestStatus.ASSIGNED) {
                    // When requesting ASSIGNED, return all tickets that have been assigned (regardless of current status)
                    // Filter by handled accounts
                    List<Request> allAssignedRequests = requestRepository.findAllAssignedTicketsOrderByCreatedAtDesc();
                    requests = allAssignedRequests.stream()
                        .filter(r -> r.getAccountId() != null && handledAccountIds.contains(r.getAccountId()))
                        .collect(Collectors.toList());
                    System.out.println("  [RequestService] Filtered to " + requests.size() + " assigned requests from handled accounts");
                } else if (status == RequestStatus.OPEN) {
                    // Specifically requesting OPEN requests (for assignment queue)
                    List<Request> allOpenRequests = requestRepository.findByStatusAndActiveTrueOrderByCreatedAtDesc(RequestStatus.OPEN);
                    System.out.println("  [RequestService] Total OPEN requests in system: " + allOpenRequests.size());
                    requests = allOpenRequests.stream()
                        .filter(r -> {
                            boolean matches = r.getAccountId() != null && handledAccountIds.contains(r.getAccountId());
                            if (!matches && r.getAccountId() != null) {
                                System.out.println("  [RequestService] OPEN Request #" + r.getRequestId() + " has accountId " + r.getAccountId() + " (not in handled accounts)");
                            }
                            return matches;
                        })
                        .collect(Collectors.toList());
                    System.out.println("  [RequestService] Filtered to " + requests.size() + " OPEN requests from handled accounts");
                } else {
                    // For other specific statuses, get all requests and filter by status
                    requests = requestRepository.findByActiveTrueOrderByCreatedAtDesc()
                        .stream()
                        .filter(r -> r.getAccountId() != null && handledAccountIds.contains(r.getAccountId()))
                        .filter(r -> r.getStatus() == status)
                        .collect(Collectors.toList());
                }
                
                // Apply additional filters
                if (priority != null) {
                    requests = requests.stream()
                        .filter(r -> r.getPriority() == priority)
                        .collect(Collectors.toList());
                }
                if (requestType != null) {
                    requests = requests.stream()
                        .filter(r -> r.getRequestType() == requestType)
                        .collect(Collectors.toList());
                }
                if (accountId != null) {
                    requests = requests.stream()
                        .filter(r -> r.getAccountId() != null && r.getAccountId().equals(accountId))
                        .collect(Collectors.toList());
                }
            }
        } else if (role == UserRole.MANAGER) {
            // MANAGER can see requests for their account
            Long managerAccountId = user.getAccountId();
            if (managerAccountId == null) {
                requests = List.of();
            } else {
                requests = requestRepository.findByAccountIdAndActiveTrueOrderByCreatedAtDesc(managerAccountId);
                // Apply additional filters
                if (status != null) {
                    requests = requests.stream()
                        .filter(r -> r.getStatus() == status)
                        .collect(Collectors.toList());
                }
                if (priority != null) {
                    requests = requests.stream()
                        .filter(r -> r.getPriority() == priority)
                        .collect(Collectors.toList());
                }
                if (requestType != null) {
                    requests = requests.stream()
                        .filter(r -> r.getRequestType() == requestType)
                        .collect(Collectors.toList());
                }
            }
        } else if (role == UserRole.DEVELOPER) {
            // DEVELOPER can see:
            // 1. Assigned requests (for "My Tickets")
            // 2. OPEN tickets (for viewing, but cannot update/delete unless they are the creator)
            if (status == RequestStatus.OPEN) {
                // When specifically requesting OPEN tickets, show all OPEN tickets
                requests = requestRepository.findByStatusAndActiveTrueOrderByCreatedAtDesc(RequestStatus.OPEN);
            } else if (status == RequestStatus.ASSIGNED) {
                // When requesting ASSIGNED, show all tickets assigned to this developer (regardless of current status)
                requests = requestRepository.findByAssignedToAndActiveTrueOrderByCreatedAtDesc(userNtid);
            } else if (status != null) {
                // For other specific statuses, show assigned requests with that status
                requests = requestRepository.findByAssignedToAndActiveTrueOrderByCreatedAtDesc(userNtid)
                    .stream()
                    .filter(r -> r.getStatus() == status)
                    .collect(Collectors.toList());
            } else {
                // No status filter: show created requests + assigned requests + OPEN tickets
                List<Request> createdRequests = requestRepository.findByCreatedByAndActiveTrueOrderByCreatedAtDesc(userNtid);
                List<Request> assignedRequests = requestRepository.findByAssignedToAndActiveTrueOrderByCreatedAtDesc(userNtid);
                List<Request> openRequests = requestRepository.findByStatusAndActiveTrueOrderByCreatedAtDesc(RequestStatus.OPEN);
                
                System.out.println("  [RequestService] DEVELOPER - Found " + createdRequests.size() + " created requests for " + userNtid);
                System.out.println("  [RequestService] DEVELOPER - Found " + assignedRequests.size() + " assigned requests for " + userNtid);
                System.out.println("  [RequestService] DEVELOPER - Found " + openRequests.size() + " OPEN requests");
                
                // Combine and remove duplicates
                requests = new java.util.ArrayList<>(createdRequests);
                for (Request assignedReq : assignedRequests) {
                    if (requests.stream().noneMatch(r -> r.getRequestId().equals(assignedReq.getRequestId()))) {
                        requests.add(assignedReq);
                    }
                }
                for (Request openReq : openRequests) {
                    if (requests.stream().noneMatch(r -> r.getRequestId().equals(openReq.getRequestId()))) {
                        requests.add(openReq);
                    }
                }
                // Sort by created date descending
                requests = requests.stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .collect(Collectors.toList());
                
                System.out.println("  [RequestService] DEVELOPER - Total requests after combining: " + requests.size());
            }
            
            // Apply additional filters
            if (priority != null) {
                requests = requests.stream()
                    .filter(r -> r.getPriority() == priority)
                    .collect(Collectors.toList());
            }
            if (requestType != null) {
                requests = requests.stream()
                    .filter(r -> r.getRequestType() == requestType)
                    .collect(Collectors.toList());
            }
            if (accountId != null) {
                requests = requests.stream()
                    .filter(r -> r.getAccountId() != null && r.getAccountId().equals(accountId))
                    .collect(Collectors.toList());
            }
        } else {
            // USER role: can see own created requests and assigned requests
            if (status == RequestStatus.OPEN) {
                // When requesting OPEN tickets, show all OPEN tickets
                requests = requestRepository.findByStatusAndActiveTrueOrderByCreatedAtDesc(RequestStatus.OPEN);
            } else if (status == RequestStatus.ASSIGNED) {
                // When requesting ASSIGNED, show all tickets assigned to this user (regardless of current status)
                requests = requestRepository.findByAssignedToAndActiveTrueOrderByCreatedAtDesc(userNtid);
            } else if (status != null) {
                // For other specific statuses, show own created requests with that status
                requests = requestRepository.findByCreatedByAndActiveTrueOrderByCreatedAtDesc(userNtid)
                    .stream()
                    .filter(r -> r.getStatus() == status)
                    .collect(Collectors.toList());
            } else {
                // No status filter: show own created requests + assigned requests
                List<Request> createdRequests = requestRepository.findByCreatedByAndActiveTrueOrderByCreatedAtDesc(userNtid);
                List<Request> assignedRequests = requestRepository.findByAssignedToAndActiveTrueOrderByCreatedAtDesc(userNtid);
                
                // Combine and remove duplicates
                requests = new java.util.ArrayList<>(createdRequests);
                for (Request assignedReq : assignedRequests) {
                    if (requests.stream().noneMatch(r -> r.getRequestId().equals(assignedReq.getRequestId()))) {
                        requests.add(assignedReq);
                    }
                }
                // Sort by created date descending
                requests = requests.stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .collect(Collectors.toList());
            }
            
            // Apply additional filters
            if (priority != null) {
                requests = requests.stream()
                    .filter(r -> r.getPriority() == priority)
                    .collect(Collectors.toList());
            }
            if (requestType != null) {
                requests = requests.stream()
                    .filter(r -> r.getRequestType() == requestType)
                    .collect(Collectors.toList());
            }
            if (accountId != null) {
                requests = requests.stream()
                    .filter(r -> r.getAccountId() != null && r.getAccountId().equals(accountId))
                    .collect(Collectors.toList());
            }
        }

        System.out.println("  [RequestService] Found " + requests.size() + " requests");
        return requests;
    }

    /**
     * Get single request by ID
     */
    public Request getRequestById(Long requestId, String userNtid) {
        System.out.println("  [RequestService] getRequestById() called");
        System.out.println("  [RequestService] Request ID: " + requestId);
        System.out.println("  [RequestService] User NTID: " + userNtid);

        Request request = requestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        if (!request.getActive()) {
            throw new RuntimeException("Request is not active");
        }

        // All users can VIEW all tickets (including OPEN tickets)
        // Access restrictions apply only for UPDATE and DELETE operations
        // No view restrictions - all active tickets are visible to all users

        return request;
    }

    /**
     * Update request
     */
    @Transactional
    public Request updateRequest(Long requestId, UpdateRequestDTO updateDTO, String userNtid) {
        System.out.println("  [RequestService] updateRequest() called");
        System.out.println("  [RequestService] Request ID: " + requestId);

        Request request = requestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        User user = userRepository.findByNtid(userNtid)
            .orElseThrow(() -> new RuntimeException("User not found: " + userNtid));

        // Update permissions:
        // 1. ADMIN can always update
        // 2. Creator can update
        // 3. Assigned user can update (after assignment)
        // 4. SCRUM_MASTER can update tickets in their accounts
        boolean canUpdate = false;
        
        if (user.getRole() == UserRole.ADMIN) {
            canUpdate = true;
        } else if (userNtid.equals(request.getCreatedBy())) {
            // Creator can update
            canUpdate = true;
        } else if (userNtid.equals(request.getAssignedTo()) && request.getAssignedTo() != null) {
            // Assigned user can update after assignment
            canUpdate = true;
        } else if (user.getRole() == UserRole.SCRUM_MASTER) {
            // SCRUM_MASTER can update tickets in their handled accounts
            List<Long> junctionAccountIds = userAccountRepository.findAccountIdsByNtid(userNtid);
            List<Long> handledAccountIds;
            
            if (junctionAccountIds.isEmpty() && user.getAccountId() != null) {
                handledAccountIds = List.of(user.getAccountId());
            } else {
                handledAccountIds = junctionAccountIds;
            }
            
            if (request.getAccountId() != null && handledAccountIds.contains(request.getAccountId())) {
                canUpdate = true;
            }
        }
        
        if (!canUpdate) {
            throw new RuntimeException("Only creator, assigned user, ADMIN, or SCRUM_MASTER (for their accounts) can update request");
        }

        if (updateDTO.getTitle() != null) {
            request.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getDescription() != null) {
            request.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getRequestType() != null) {
            request.setRequestType(updateDTO.getRequestType());
        }
        if (updateDTO.getPriority() != null) {
            request.setPriority(updateDTO.getPriority());
        }

        return requestRepository.save(request);
    }

    /**
     * Assign request to developer
     */
    @Transactional
    public Request assignRequest(Long requestId, AssignRequestDTO assignDTO, String assignedBy) {
        System.out.println("  [RequestService] assignRequest() called");
        System.out.println("  [RequestService] Request ID: " + requestId);
        System.out.println("  [RequestService] Assigned by: " + assignedBy);

        Request request = requestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        User assigner = userRepository.findByNtid(assignedBy)
            .orElseThrow(() -> new RuntimeException("User not found: " + assignedBy));

        // Only SCRUM_MASTER or ADMIN can assign
        if (assigner.getRole() != UserRole.SCRUM_MASTER && assigner.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only SCRUM_MASTER or ADMIN can assign requests");
        }

        // Check if assigned user exists (can assign to any active user)
        User assignedUser = userRepository.findByNtid(assignDTO.getAssignedTo())
            .orElseThrow(() -> new RuntimeException("Assigned user not found: " + assignDTO.getAssignedTo()));

        // Ensure assigned user is active
        if (!assignedUser.getActive()) {
            throw new RuntimeException("Cannot assign to inactive user");
        }

        request.setAssignedTo(assignDTO.getAssignedTo());
        request.setAssignedAt(LocalDateTime.now());
        request.setAssignedBy(assignedBy); // Track who assigned the ticket
        request.setStatus(RequestStatus.ASSIGNED);
        request.setEta(assignDTO.getEta());

        return requestRepository.save(request);
    }

    /**
     * Update request status
     */
    @Transactional
    public Request updateStatus(Long requestId, UpdateStatusDTO statusDTO, String userNtid) {
        System.out.println("  [RequestService] updateStatus() called");
        System.out.println("  [RequestService] Request ID: " + requestId);
        System.out.println("  [RequestService] New status: " + statusDTO.getStatus());

        Request request = requestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        User user = userRepository.findByNtid(userNtid)
            .orElseThrow(() -> new RuntimeException("User not found: " + userNtid));

        // Cannot update status of OPEN tickets (must be assigned first)
        if (request.getStatus() == RequestStatus.OPEN) {
            throw new RuntimeException("Cannot update status of OPEN tickets. Ticket must be assigned first.");
        }

        // ADMIN can always update status (except OPEN)
        // Assigned user can update status (regardless of role)
        // SCRUM_MASTER can update status for tickets in their accounts
        boolean canUpdate = false;
        
        if (user.getRole() == UserRole.ADMIN) {
            canUpdate = true;
        } else if (userNtid.equals(request.getAssignedTo())) {
            // Assigned user can update status
            canUpdate = true;
        } else if (user.getRole() == UserRole.SCRUM_MASTER) {
            // SCRUM_MASTER can update status for tickets in their handled accounts
            List<Long> junctionAccountIds = userAccountRepository.findAccountIdsByNtid(userNtid);
            List<Long> handledAccountIds;
            
            if (junctionAccountIds.isEmpty() && user.getAccountId() != null) {
                handledAccountIds = List.of(user.getAccountId());
            } else {
                handledAccountIds = junctionAccountIds;
            }
            
            if (request.getAccountId() != null && handledAccountIds.contains(request.getAccountId())) {
                canUpdate = true;
            }
        }
        
        if (!canUpdate) {
            throw new RuntimeException("Only assigned user, ADMIN, or SCRUM_MASTER (for their accounts) can update request status");
        }

        // Validate status transition
        RequestStatus newStatus = statusDTO.getStatus();
        if (newStatus == RequestStatus.COMPLETED || newStatus == RequestStatus.ON_HOLD || newStatus == RequestStatus.DELAYED) {
            request.setStatus(newStatus);
        } else if (newStatus == RequestStatus.IN_PROGRESS) {
            if (request.getStatus() == RequestStatus.ASSIGNED) {
                request.setStatus(newStatus);
            } else {
                throw new RuntimeException("Can only set IN_PROGRESS from ASSIGNED status");
            }
        } else {
            throw new RuntimeException("Invalid status transition");
        }

        return requestRepository.save(request);
    }

    /**
     * Delete request (soft delete)
     */
    @Transactional
    public void deleteRequest(Long requestId, String userNtid) {
        System.out.println("  [RequestService] deleteRequest() called");
        System.out.println("  [RequestService] Request ID: " + requestId);

        Request request = requestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        User user = userRepository.findByNtid(userNtid)
            .orElseThrow(() -> new RuntimeException("User not found: " + userNtid));

        // Deletion rules: Only creator, SCRUM_MASTER, or ADMIN can delete tickets (regardless of status)
        if (user.getRole() != UserRole.ADMIN && 
            user.getRole() != UserRole.SCRUM_MASTER && 
            !userNtid.equals(request.getCreatedBy())) {
            throw new RuntimeException("Only creator, SCRUM_MASTER, or ADMIN can delete tickets");
        }

        request.setActive(false);
        requestRepository.save(request);
    }

    /**
     * Get requests approaching ETA
     */
    public List<Request> getRequestsApproachingEta(int thresholdMinutes) {
        LocalDateTime thresholdTime = LocalDateTime.now().plusMinutes(thresholdMinutes);
        return requestRepository.findRequestsApproachingEta(thresholdTime);
    }

    /**
     * Get dashboard statistics
     */
    public java.util.Map<String, Object> getDashboardStats(String userNtid) {
        User user = userRepository.findByNtid(userNtid)
            .orElseThrow(() -> new RuntimeException("User not found: " + userNtid));

        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        if (user.getRole() == UserRole.ADMIN) {
            stats.put("totalRequests", requestRepository.countByStatusAndActiveTrue(null));
            stats.put("openRequests", requestRepository.countByStatusAndActiveTrue(RequestStatus.OPEN));
            stats.put("assignedRequests", requestRepository.countByStatusAndActiveTrue(RequestStatus.ASSIGNED));
            stats.put("inProgressRequests", requestRepository.countByStatusAndActiveTrue(RequestStatus.IN_PROGRESS));
            stats.put("completedRequests", requestRepository.countByStatusAndActiveTrue(RequestStatus.COMPLETED));
        } else if (user.getRole() == UserRole.SCRUM_MASTER) {
            // SCRUM_MASTER sees stats for accounts they handle
            // Check both FLOWAI_USER_ACCOUNTS junction table and user's accountId from FLOWAI_USERS
            List<Long> junctionAccountIds = userAccountRepository.findAccountIdsByNtid(userNtid);
            List<Long> handledAccountIds;
            
            // If no accounts in junction table, check user's accountId from FLOWAI_USERS
            if (junctionAccountIds.isEmpty() && user.getAccountId() != null) {
                handledAccountIds = List.of(user.getAccountId());
            } else {
                handledAccountIds = junctionAccountIds;
            }
            
            if (handledAccountIds.isEmpty()) {
                stats.put("openRequests", 0);
                stats.put("assignedRequests", 0);
                stats.put("inProgressRequests", 0);
                stats.put("completedRequests", 0);
                stats.put("totalRequests", 0);
            } else {
                List<Request> allRequests = requestRepository.findByActiveTrueOrderByCreatedAtDesc()
                    .stream()
                    .filter(r -> r.getAccountId() != null && handledAccountIds.contains(r.getAccountId()))
                    .collect(Collectors.toList());
                
                stats.put("openRequests", (long) allRequests.stream().filter(r -> r.getStatus() == RequestStatus.OPEN).count());
                stats.put("assignedRequests", (long) allRequests.stream().filter(r -> r.getStatus() == RequestStatus.ASSIGNED).count());
                stats.put("inProgressRequests", (long) allRequests.stream().filter(r -> r.getStatus() == RequestStatus.IN_PROGRESS).count());
                stats.put("completedRequests", (long) allRequests.stream().filter(r -> r.getStatus() == RequestStatus.COMPLETED).count());
                stats.put("totalRequests", (long) allRequests.size());
            }
        } else if (user.getRole() == UserRole.DEVELOPER) {
            stats.put("assignedToMe", requestRepository.countByAssignedToAndActiveTrue(userNtid));
        } else if (user.getRole() == UserRole.USER) {
            List<Request> myRequests = requestRepository.findByCreatedByAndActiveTrueOrderByCreatedAtDesc(userNtid);
            stats.put("myRequests", myRequests.size());
        }

        return stats;
    }
}
