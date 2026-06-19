package com.cardwise.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank @Email
    private String email;
    @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    @NotBlank
    private String name;

    public @NotBlank @Email String getEmail() { return email; }
    public void setEmail(@NotBlank @Email String email) { this.email = email; }
    public @NotBlank @Size(min = 6) String getPassword() { return password; }
    public void setPassword(@NotBlank @Size(min = 6) String password) { this.password = password; }
    public @NotBlank String getName() { return name; }
    public void setName(@NotBlank String name) { this.name = name; }
}
