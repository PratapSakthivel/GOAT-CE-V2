package com.goatce.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyOtpRequest(
        @NotBlank @Email String email,
        @NotBlank String otp,
        @NotBlank @Size(min = 6) String newPassword
) {}
