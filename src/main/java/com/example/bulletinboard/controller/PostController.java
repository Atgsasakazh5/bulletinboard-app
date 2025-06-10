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

    @PostMapping("/posts") // POSTれクエスト
    @ResponseStatus(HttpStatus.CREATED)
    public Post createPost(@Valid @RequestBody PostCreateRequest request) {
        return postService.createPost(request);
    }
}
