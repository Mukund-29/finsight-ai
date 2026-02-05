package com.finsight.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * User Registration DTO
 * 
 * @author Mukund Kute
 */
public class UserRegistrationDTO {

    @NotBlank(message = "NTID is required")
    private String ntid;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    private String account; // Account name from dropdown

    private String password;

    // Constructors
    public UserRegistrationDTO() {
    }

    public UserRegistrationDTO(String ntid, String email, String account, String password) {
        this.ntid = ntid;
        this.email = email;
        this.account = account;
        this.password = password;
    }


    // Getters and Setters
    public String getNtid() {
        return ntid;
    }

    public void setNtid(String ntid) {
        this.ntid = ntid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
