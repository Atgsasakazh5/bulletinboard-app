package com.example.bulletinboard.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.*;
import java.util.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.http.*;
import org.springframework.test.web.servlet.*;

import com.example.bulletinboard.dto.*;
import com.example.bulletinboard.entity.*;
import com.example.bulletinboard.service.*;
import com.fasterxml.jackson.databind.*;

@WebMvcTest(PostController.class)
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @Test
    @DisplayName("全件取得テスト-正常系")
    void testFindAll_shouldReturnCorrectHttpResponse() throws Exception {

        // Arrange
        Post post1 = new Post(1L, "Taro", "Content 1", LocalDateTime.now());
        Post post2 = new Post(2L, "Jiro", "Content 2", LocalDateTime.now().minusDays(1));
        List<Post> expectedPosts = List.of(post1, post2);

        // postRepositoryのメソッドが呼ばれたらモックデータを返すように設定
        when(postService.findAll()).thenReturn(expectedPosts);

        // Act
        mockMvc.perform(get("/api/posts")) // /api/postsへGETリクエストを疑似的に実行
                .andExpect(status().isOk()) // HTTPステータスが200OKであることを期待
                .andExpect(jsonPath("$.size()").value(2)) // リストサイズが２であることを期待
                .andExpect(jsonPath("$[0].author").value("Taro")) // Jsonの ０番目のauthorがTaroであることを期待
                .andExpect(jsonPath("$[1].id").value(2L)); // json1番目のidが２であることを期待

        // Serviceのメソッドがちょうど一回呼ばれたことを検証
        verify(postService, times(1)).findAll();

    }

    @Test
    @DisplayName("新規作成APIのテスト-正常系")
    void testCreatePost_shouldReturnCorrectHttpStatus() throws Exception {

        // Arange
        PostCreateRequest request = new PostCreateRequest("A", "テストです");
        Post expectedPost = new Post(1L, request.author(), request.content(), LocalDateTime.now());

        when(postService.createPost(any(PostCreateRequest.class))).thenReturn(expectedPost);

        // Act
        // Assert objectMapperでjsonを文字列にしてPOSTリクエストを疑似実行
        String requestBody = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/api/posts").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.author").value("A"));

        verify(postService, times(1)).createPost(any(PostCreateRequest.class));

    }

    @Test
    @DisplayName("新規作成に必要な項目が不足している場合のテスト-異常系")
    void testCreatePost_shouldReturnBadStatus_whenSendWrongRequest() throws Exception {

        // Arange
        PostCreateRequest request = new PostCreateRequest(null, "テストです");

        // Act
        // Assert objectMapperでjsonを文字列にしてPOSTリクエストを疑似実行
        String requestBody = objectMapper.writeValueAsString(request);
        mockMvc.perform(post("/api/posts").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest());

        verify(postService, never()).createPost(any(PostCreateRequest.class));
    }
}
