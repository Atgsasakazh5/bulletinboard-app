package com.example.bulletinboard.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;

import com.example.bulletinboard.entity.*;
import com.example.bulletinboard.repository.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Test
    @DisplayName("全件取得テスト-正常系")
    void testFindAll_shouldReturnAllPostList() {

        // Arrange
        Post post1 = new Post(1L, "Taro", "Content 1", LocalDateTime.now());
        Post post2 = new Post(2L, "Jiro", "Content 2", LocalDateTime.now().minusDays(1));
        List<Post> expectedPosts = List.of(post1, post2);

        // postRepositoryのメソッドが呼ばれたらモックデータを返すように設定
        when(postRepository.findAllByOrderByCreatedAtDesc()).thenReturn(expectedPosts);

        // Act
        List<Post> actualPosts = postService.findAll();

        // Assert
        assertThat(actualPosts).isEqualTo(expectedPosts);
        assertThat(actualPosts.size()).isEqualTo(2);

        verify(postRepository, times(1)).findAllByOrderByCreatedAtDesc();   //ちょうど一回メソッドが呼ばれたことを検証
        
    }
}
