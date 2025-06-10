package com.example.bulletinboard.service;

import java.time.*;
import java.util.*;

import org.springframework.stereotype.*;

import com.example.bulletinboard.dto.*;
import com.example.bulletinboard.entity.*;
import com.example.bulletinboard.repository.*;

@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> findAll() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public Post createPost(PostCreateRequest request) {
        Post newPost = new Post();
        newPost.setAuthor(request.author()); // recordなのでゲッターはフィールド名で自動生成
        newPost.setContent(request.content());
        newPost.setCreatedAt(LocalDateTime.now());
        Post savedPost = postRepository.save(newPost); // saveメソッドは継承元に含まれており、渡したエンティティをIDが採番された状態で保存し、戻り値として渡してくれる
        return savedPost;
    }
}
