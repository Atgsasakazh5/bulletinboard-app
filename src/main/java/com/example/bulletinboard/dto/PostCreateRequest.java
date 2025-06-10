package com.example.bulletinboard.dto;

import jakarta.validation.constraints.*;

public record PostCreateRequest(
        
        @NotBlank(message = "投稿者名は必須です") String author,

        @NotBlank(message = "投稿内容は必須です") String content

) {

}
