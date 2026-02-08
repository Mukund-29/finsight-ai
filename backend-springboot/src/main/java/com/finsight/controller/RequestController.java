package com.finsight.controller;

import com.finsight.dto.AssignRequestDTO;
import com.finsight.dto.CreateRequestDTO;
import com.finsight.dto.UpdateRequestDTO;
import com.finsight.dto.UpdateStatusDTO;
import com.finsight.entity.Request;
import com.finsight.entity.RequestPriority;
import com.finsight.entity.RequestStatus;
import com.finsight.entity.RequestType;
import com.finsight.service.RequestService;
import com.finsight.service.RequestCommentService;
import com.finsight.service.TimerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Request Controller
 * 
 * @author Mukund Kute
 */
@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "*")
public class RequestController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private TimerService timerService;

    @Autowired
    private RequestCommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Create new request
     */
    @PostMapping
    public ResponseEntity<?> createRequest(
            @RequestBody String requestBody,
            @RequestHeader(value = "X-User-NTID", required = false) String userNtid) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: POST /api/requests");
        System.out.println("Request Header - X-User-NTID: " + userNtid);
        
        try {
            if (userNtid == null || userNtid.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            CreateRequestDTO createDTO = objectMapper.readValue(requestBody, CreateRequestDTO.class);
            System.out.println("Request Data:");
            System.out.println("  - Title: " + createDTO.getTitle());
            System.out.println("  - Type: " + createDTO.getRequestType());
            System.out.println("  - Priority: " + createDTO.getPriority());
            System.out.println("  - Account ID: " + createDTO.getAccountId());
            
            // Validate accountId is provided
            if (createDTO.getAccountId() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Account ID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            Request request = requestService.createRequest(createDTO, userNtid);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Request created successfully");
            response.put("requestId", request.getRequestId());
            response.put("title", request.getTitle());
            response.put("status", request.getStatus());
            
            System.out.println("Response: " + response);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get all requests (filtered by user role)
     */
    @GetMapping
    public ResponseEntity<?> getRequests(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(required = false) RequestPriority priority,
            @RequestParam(required = false) RequestType requestType,
            @RequestParam(required = false) Long accountId,
            @RequestHeader(value = "X-User-NTID", required = false) String userNtid) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: GET /api/requests");
        System.out.println("Request Header - X-User-NTID: " + userNtid);
        
        try {
            if (userNtid == null || userNtid.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            List<Request> requests = requestService.getRequests(userNtid, status, priority, requestType, accountId);
            
            // Enrich with timer information
            List<Map<String, Object>> responseList = requests.stream().map(request -> {
                Map<String, Object> requestMap = new HashMap<>();
                requestMap.put("requestId", request.getRequestId());
                requestMap.put("title", request.getTitle());
                requestMap.put("description", request.getDescription());
                requestMap.put("requestType", request.getRequestType());
                requestMap.put("priority", request.getPriority());
                requestMap.put("status", request.getStatus());
                requestMap.put("createdBy", request.getCreatedBy());
                requestMap.put("assignedTo", request.getAssignedTo());
                requestMap.put("assignedBy", request.getAssignedBy());
                requestMap.put("accountId", request.getAccountId());
                requestMap.put("createdAt", request.getCreatedAt());
                requestMap.put("assignedAt", request.getAssignedAt());
                requestMap.put("eta", request.getEta());
                
                // Add timer information
                Duration timeInOpenQueue = timerService.getTimeInOpenQueue(request);
                Duration timeInDeveloperQueue = timerService.getTimeInDeveloperQueue(request);
                Duration timeUntilEta = timerService.getTimeUntilEta(request);
                
                requestMap.put("timeInOpenQueue", timerService.formatDuration(timeInOpenQueue));
                requestMap.put("timeInDeveloperQueue", timerService.formatDuration(timeInDeveloperQueue));
                requestMap.put("timeUntilEta", timeUntilEta != null ? timerService.formatDuration(timeUntilEta) : null);
                requestMap.put("etaApproaching", timerService.isEtaApproaching(request, 30));
                requestMap.put("etaExceeded", timerService.isEtaExceeded(request));
                
                return requestMap;
            }).collect(Collectors.toList());
            
            System.out.println("Found " + responseList.size() + " requests");
            System.out.println("=========================================\n");
            return ResponseEntity.ok(responseList);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get single request by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getRequestById(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-NTID", required = false) String userNtid) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: GET /api/requests/" + id);
        
        try {
            if (userNtid == null || userNtid.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            Request request = requestService.getRequestById(id, userNtid);
            
            Map<String, Object> response = new HashMap<>();
            response.put("requestId", request.getRequestId());
            response.put("title", request.getTitle());
            response.put("description", request.getDescription());
            response.put("requestType", request.getRequestType());
            response.put("priority", request.getPriority());
            response.put("status", request.getStatus());
            response.put("createdBy", request.getCreatedBy());
            response.put("assignedTo", request.getAssignedTo());
            response.put("assignedBy", request.getAssignedBy());
            response.put("accountId", request.getAccountId());
            response.put("createdAt", request.getCreatedAt());
            response.put("updatedAt", request.getUpdatedAt());
            response.put("assignedAt", request.getAssignedAt());
            response.put("eta", request.getEta());
            
            // Add timer information
            Duration timeInOpenQueue = timerService.getTimeInOpenQueue(request);
            Duration timeInDeveloperQueue = timerService.getTimeInDeveloperQueue(request);
            Duration timeUntilEta = timerService.getTimeUntilEta(request);
            
            response.put("timeInOpenQueue", timerService.formatDuration(timeInOpenQueue));
            response.put("timeInDeveloperQueue", timerService.formatDuration(timeInDeveloperQueue));
            response.put("timeUntilEta", timeUntilEta != null ? timerService.formatDuration(timeUntilEta) : null);
            response.put("etaApproaching", timerService.isEtaApproaching(request, 30));
            response.put("etaExceeded", timerService.isEtaExceeded(request));
            
            System.out.println("Response: " + response);
            System.out.println("=========================================\n");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Update request
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRequest(
            @PathVariable Long id,
            @RequestBody String requestBody,
            @RequestHeader(value = "X-User-NTID", required = false) String userNtid) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: PUT /api/requests/" + id);
        
        try {
            if (userNtid == null || userNtid.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            UpdateRequestDTO updateDTO = objectMapper.readValue(requestBody, UpdateRequestDTO.class);
            Request request = requestService.updateRequest(id, updateDTO, userNtid);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Request updated successfully");
            response.put("requestId", request.getRequestId());
            response.put("title", request.getTitle());
            
            System.out.println("Response: " + response);
            System.out.println("=========================================\n");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Assign request to developer
     */
    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assignRequest(
            @PathVariable Long id,
            @RequestBody String requestBody,
            @RequestHeader(value = "X-User-NTID", required = false) String userNtid) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: POST /api/requests/" + id + "/assign");
        
        try {
            if (userNtid == null || userNtid.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            AssignRequestDTO assignDTO = objectMapper.readValue(requestBody, AssignRequestDTO.class);
            System.out.println("Assignment Data:");
            System.out.println("  - Assigned to: " + assignDTO.getAssignedTo());
            System.out.println("  - ETA: " + assignDTO.getEta());

            Request request = requestService.assignRequest(id, assignDTO, userNtid);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Request assigned successfully");
            response.put("requestId", request.getRequestId());
            response.put("assignedTo", request.getAssignedTo());
            response.put("eta", request.getEta());
            
            System.out.println("Response: " + response);
            System.out.println("=========================================\n");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Update request status
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody String requestBody,
            @RequestHeader(value = "X-User-NTID", required = false) String userNtid) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: PUT /api/requests/" + id + "/status");
        
        try {
            if (userNtid == null || userNtid.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            UpdateStatusDTO statusDTO = objectMapper.readValue(requestBody, UpdateStatusDTO.class);
            System.out.println("Status Update Data:");
            System.out.println("  - New status: " + statusDTO.getStatus());
            System.out.println("  - Comment: " + statusDTO.getComment());

            Request request = requestService.updateStatus(id, statusDTO, userNtid);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Request status updated successfully");
            response.put("requestId", request.getRequestId());
            response.put("status", request.getStatus());
            
            System.out.println("Response: " + response);
            System.out.println("=========================================\n");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Delete request
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRequest(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-NTID", required = false) String userNtid) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: DELETE /api/requests/" + id);
        
        try {
            if (userNtid == null || userNtid.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            requestService.deleteRequest(id, userNtid);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Request deleted successfully");
            
            System.out.println("Response: " + response);
            System.out.println("=========================================\n");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get dashboard statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(
            @RequestHeader(value = "X-User-NTID", required = false) String userNtid) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: GET /api/requests/stats");
        
        try {
            if (userNtid == null || userNtid.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            Map<String, Object> stats = requestService.getDashboardStats(userNtid);
            
            System.out.println("Response: " + stats);
            System.out.println("=========================================\n");
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get requests approaching ETA
     */
    @GetMapping("/eta-alerts")
    public ResponseEntity<?> getEtaAlerts(
            @RequestParam(defaultValue = "30") int thresholdMinutes,
            @RequestHeader(value = "X-User-NTID", required = false) String userNtid) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: GET /api/requests/eta-alerts");
        System.out.println("Threshold: " + thresholdMinutes + " minutes");
        
        try {
            if (userNtid == null || userNtid.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            List<Request> requests = requestService.getRequestsApproachingEta(thresholdMinutes);
            
            List<Map<String, Object>> responseList = requests.stream().map(request -> {
                Map<String, Object> requestMap = new HashMap<>();
                requestMap.put("requestId", request.getRequestId());
                requestMap.put("title", request.getTitle());
                requestMap.put("assignedTo", request.getAssignedTo());
                requestMap.put("eta", request.getEta());
                requestMap.put("timeUntilEta", timerService.formatDuration(timerService.getTimeUntilEta(request)));
                requestMap.put("etaExceeded", timerService.isEtaExceeded(request));
                return requestMap;
            }).collect(Collectors.toList());
            
            System.out.println("Found " + responseList.size() + " requests approaching ETA");
            System.out.println("=========================================\n");
            return ResponseEntity.ok(responseList);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get account statistics (tickets per account)
     * Available to all users
     */
    @GetMapping("/account-statistics")
    public ResponseEntity<?> getAccountStatistics(
            @RequestHeader(value = "X-User-NTID", required = false) String userNtid) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: GET /api/requests/account-statistics");
        System.out.println("Request Header - X-User-NTID: " + userNtid);
        
        try {
            if (userNtid == null || userNtid.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            List<com.finsight.dto.AccountStatsDTO> stats = requestService.getAccountStatistics();
            
            System.out.println("Returning statistics for " + stats.size() + " accounts");
            System.out.println("=========================================\n");
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get user ticket statistics filtered by account
     * Available to all users
     */
    @GetMapping("/user-statistics-by-account/{accountId}")
    public ResponseEntity<?> getUserStatisticsByAccount(
            @PathVariable Long accountId,
            @RequestHeader(value = "X-User-NTID", required = false) String userNtid) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: GET /api/requests/user-statistics-by-account/" + accountId);
        System.out.println("Request Header - X-User-NTID: " + userNtid);
        
        try {
            if (userNtid == null || userNtid.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            List<com.finsight.dto.UserTicketStatsDTO> stats = requestService.getUserStatisticsByAccount(accountId);
            
            System.out.println("Returning statistics for " + stats.size() + " users in account " + accountId);
            System.out.println("=========================================\n");
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get user ticket statistics (all users)
     * Available to all users
     */
    @GetMapping("/user-statistics")
    public ResponseEntity<?> getUserTicketStatistics(
            @RequestHeader(value = "X-User-NTID", required = false) String userNtid) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: GET /api/requests/user-statistics");
        System.out.println("Request Header - X-User-NTID: " + userNtid);
        
        try {
            if (userNtid == null || userNtid.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            List<com.finsight.dto.UserTicketStatsDTO> stats = requestService.getUserTicketStatistics(userNtid);
            
            System.out.println("Returning statistics for " + stats.size() + " users");
            System.out.println("=========================================\n");
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Update ETA for a request
     */
    @PutMapping("/{id}/eta")
    public ResponseEntity<?> updateEta(
            @PathVariable Long id,
            @RequestBody String requestBody,
            @RequestHeader(value = "X-User-NTID", required = false) String userNtid) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: PUT /api/requests/" + id + "/eta");
        
        try {
            if (userNtid == null || userNtid.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            com.finsight.dto.UpdateEtaDTO updateEtaDTO = objectMapper.readValue(requestBody, com.finsight.dto.UpdateEtaDTO.class);
            System.out.println("ETA Update Data:");
            System.out.println("  - New ETA: " + updateEtaDTO.getNewEta());
            System.out.println("  - Reason: " + updateEtaDTO.getChangeReason());

            Request request = requestService.updateEta(id, updateEtaDTO, userNtid);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "ETA updated successfully");
            response.put("requestId", request.getRequestId());
            response.put("eta", request.getEta());
            
            System.out.println("Response: " + response);
            System.out.println("=========================================\n");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Get all comments for a request
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getComments(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-NTID", required = false) String userNtid) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: GET /api/requests/" + id + "/comments");
        
        try {
            if (userNtid == null || userNtid.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            List<com.finsight.dto.CommentDTO> comments = commentService.getCommentsByRequestId(id);
            
            System.out.println("Returning " + comments.size() + " comments");
            System.out.println("=========================================\n");
            return ResponseEntity.ok(comments);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Add a comment to a request
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable Long id,
            @RequestBody String requestBody,
            @RequestHeader(value = "X-User-NTID", required = false) String userNtid) {
        
        System.out.println("\n=========================================");
        System.out.println("API CALLED: POST /api/requests/" + id + "/comments");
        
        try {
            if (userNtid == null || userNtid.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User NTID is required in header X-User-NTID");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            com.finsight.dto.CreateCommentDTO createDTO = objectMapper.readValue(requestBody, com.finsight.dto.CreateCommentDTO.class);
            System.out.println("Comment Data:");
            System.out.println("  - Comment: " + createDTO.getCommentText());
            System.out.println("  - Is ETA Change: " + createDTO.getIsEtaChange());

            com.finsight.dto.CommentDTO comment = commentService.addComment(id, createDTO, userNtid);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Comment added successfully");
            response.put("comment", comment);
            
            System.out.println("Response: " + response);
            System.out.println("=========================================\n");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            System.out.println("Response: " + error);
            System.out.println("=========================================\n");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}
