package com.example.bulletinboard.dto;

import java.time.LocalDateTime;

import com.example.bulletinboard.entity.Post;

// Postエンティティから、クライアントに返すための情報を抽出するDTO
public record PostResponse(
        Long id,
        String authorUsername, // Userエンティティからユーザー名を取得
        String content,
        LocalDateTime createdAt
) {
    // PostエンティティをPostResponseに変換するファクトリメソッド
    public static PostResponse fromEntity(Post post) {
        return new PostResponse(
                post.getId(),
                post.getUser().getUsername(), // 関連するUserからユーザー名を取得
                post.getContent(),
                post.getCreatedAt()
        );
    }
}