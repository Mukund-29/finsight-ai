package com.finsight.dto;

import com.finsight.entity.RequestPriority;
import com.finsight.entity.RequestType;

/**
 * Update Request DTO
 * 
 * @author Mukund Kute
 */
public class UpdateRequestDTO {

    private String title;
    private String description;
    private RequestType requestType;
    private RequestPriority priority;

    // Constructors
    public UpdateRequestDTO() {
    }

    public UpdateRequestDTO(String title, String description, RequestType requestType, RequestPriority priority) {
        this.title = title;
        this.description = description;
        this.requestType = requestType;
        this.priority = priority;
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
}
