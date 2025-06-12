package com.example.bulletinboard.controller;

import java.util.*;

import jakarta.validation.*;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.example.bulletinboard.dto.*;
import com.example.bulletinboard.entity.*;
import com.example.bulletinboard.service.*;

@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/posts") // GETリクエスト
    public List<Post> findAll() {
        return postService.findAll();
    }

    @PostMapping("/posts") // POSTリクエスト
    @ResponseStatus(HttpStatus.CREATED)
    public Post createPost(@Valid @RequestBody PostCreateRequest request) {
        return postService.createPost(request);
    }

    // idの投稿が存在しない場合はExceptionの@ResponseStatusからSpringがレスポンスを生成。
    // 200OKのStatusは省略可
    @GetMapping("/posts/{id}")
    public Post findById(@PathVariable Long id) {
        return postService.findById(id);
    }
    
    //idの一致する投稿が存在する場合に削除
    @DeleteMapping("/posts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        postService.deleteById(id);
    }
}
