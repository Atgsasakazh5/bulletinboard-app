package com.example.bulletinboard.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.bulletinboard.dto.PostCreateRequest;
import com.example.bulletinboard.dto.PostResponse; // ★★★ importを変更 ★★★
import com.example.bulletinboard.service.PostService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/posts") // ★★★ ベースパスを/api/postsに集約 ★★★
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping // GET /api/posts
    public List<PostResponse> findAll() { // ★★★ 戻り値の型を変更 ★★★
        return postService.findAll();
    }

    @PostMapping // POST /api/posts
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse createPost(@Valid @RequestBody PostCreateRequest request,
                                   @AuthenticationPrincipal UserDetails userDetails) { // ★★★ 戻り値の型を変更 ★★★
        return postService.createPost(request, userDetails);
    }

    @GetMapping("/{id}") // GET /api/posts/{id}
    public PostResponse findById(@PathVariable Long id) { // ★★★ 戻り値の型を変更 ★★★
        return postService.findById(id);
    }

    @PutMapping("/{id}") // ★★★ @PutMappingアノテーションを追加 ★★★
    public PostResponse updatePost(@PathVariable Long id,
                                   @Valid @RequestBody PostCreateRequest request,
                                   @AuthenticationPrincipal UserDetails userDetails) { // ★★★ 戻り値の型を変更 ★★★
        return postService.updatePost(id, request, userDetails);
    }

    @DeleteMapping("/{id}") // DELETE /api/posts/{id}
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails) {
        postService.deleteById(id, userDetails);
    }
}