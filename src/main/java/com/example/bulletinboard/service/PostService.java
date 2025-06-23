package com.example.bulletinboard.service;

import java.time.*;
import java.util.*;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.*;

import com.example.bulletinboard.dto.*;
import com.example.bulletinboard.entity.*;
import com.example.bulletinboard.exception.*;
import com.example.bulletinboard.repository.*;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {
    private final PostRepository postRepository;

    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }


    // 全件取得
    public List<Post> findAll() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    // 新規登録
    // ★★★ PostService の createPost メソッドを置き換え ★★★
    public Post createPost(PostCreateRequest request, UserDetails userDetails) {
        // ユーザー名からUserエンティティを検索
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Post newPost = new Post();
        // 投稿者名ではなく、Userエンティティそのものを設定
        newPost.setUser(user);
        newPost.setContent(request.content());
        newPost.setCreatedAt(LocalDateTime.now());
        return postRepository.save(newPost);
    }

    // IDで投稿検索
    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
    }

    // ID指定で投稿削除
    @Transactional
    public Post updatePost(Long id, PostCreateRequest request, UserDetails userDetails) {
        Post targetPost = findById(id);
        String requestUsername = userDetails.getUsername();

        // ★★★ 所有権チェックを追加 ★★★
        if (!targetPost.getUser().getUsername().equals(requestUsername)) {
            throw new AccessDeniedException("この投稿を編集する権限がありません。");
        }

        targetPost.setContent(request.content());
        return postRepository.save(targetPost);
    }

    @Transactional
    public void deleteById(Long id, UserDetails userDetails) {
        Post targetPost = findById(id);
        String requestUsername = userDetails.getUsername();

        // ★★★ 所有権チェックを追加 ★★★
        if (!targetPost.getUser().getUsername().equals(requestUsername)) {
            throw new AccessDeniedException("この投稿を削除する権限がありません。");
        }

        postRepository.delete(targetPost);
    }
}
