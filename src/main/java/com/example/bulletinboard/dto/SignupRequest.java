package com.example.bulletinboard.dto;

import jakarta.validation.constraints.*;

public record SignupRequest(@NotBlank(message = "ユーザー名は必須です") String username,
        @NotBlank @Size(min = 6, max = 20) String password) {

}
