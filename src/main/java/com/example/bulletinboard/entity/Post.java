package com.example.bulletinboard.entity;

import java.time.*;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id // 主キー
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // 多対一のリレーションシップ
    @JoinColumn(name = "user_id", nullable = false) // postsテーブルにuser_idカラムを作成
    private User user;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}