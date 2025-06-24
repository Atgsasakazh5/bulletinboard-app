package com.example.bulletinboard.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.bulletinboard.dto.LoginRequest;
import com.example.bulletinboard.dto.SignupRequest;
import com.example.bulletinboard.entity.User;
import com.example.bulletinboard.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest // アプリケーションの全機能を読み込む
@AutoConfigureMockMvc // MockMvcを自動設定する
@Transactional // 各テスト後にデータベースの状態を元に戻す
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("signupのテスト-正常系")
    void testRegisterUser_shouldReturn201_whenRequestIsCorrect() throws Exception {
        // Arrange
        SignupRequest dto = new SignupRequest("testuser", "password123");

        // Act & Assert
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc.perform(
                post("/api/auth/signup").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.username").value("testuser"));

        // データベースにユーザーが正しく保存されたかを検証
        assertThat(userRepository.findByUsername("testuser")).isPresent();
    }

    @Test
    @DisplayName("signupの空ユーザー名テスト-異常系")
    void testRegisterUser_shouldReturn400_whenUsernameIsInvalid() throws Exception {
        // Arrange
        SignupRequest dto = new SignupRequest("", "password123"); // 不正なリクエスト

        // Act & Assert
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc.perform(
                post("/api/auth/signup").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("signupのユーザー名重複テスト-異常系")
    void testRegisterUser_shouldReturn409_whenUsernameExists() throws Exception {
        // Arrange: 先に重複するユーザーをデータベースに保存しておく
        userRepository.save(new User(null, "existinguser", "password123"));

        SignupRequest dto = new SignupRequest("existinguser", "password123456");

        // Act & Assert
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc.perform(
                post("/api/auth/signup").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("loginのテスト-正常系")
    void testAuthenticateUser_正しい資格情報で200OKとJWTを返す() throws Exception {
        // Arrange: データベースにテストユーザーを実際に保存する
        User testUser = new User();
        testUser.setUsername("A");
        testUser.setPassword(passwordEncoder.encode("password123")); // パスワードはハッシュ化して保存
        userRepository.save(testUser);

        LoginRequest dto = new LoginRequest("A", "password123");

        // Act & Assert
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/api/auth/login").with(csrf()) // CSRF対策はそのまま
                .contentType(MediaType.APPLICATION_JSON).content(requestBody)).andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty()); // トークンが空でないことを検証
    }

    @Test
    @DisplayName("loginのテスト-異常系（パスワード間違い）")
    void testAuthenticateUser_不正な資格情報で401を返す() throws Exception {
        // Arrange: 正しいユーザー情報をDBに保存
        User testUser = new User();
        testUser.setUsername("A");
        testUser.setPassword(passwordEncoder.encode("correct_password"));
        userRepository.save(testUser);

        LoginRequest dto = new LoginRequest("A", "wrong_password"); // 間違ったパスワードでログイン試行

        // Act & Assert
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc.perform(
                post("/api/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isForbidden());
    }
}