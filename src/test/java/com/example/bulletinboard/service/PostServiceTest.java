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

    @Test
    @DisplayName("IDが存在し、削除が成功する場合")
    void testDeleteById_shouldRunDeleteOnece_whenIdFound() {

        // Arrange
        when(postRepository.existsById(1L)).thenReturn(true);
        doNothing().when(postRepository).deleteById(1L);

        // Act
        postService.deleteById(1L);

        // Assert
        verify(postRepository, times(1)).existsById(1L);
        verify(postRepository, times(1)).deleteById(1L);

    }

    @Test
    @DisplayName("IDが存在しない場合")
    void testDeleteById_shouldReturnResourceNotFoundException_whenIdNotFound() {

        // Arange
        when(postRepository.existsById(anyLong())).thenReturn(false);

        // Act Assert
        assertThatThrownBy(() -> {
            postService.deleteById(99L);
        }).isInstanceOf(ResourceNotFoundException.class).hasMessage("Post not found with id: 99");

        verify(postRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("IDが見つかり、更新が成功する場合")
    void testUpdatePost_shouldReturnCorrectPost_whenIdExists() {

        // Arange
        var requestDto = new PostCreateRequest("B", "更新後");
        var existingPost = new Post(1L, "A", "更新前", LocalDateTime.now());
        var updatedPost = new Post(1L, "B", "更新後", LocalDateTime.now());

        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));
        when(postRepository.save(any(Post.class))).thenReturn(updatedPost);

        // Act
        var actualPost = postService.updatePost(1L, requestDto);

        // Assert
        ArgumentCaptor<Post> postCapture = ArgumentCaptor.forClass(Post.class);
        assertThat(actualPost).isEqualTo(updatedPost);

        // postRepositoryのsaveメソッドが呼ばれたことを確認し、その時の引数をpostCaptorに捕獲させる
        verify(postRepository).save(postCapture.capture());

        Post capturedPost = postCapture.getValue(); // captureしたオブジェクトの取得

        assertThat(capturedPost.getAuthor()).isEqualTo(requestDto.author());
        assertThat(capturedPost.getContent()).isEqualTo(requestDto.content());
        assertThat(capturedPost.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("IDが見つからず、更新できない場合")
    void testUpdatePost_shouldReturnResourceNotFoundException_whenIdNotFound() {

        // Arrange
        var requestDto = new PostCreateRequest("B", "更新後");
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        // Act Assrt
        assertThatThrownBy(() -> {
            postService.updatePost(99L, requestDto);
        }).isInstanceOf(ResourceNotFoundException.class).hasMessage("Post not found with id: 99");

        verify(postRepository, never()).save(any(Post.class));
    }
}
