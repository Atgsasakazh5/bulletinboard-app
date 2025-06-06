package com.example.bulletinboard.entity;

import java.time.*;

import jakarta.persistence.*;

import org.springframework.data.annotation.Id;

import lombok.*;

@Entity
@Table(name ="posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    
    @Id     //このフィールドが主キーであることを示す
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)   //NotNULL制約
    private String author;
    
    @Column(nullable = false, length = 1000)    //Not Null制約と文字数制限
    private String content;
    
    @Column(name = "created_at", nullable = false, updatable = false)   //カラム名を指定、Not NULL制約、更新も不可
    private LocalDateTime createdAt;

    
}
