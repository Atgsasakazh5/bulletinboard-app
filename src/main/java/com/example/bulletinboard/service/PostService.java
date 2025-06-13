package com.example.bulletinboard.service;

import java.time.*;
import java.util.*;

import org.springframework.stereotype.*;

import com.example.bulletinboard.dto.*;
import com.example.bulletinboard.entity.*;
import com.example.bulletinboard.exception.*;
import com.example.bulletinboard.repository.*;

@Service
public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // 全件取得
    public List<Post> findAll() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    // 新規登録
    public Post createPost(PostCreateRequest request) {
        Post newPost = new Post();
        newPost.setAuthor(request.author()); // recordなのでゲッターはフィールド名で自動生成
        newPost.setContent(request.content());
        newPost.setCreatedAt(LocalDateTime.now());
        // saveメソッドは継承元に含まれており、渡したエンティティをIDが採番された状態で保存し、戻り値として渡してくれる
        return postRepository.save(newPost);
    }

    // IDで投稿検索
    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
    }

    // ID指定で投稿削除
    public void deleteById(Long id) {
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Post not found with id: " + id);
        }
    }

    // ID指定で投稿を更新
    public Post updatePost(Long id, PostCreateRequest request) {
        // findByIdに渡し、IDが存在しなければ例外を投げてくれる
        Post targetPost = findById(id);

        targetPost.setAuthor(request.author());
        targetPost.setContent(request.content());

        return postRepository.save(targetPost);
    }
}
