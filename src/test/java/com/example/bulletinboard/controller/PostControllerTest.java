package com.example.bulletinboard.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.bulletinboard.dto.PostCreateRequest;
import com.example.bulletinboard.entity.Post;
import com.example.bulletinboard.entity.User;
import com.example.bulletinboard.repository.PostRepository;
import com.example.bulletinboard.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Post testPost1;

    // 各テストの実行前に、このメソッドが毎回実行される
    @BeforeEach
    void setUp() {
        // 既存のデータをクリア
        postRepository.deleteAll();
        userRepository.deleteAll();

        // テスト用ユーザーを作成してDBに保存
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password"));
        userRepository.save(testUser);

        // テスト用投稿を1件作成してDBに保存
        testPost1 = new Post(null, "Author1", "Content1", LocalDateTime.now());
        postRepository.save(testPost1);
    }

    @Test
    @DisplayName("全件取得API - 正常系")
    void testFindAll_shouldReturnCorrectHttpResponse() throws Exception {
        // Arrange
        // @BeforeEachで1件、ここで1件、合計2件の投稿をDBに保存
        postRepository.save(new Post(null, "Author2", "Content2", LocalDateTime.now().plusMinutes(1)));

        // Act & Assert
        mockMvc.perform(get("/api/posts")).andExpect(status().isOk()).andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].author").value("Author2")); // 新しい順
    }

    @Test
    @DisplayName("ID検索で投稿が見つかった場合のテスト-正常系")
    void testFindById_shouldReturnCorrectHttpStatus_whenIdFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/posts/{id}", testPost1.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testPost1.getId())).andExpect(jsonPath("$.author").value("Author1"));
    }

    @Test
    @DisplayName("ID検索で投稿が見つからなかった場合のテスト-異常系")
    void testFindById_shouldReturnBadStatus_whenIdNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/posts/{id}", 9999L)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("新規作成APIのテスト-正常系")
    void testCreatePost_shouldReturnCorrectHttpStatus() throws Exception {
        // Arrange
        PostCreateRequest requestDto = new PostCreateRequest("New Author", "New Content");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // Act & Assert
        mockMvc.perform(post("/api/posts").with(csrf()).with(user("testuser")) // @BeforeEachで作成したユーザーで認証
                .contentType(MediaType.APPLICATION_JSON).content(requestBody)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.author").value("New Author"));

        // @BeforeEachの1件 + このテストで1件 = 合計2件になっていることを確認
        assertThat(postRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("新規作成に必要な項目が不足している場合のテスト-異常系")
    void testCreatePost_shouldReturnBadStatus_whenSendWrongRequest() throws Exception {
        // Arrange
        PostCreateRequest requestDto = new PostCreateRequest("", "Content"); // ユーザー名が空
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // Act & Assert
        mockMvc.perform(post("/api/posts").with(csrf()).with(user("testuser")).contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("更新成功-正常系")
    void testUpdatePost_shouldReturnUpdatedPost_whenIdExists() throws Exception {
        // Arrange
        PostCreateRequest requestDto = new PostCreateRequest("Updated Author", "Updated Content");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // Act & Assert
        mockMvc.perform(put("/api/posts/{id}", testPost1.getId()).with(csrf()).with(user("testuser"))
                .contentType(MediaType.APPLICATION_JSON).content(requestBody)).andExpect(status().isOk())
                .andExpect(jsonPath("$.author").value("Updated Author"));

        // DBの値が本当に更新されたかを確認
        User updatedUser = userRepository.findByUsername("testuser").get();
        assertThat(updatedUser.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("更新失敗-異常系 (IDが見つからない)")
    void testUpdatePost_shouldReturn404_whenIdNotFound() throws Exception {
        // Arrange
        PostCreateRequest requestDto = new PostCreateRequest("A", "テストです");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // Act & Assert
        mockMvc.perform(put("/api/posts/{id}", 9999L).with(csrf()).with(user("testuser"))
                .contentType(MediaType.APPLICATION_JSON).content(requestBody)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("削除が成功する場合-正常系")
    void testDeleteById_shouldReturn204_whenIdExists() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/posts/{id}", testPost1.getId()).with(csrf()).with(user("testuser")))
                .andExpect(status().isNoContent());

        // DBから削除されたかを確認
        assertThat(postRepository.findById(testPost1.getId())).isEmpty();
    }

    @Test
    @DisplayName("IDが存在せず削除できない場合-異常系")
    void testDeleteById_shouldReturnBadStatus_whenIdNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/posts/{id}", 9999L).with(csrf()).with(user("testuser")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("新規作成APIのテスト-未認証")
    void createPost_unauthenticated_shouldReturn401() throws Exception {
        // Arrange
        PostCreateRequest requestDto = new PostCreateRequest("New Author", "New Content");
        String requestBody = objectMapper.writeValueAsString(requestDto);

        // Act & Assert
        mockMvc.perform(post("/api/posts") // .with(user(...)) を付けずに実行
                .with(csrf()).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isForbidden()); // 401 Unauthorizedを期待
    }
}