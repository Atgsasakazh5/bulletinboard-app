package com.example.bulletinboard.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.runtime.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.test.web.servlet.*;

import com.example.bulletinboard.config.*;
import com.example.bulletinboard.dto.*;
import com.example.bulletinboard.entity.*;
import com.example.bulletinboard.exception.*;
import com.example.bulletinboard.security.*;
import com.example.bulletinboard.service.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("signupのテスト-正常系")
    void testRegisterUser_shouldReturn201_whentakeCorrectRequest() throws Exception {

        // Arange
        SignupRequest dto = new SignupRequest("A", "password123");
        User expectedUser = new User(1L, "A", "hashed_password");

        when(authService.registerUser(any(SignupRequest.class))).thenReturn(expectedUser);

        // Act Assert
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("A"));

    }

    @Test
    @DisplayName("signupの空ユーザー名テスト-異常系")
    void testRegisterUser_shouldReturn400_whenUsernameIsInvalid() throws Exception {

        // Arange
        SignupRequest dto = new SignupRequest("", "password123");

        // Act Assert
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registerUser(any());
    }

    @Test
    @DisplayName("signupのユーザー名重複テスト-異常系")
    void testRegisterUser_shouldReturn409_whenUsernameExists() throws Exception {

        // Arange
        SignupRequest dto = new SignupRequest("A", "password123");
        when(authService.registerUser(any(SignupRequest.class)))
                .thenThrow(new UserAlreadyExistsException("既にユーザー名が存在します"));

        // Act Assert
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isConflict());

        verify(authService, times(1)).registerUser(any(SignupRequest.class));
    }

    @Test
    @DisplayName("loginのテスト-正常系")
    void testAuthenticateUser_正しい資格情報で200OKとJWTを返す() throws Exception {

        // Arange
        LoginRequest dto = new LoginRequest("A", "password123");

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        when(jwtUtils.generateToken(authentication)).thenReturn("mocked.jwt.token");

        // Act Assert
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").value("mocked.jwt.token"));

        verify(jwtUtils, times(1)).generateToken(any(Authentication.class));

    }

    @Test
    @DisplayName("loginのテスト-異常系")
    void testAuthenticateUser_不正な資格情報で401を返す() throws Exception {

        // Arange
        LoginRequest dto = new LoginRequest("A", "password123");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        // Act Assert
        String requestBody = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isUnauthorized());

        verify(jwtUtils, never()).generateToken(any());

    }
}
