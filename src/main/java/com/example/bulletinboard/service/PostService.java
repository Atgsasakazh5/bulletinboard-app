package com.example.bulletinboard.service;

import java.util.*;

import org.springframework.stereotype.*;

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
}
