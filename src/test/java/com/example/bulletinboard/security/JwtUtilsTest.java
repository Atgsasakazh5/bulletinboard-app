package com.example.bulletinboard.security;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.security.core.*;
import org.springframework.security.core.userdetails.*;

@SpringBootTest
class JwtUtilsTest {

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    @DisplayName("jwtUtilsのトークン生成テスト")
    void testGenerateTolen() {

        // Arange
        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(userDetails.getUsername()).thenReturn("testuser");

        // Act
        String token = jwtUtils.generateToken(authentication);

        // Assert
        assertThat(token).isNotNull().isNotEmpty();
    }

}
