package com.example.bulletinboard.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;

import com.example.bulletinboard.dto.*;
import com.example.bulletinboard.entity.*;
import com.example.bulletinboard.exception.*;
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

        verify(postRepository, times(1)).findAllByOrderByCreatedAtDesc(); // ちょうど一回メソッドが呼ばれたことを検証

    }

    @Test
    @DisplayName("新規作成のテスト-正常系")
    void testCreatePost_shouldReturnPostRepositorySaveObject() {

        // Arange
        PostCreateRequest testRequest = new PostCreateRequest("A", "テストです");

        Post testPost = new Post();
        testPost.setAuthor(testRequest.author());
        testPost.setContent(testRequest.content());
        testPost.setId(1L);
        testPost.setCreatedAt(LocalDateTime.of(2025, 6, 11, 11, 0));

        when(postRepository.save(any(Post.class))).thenReturn(testPost); // postrepositoryはモック化

        // Act
        Post actualPost = postService.createPost(testRequest);

        // Assert
        assertThat(actualPost).isEqualTo(testPost); // 同一か

        ArgumentCaptor<Post> postCapture = ArgumentCaptor.forClass(Post.class); // ArgumentCaptorでPostの要素も確認

        // postRepositoryのsaveメソッドが呼ばれたことを確認し、その時の引数をpostCaptorに捕獲させる
        verify(postRepository).save(postCapture.capture());

        Post capturedPost = postCapture.getValue(); // captureしたオブジェクトの取得

        assertThat(capturedPost.getAuthor()).isEqualTo(testRequest.author());
        assertThat(capturedPost.getContent()).isEqualTo(testRequest.content());
        assertThat(capturedPost.getId()).isNull();
        assertThat(capturedPost.getCreatedAt()).isNotNull();

    }

    @Test
    @DisplayName("投稿のID検索でIDが見つかる場合-正常系")
    void testFindById_shouldReturnCorrectPost_whenIdFound() {

        // Arange
        Post expectedPost = new Post();
        expectedPost.setAuthor("A");
        expectedPost.setContent("テスト");
        expectedPost.setId(1L);
        expectedPost.setCreatedAt(LocalDateTime.of(2025, 6, 11, 11, 0));

        when(postRepository.findById(1L)).thenReturn(Optional.of(expectedPost));

        // Act
        Post actualPost = postService.findById(1L);

        // Assert
        assertThat(actualPost).isEqualTo(expectedPost);
        verify(postRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("投稿のID検索でIDが見つからなかった場合-異常系")
    void testFindById_shouldReturnResourceNotFoundException_whenIdNotFound() {

        // Arange
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act Assert
        assertThatThrownBy(() -> {
            // この中で例外を投げるはずのメソッドを呼び出す
            postService.findById(99L);
        }).isInstanceOf(ResourceNotFoundException.class) // 設定した例外と同じ型か
                .hasMessage("Post not found with id: 99"); // 例外メッセージが正しいか検証

    }
}
