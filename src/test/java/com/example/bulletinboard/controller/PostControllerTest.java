package com.example.bulletinboard.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import java.time.*;
import java.util.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.test.web.servlet.*;

import com.example.bulletinboard.config.*;
import com.example.bulletinboard.dto.*;
import com.example.bulletinboard.entity.*;
import com.example.bulletinboard.exception.*;
import com.example.bulletinboard.service.*;
import com.fasterxml.jackson.databind.*;

@WebMvcTest(PostController.class)
@Import(SecurityConfig.class) // Test configまで自動で読み込んでくれないので、明示する
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
        // Assert
        // objectMapperでjsonを文字列にしてPOSTリクエストを疑似実行
        String requestBody = objectMapper.writeValueAsString(request);
        mockMvc.perform(
                post("/api/posts").with(user("user")).contentType(MediaType.APPLICATION_JSON).content(requestBody))
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
        mockMvc.perform(
                post("/api/posts").with(user("user")).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest());

        verify(postService, never()).createPost(any(PostCreateRequest.class));
    }

    @Test
    @DisplayName("ID検索で投稿が見つかった場合のテスト-正常系")
    void testFindById_shouldReturnCorrectHttpStatus_whenIdFound() throws Exception {

        // Arange
        Post expectedPost = new Post(1L, "A", "テスト", LocalDateTime.now());

        when(postService.findById(1L)).thenReturn(expectedPost);

        // Act Assert
        mockMvc.perform(get("/api/posts/{id}", 1L).with(user("user"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.author").value("A"));

        // 1回だけfindById(1L)が呼び出されるか
        verify(postService, times(1)).findById(1L);

    }

    @Test
    @DisplayName("ID検索で投稿が見つからなかった場合のテスト-異常系")
    void testFindById_shouldReturnBadStatus_whenIdNotFound() throws Exception {

        // Arange
        when(postService.findById(99L)).thenThrow(new ResourceNotFoundException("Post not found"));

        // Act Assert
        mockMvc.perform(get("/api/posts/{id}", 99L).with(user("user"))).andExpect(status().isNotFound());

        // 1回だけfindById(99L)が呼び出されるか
        verify(postService, times(1)).findById(99L);

    }

    @Test
    @DisplayName("削除が成功する場合-正常系")
    void testDeleteById_shouldReturn204_whenIdExists() throws Exception {

        // Arange 戻り値がvoidなので、何もしないようにする
        doNothing().when(postService).deleteById(1L);

        // Act Assert
        // 204を返すか確認
        mockMvc.perform(delete("/api/posts/{id}", 1L).with(user("user"))).andExpect(status().isNoContent());

        verify(postService, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("IDが存在せず削除できない場合-異常系")
    void testDeleteById_shouldReturnBadStatus_whenIdNotFound() throws Exception {

        // Arange voidメソッドが例外をスローする場合
        doThrow(new ResourceNotFoundException("Post not found")).when(postService).deleteById(99L);

        // Act Assert
        // 404を返すか確認
        mockMvc.perform(delete("/api/posts/{id}", 99L).with(user("user"))).andExpect(status().isNotFound());

        verify(postService, times(1)).deleteById(99L);
    }

    @Test
    @DisplayName("更新成功-正常系")
    void testUpdatePost_shouldReturnUpdatedPost_whenIdExists() throws Exception {

        // Arrange
        PostCreateRequest requestDto = new PostCreateRequest("A", "テストです");
        Post updatedPost = new Post(1L, requestDto.author(), requestDto.content(), LocalDateTime.now());

        when(postService.updatePost(eq(1L), any(PostCreateRequest.class))).thenReturn(updatedPost);

        // Act Assret
        String requestBody = objectMapper.writeValueAsString(requestDto);
        mockMvc.perform(put("/api/posts/{id}", 1L).with(user("user")).contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.author").value("A"));

        verify(postService, times(1)).updatePost(eq(1L), any(PostCreateRequest.class));

    }

    @Test
    @DisplayName("更新失敗-異常系")
    void testUpdatePost_shouldReturn404_whenIdNotFound() throws Exception {

        // Arrange
        PostCreateRequest requestDto = new PostCreateRequest("A", "テストです");
        when(postService.updatePost(eq(99L), any(PostCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Post not found"));

        // Act Assert
        String requestBody = objectMapper.writeValueAsString(requestDto);
        mockMvc.perform(put("/api/posts/{id}", 99L).with(user("user")).contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("新規作成APIのテスト-未認証")
    void createPost_unauthenticated_shouldReturn401() throws Exception {

        // Arange
        String requestBody = "{\"author\":\"test\",\"content\":\"test content\"}";

        // Act assert
        mockMvc.perform(post("/api/posts").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isUnauthorized());
    }
}
