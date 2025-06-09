package com.example.bulletinboard.controller;

import java.util.*;

import org.springframework.web.bind.annotation.*;

import com.example.bulletinboard.entity.*;
import com.example.bulletinboard.service.*;

@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/posts")
    public List<Post> findAll() {
        return postService.findAll();
    }
}
