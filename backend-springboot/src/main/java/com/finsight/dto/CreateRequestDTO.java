package com.finsight.dto;

import com.finsight.entity.RequestPriority;
import com.finsight.entity.RequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Create Request DTO
 * 
 * @author Mukund Kute
 */
public class CreateRequestDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Request type is required")
    private RequestType requestType;

    @NotNull(message = "Priority is required")
    private RequestPriority priority;

    @NotNull(message = "Account ID is required")
    private Long accountId;

    // Constructors
    public CreateRequestDTO() {
    }

    public CreateRequestDTO(String title, String description, RequestType requestType, RequestPriority priority, Long accountId) {
        this.title = title;
        this.description = description;
        this.requestType = requestType;
        this.priority = priority;
        this.accountId = accountId;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public RequestPriority getPriority() {
        return priority;
    }

    public void setPriority(RequestPriority priority) {
        this.priority = priority;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
