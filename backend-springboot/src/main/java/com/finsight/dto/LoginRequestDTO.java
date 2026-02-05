package com.finsight.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Login Request DTO
 * 
 * @author Mukund Kute
 */
public class LoginRequestDTO {

    @NotBlank(message = "NTID is required")
    private String ntid;

    @NotBlank(message = "Password is required")
    private String password;

    // Constructors
    public LoginRequestDTO() {
    }

    public LoginRequestDTO(String ntid, String password) {
        this.ntid = ntid;
        this.password = password;
    }

    // Getters and Setters
    public String getNtid() {
        return ntid;
    }

    public void setNtid(String ntid) {
        this.ntid = ntid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
