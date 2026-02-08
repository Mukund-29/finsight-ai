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

    @Autowired
    private com.finsight.repository.AccountRepository accountRepository;

    @Autowired
    private RequestCommentService commentService;

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
            // SCRUM_MASTER can see:
            // 1. All OPEN tickets (for assignment purposes)
            // 2. All ASSIGNED tickets (to see what's been assigned)
            // 3. Tickets assigned to them personally (regardless of account)
            // 4. Tickets from accounts they handle (when status is null or other statuses)
            
            List<Long> junctionAccountIds = userAccountRepository.findAccountIdsByNtid(userNtid);
            List<Long> handledAccountIds;
            
            // If no accounts in junction table, check user's accountId from FLOWAI_USERS
            if (junctionAccountIds.isEmpty() && user.getAccountId() != null) {
                System.out.println("  [RequestService] SCRUM_MASTER has no accounts in junction table, using accountId from user table: " + user.getAccountId());
                handledAccountIds = List.of(user.getAccountId());
            } else {
                handledAccountIds = junctionAccountIds;
            }
            
            System.out.println("  [RequestService] SCRUM_MASTER handles " + handledAccountIds.size() + " accounts: " + handledAccountIds);
            
            if (status == RequestStatus.OPEN) {
                // SCRUM_MASTER can see ALL OPEN tickets (for assignment purposes)
                requests = requestRepository.findByStatusAndActiveTrueOrderByCreatedAtDesc(RequestStatus.OPEN);
                System.out.println("  [RequestService] SCRUM_MASTER sees " + requests.size() + " OPEN requests (all open tickets)");
            } else if (status == RequestStatus.ASSIGNED) {
                // SCRUM_MASTER can see ALL assigned tickets (to see what's been assigned)
                requests = requestRepository.findAllAssignedTicketsOrderByCreatedAtDesc();
                System.out.println("  [RequestService] SCRUM_MASTER sees " + requests.size() + " ASSIGNED requests (all assigned tickets)");
            } else if (status == null) {
                // No status filter - get tickets from handled accounts AND tickets assigned to SCRUM_MASTER
                List<Request> allRequests = requestRepository.findByActiveTrueOrderByCreatedAtDesc();
                System.out.println("  [RequestService] Total active requests in system: " + allRequests.size());
                
                requests = allRequests.stream()
                    .filter(r -> {
                        // Include if: assigned to SCRUM_MASTER OR from handled accounts
                        boolean assignedToMe = r.getAssignedTo() != null && r.getAssignedTo().equalsIgnoreCase(userNtid);
                        boolean fromHandledAccount = r.getAccountId() != null && handledAccountIds.contains(r.getAccountId());
                        return assignedToMe || fromHandledAccount;
                    })
                    .collect(Collectors.toList());
                System.out.println("  [RequestService] Filtered to " + requests.size() + " requests (assigned to SCRUM_MASTER or from handled accounts)");
            } else {
                // For other specific statuses, get tickets from handled accounts AND tickets assigned to SCRUM_MASTER
                List<Request> allRequests = requestRepository.findByActiveTrueOrderByCreatedAtDesc();
                requests = allRequests.stream()
                    .filter(r -> r.getStatus() == status)
                    .filter(r -> {
                        // Include if: assigned to SCRUM_MASTER OR from handled accounts
                        boolean assignedToMe = r.getAssignedTo() != null && r.getAssignedTo().equalsIgnoreCase(userNtid);
                        boolean fromHandledAccount = r.getAccountId() != null && handledAccountIds.contains(r.getAccountId());
                        return assignedToMe || fromHandledAccount;
                    })
                    .collect(Collectors.toList());
                System.out.println("  [RequestService] Filtered to " + requests.size() + " " + status + " requests (assigned to SCRUM_MASTER or from handled accounts)");
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
            // USER role and all other roles: can see ALL tickets (view restriction removed)
            // All users can view all tickets, but other restrictions (update, assign, etc.) remain
            if (status == RequestStatus.ASSIGNED) {
                // When requesting ASSIGNED, return all tickets that have been assigned (regardless of current status)
                requests = requestRepository.findAllAssignedTicketsOrderByCreatedAtDesc();
            } else if (status != null || priority != null || requestType != null || accountId != null) {
                requests = requestRepository.findWithFilters(status, priority, requestType, accountId);
            } else {
                // No filters: show all active tickets
                requests = requestRepository.findByActiveTrueOrderByCreatedAtDesc();
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
     * Update ETA with comment
     */
    @Transactional
    public Request updateEta(Long requestId, com.finsight.dto.UpdateEtaDTO updateEtaDTO, String userNtid) {
        System.out.println("  [RequestService] updateEta() called");
        System.out.println("  [RequestService] Request ID: " + requestId);
        System.out.println("  [RequestService] Updated by: " + userNtid);

        Request request = requestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Request not found: " + requestId));

        User user = userRepository.findByNtid(userNtid)
            .orElseThrow(() -> new RuntimeException("User not found: " + userNtid));

        // Check permissions: ADMIN, SCRUM_MASTER, or assigned user can update ETA
        boolean canUpdateEta = false;
        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.SCRUM_MASTER) {
            canUpdateEta = true;
        } else if (request.getAssignedTo() != null && request.getAssignedTo().equalsIgnoreCase(userNtid)) {
            canUpdateEta = true;
        }

        if (!canUpdateEta) {
            throw new RuntimeException("Only ADMIN, SCRUM_MASTER, or assigned user can update ETA");
        }

        // Validate that change reason is provided (mandatory)
        if (updateEtaDTO.getChangeReason() == null || updateEtaDTO.getChangeReason().trim().isEmpty()) {
            throw new RuntimeException("Reason for ETA change is required");
        }

        // Validate new ETA is provided
        if (updateEtaDTO.getNewEta() == null) {
            throw new RuntimeException("New ETA is required");
        }

        // Store old ETA
        LocalDateTime oldEta = request.getEta();
        LocalDateTime newEta = updateEtaDTO.getNewEta();

        // Update ETA
        request.setEta(newEta);

        // Save request first
        Request savedRequest = requestRepository.save(request);

        // Add comment for ETA change
        try {
            commentService.addEtaChangeComment(
                requestId,
                oldEta,
                newEta,
                updateEtaDTO.getChangeReason(),
                userNtid
            );
        } catch (Exception e) {
            System.out.println("  [RequestService] Warning: Failed to add ETA change comment: " + e.getMessage());
            // Don't fail the ETA update if comment creation fails
        }

        return savedRequest;
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

    /**
     * Get account statistics (tickets per account)
     * Available to all users
     */
    public List<com.finsight.dto.AccountStatsDTO> getAccountStatistics() {
        System.out.println("\n=========================================");
        System.out.println("API CALLED: getAccountStatistics");
        
        List<com.finsight.entity.Account> allAccounts = accountRepository.findByActiveTrueOrderByAccountNameAsc();
        List<com.finsight.dto.AccountStatsDTO> accountStatsList = new java.util.ArrayList<>();

        for (com.finsight.entity.Account account : allAccounts) {
            List<Request> accountRequests = requestRepository.findByAccountIdAndActiveTrueOrderByCreatedAtDesc(account.getAccountId());
            
            long totalTickets = accountRequests.size();
            
            long openTickets = accountRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.OPEN)
                .count();

            long resolvedTickets = accountRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.COMPLETED)
                .count();

            long pendingTickets = accountRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.ASSIGNED || r.getStatus() == RequestStatus.IN_PROGRESS)
                .count();

            long onHoldTickets = accountRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.ON_HOLD)
                .count();

            // ETA calculations
            long crossedEta = 0;
            LocalDateTime now = LocalDateTime.now();

            for (Request ticket : accountRequests) {
                if (ticket.getEta() != null) {
                    if (ticket.getStatus() == RequestStatus.COMPLETED) {
                        if (ticket.getUpdatedAt() != null && !ticket.getUpdatedAt().isBefore(ticket.getEta())) {
                            crossedEta++;
                        }
                    } else if (ticket.getEta().isBefore(now)) {
                        // ETA has passed but ticket is not completed
                        crossedEta++;
                    }
                }
            }

            com.finsight.dto.AccountStatsDTO accountStats = new com.finsight.dto.AccountStatsDTO(
                account.getAccountId(),
                account.getAccountName(),
                openTickets
            );

            accountStats.setTotalTickets(totalTickets);
            accountStats.setResolvedTickets(resolvedTickets);
            accountStats.setPendingTickets(pendingTickets);
            accountStats.setOnHoldTickets(onHoldTickets);
            accountStats.setCrossedEtaTickets(crossedEta);

            // Debug logging
            System.out.println("  Account: " + account.getAccountName() + " (ID: " + account.getAccountId() + ")");
            System.out.println("    Total: " + accountStats.getTotalTickets());
            System.out.println("    Resolved: " + accountStats.getResolvedTickets());
            System.out.println("    Pending (In Progress): " + accountStats.getPendingTickets());
            System.out.println("    On Hold: " + accountStats.getOnHoldTickets());
            System.out.println("    Open: " + accountStats.getOpenTickets());
            System.out.println("    Crossed ETA: " + accountStats.getCrossedEtaTickets());

            accountStatsList.add(accountStats);
        }

        System.out.println("Returning statistics for " + accountStatsList.size() + " accounts");
        System.out.println("=========================================\n");
        return accountStatsList;
    }

    /**
     * Get user ticket statistics filtered by account
     * Available to all users
     */
    public List<com.finsight.dto.UserTicketStatsDTO> getUserStatisticsByAccount(Long accountId) {
        System.out.println("\n=========================================");
        System.out.println("API CALLED: getUserStatisticsByAccount");
        System.out.println("Account ID: " + accountId);
        
        if (accountId == null) {
            System.out.println("ERROR: Account ID is null");
            throw new RuntimeException("Account ID cannot be null");
        }
        
        // Get all users who have this account (either directly or through junction table)
        List<User> allUsers = userRepository.findAllByOrderByNtidAsc();
        List<com.finsight.dto.UserTicketStatsDTO> statsList = new java.util.ArrayList<>();
        
        System.out.println("Total users in system: " + allUsers.size());

        for (User user : allUsers) {
            // Check if user belongs to this account
            boolean belongsToAccount = false;
            if (user.getAccountId() != null && user.getAccountId().equals(accountId)) {
                belongsToAccount = true;
            } else {
                // Check junction table
                List<Long> userAccountIds = userAccountRepository.findAccountIdsByNtid(user.getNtid());
                if (userAccountIds.contains(accountId)) {
                    belongsToAccount = true;
                }
            }

            if (!belongsToAccount) {
                continue; // Skip users not in this account
            }

            // Get tickets assigned to this user for this account
            // Use case-insensitive comparison by trimming and normalizing
            String userNtidNormalized = user.getNtid() != null ? user.getNtid().trim() : "";
            List<Request> assignedTickets = requestRepository.findByAssignedToAndActiveTrueOrderByCreatedAtDesc(userNtidNormalized)
                .stream()
                .filter(r -> r.getAccountId() != null && r.getAccountId().equals(accountId))
                .collect(Collectors.toList());
            
            // Debug logging
            System.out.println("  User: " + user.getNtid() + " in Account: " + accountId + " - Found " + assignedTickets.size() + " assigned tickets");
            
            com.finsight.dto.UserTicketStatsDTO stats = new com.finsight.dto.UserTicketStatsDTO(
                user.getNtid(),
                user.getEmail(),
                user.getRole().toString()
            );
            
            stats.setTotalTickets((long) assignedTickets.size());

            // Count by status
            long resolved = assignedTickets.stream()
                .filter(r -> r.getStatus() == RequestStatus.COMPLETED)
                .count();
            stats.setResolvedTickets(resolved);

            long pending = assignedTickets.stream()
                .filter(r -> r.getStatus() == RequestStatus.ASSIGNED || r.getStatus() == RequestStatus.IN_PROGRESS)
                .count();
            stats.setPendingTickets(pending);
            System.out.println("  User: " + user.getNtid() + " in Account: " + accountId + " - Pending (In Progress): " + pending);

            long onHold = assignedTickets.stream()
                .filter(r -> r.getStatus() == RequestStatus.ON_HOLD)
                .count();
            stats.setOnHoldTickets(onHold);

            // Unresolved is no longer displayed, but keep for backward compatibility
            long unresolved = assignedTickets.stream()
                .filter(r -> r.getStatus() != RequestStatus.COMPLETED && r.getStatus() != RequestStatus.CANCELLED)
                .count();
            stats.setUnresolvedTickets(unresolved);

            // ETA calculations
            long crossedEta = 0;
            LocalDateTime now = LocalDateTime.now();

            for (Request ticket : assignedTickets) {
                if (ticket.getEta() != null) {
                    if (ticket.getStatus() == RequestStatus.COMPLETED) {
                        if (ticket.getUpdatedAt() != null && !ticket.getUpdatedAt().isBefore(ticket.getEta())) {
                            crossedEta++;
                        }
                    } else if (ticket.getEta().isBefore(now)) {
                        crossedEta++;
                    }
                }
            }

            stats.setCrossedEtaTickets(crossedEta);

            statsList.add(stats);
        }

        System.out.println("Returning statistics for " + statsList.size() + " users in account " + accountId);
        if (statsList.isEmpty()) {
            System.out.println("WARNING: No users found for account " + accountId);
        }
        System.out.println("=========================================\n");
        return statsList;
    }

    /**
     * Get ticket statistics for all users
     * Available to all users
     */
    public List<com.finsight.dto.UserTicketStatsDTO> getUserTicketStatistics(String requestedBy) {
        System.out.println("\n=========================================");
        System.out.println("API CALLED: getUserTicketStatistics");
        System.out.println("Requested by: " + requestedBy);
        
        // Verify user exists (but no role restriction - all users can view)
        userRepository.findByNtid(requestedBy)
            .orElseThrow(() -> new RuntimeException("Requester not found"));

        // Get all users (both active and inactive) - to show statistics for all users including their tickets
        // Note: Tickets assigned to inactive users remain visible in statistics and ticket views
        // When a user is reactivated, they can immediately see their tickets again
        List<User> allUsers = userRepository.findAllByOrderByNtidAsc();
        List<com.finsight.dto.UserTicketStatsDTO> statsList = new java.util.ArrayList<>();

        for (User user : allUsers) {
            com.finsight.dto.UserTicketStatsDTO stats = new com.finsight.dto.UserTicketStatsDTO(
                user.getNtid(),
                user.getEmail(),
                user.getRole().toString()
            );

            // Get all tickets assigned to this user (regardless of user's active status)
            // This ensures tickets remain visible even when user is deactivated
            // Use case-insensitive comparison by trimming and normalizing
            String userNtidNormalized = user.getNtid() != null ? user.getNtid().trim() : "";
            List<Request> assignedTickets = requestRepository.findByAssignedToAndActiveTrueOrderByCreatedAtDesc(userNtidNormalized);
            
            // Debug logging
            System.out.println("  User: " + user.getNtid() + " - Found " + assignedTickets.size() + " assigned tickets");
            for (Request ticket : assignedTickets) {
                System.out.println("    Ticket ID: " + ticket.getRequestId() + ", Status: " + ticket.getStatus() + ", AssignedTo: '" + ticket.getAssignedTo() + "'");
            }
            
            stats.setTotalTickets((long) assignedTickets.size());

            // Count by status
            long resolved = assignedTickets.stream()
                .filter(r -> r.getStatus() == RequestStatus.COMPLETED)
                .count();
            stats.setResolvedTickets(resolved);

            long pending = assignedTickets.stream()
                .filter(r -> r.getStatus() == RequestStatus.ASSIGNED || r.getStatus() == RequestStatus.IN_PROGRESS)
                .count();
            stats.setPendingTickets(pending);

            long onHold = assignedTickets.stream()
                .filter(r -> r.getStatus() == RequestStatus.ON_HOLD)
                .count();
            stats.setOnHoldTickets(onHold);

            long unresolved = assignedTickets.stream()
                .filter(r -> r.getStatus() != RequestStatus.COMPLETED && r.getStatus() != RequestStatus.CANCELLED)
                .count();
            stats.setUnresolvedTickets(unresolved);

            // ETA calculations
            long crossedEta = 0;
            LocalDateTime now = LocalDateTime.now();

            for (Request ticket : assignedTickets) {
                if (ticket.getEta() != null) {
                    if (ticket.getStatus() == RequestStatus.COMPLETED) {
                        // Check if completed after ETA
                        if (ticket.getUpdatedAt() != null && !ticket.getUpdatedAt().isBefore(ticket.getEta())) {
                            crossedEta++;
                        }
                    } else if (ticket.getEta().isBefore(now)) {
                        // ETA has passed but ticket is not completed
                        crossedEta++;
                    }
                }
            }

            stats.setCrossedEtaTickets(crossedEta);

            statsList.add(stats);
        }

        System.out.println("Returning statistics for " + statsList.size() + " users");
        System.out.println("=========================================\n");
        return statsList;
    }
}
