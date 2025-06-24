package com.example.bulletinboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.bulletinboard.dto.PostCreateRequest;
import com.example.bulletinboard.dto.PostResponse;
import com.example.bulletinboard.entity.Post;
import com.example.bulletinboard.entity.User;
import com.example.bulletinboard.exception.ResourceNotFoundException;
import com.example.bulletinboard.repository.PostRepository;
import com.example.bulletinboard.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("全件取得テスト-正常系")
    void testFindAll_shouldReturnAllPostList() {
        // Arrange
        User user1 = new User(1L, "Taro", "pass");
        User user2 = new User(2L, "Jiro", "pass");
        // ★★★ Postエンティティの正しいコンストラクタに合わせて修正 ★★★
        Post post1 = new Post(1L, user1, "Content 1", LocalDateTime.now());
        Post post2 = new Post(2L, user2, "Content 2", LocalDateTime.now().plusDays(1));
        // 新しい順なので、post2, post1の順で返す
        List<Post> expectedPosts = List.of(post2, post1);

        when(postRepository.findAllByOrderByCreatedAtDesc()).thenReturn(expectedPosts);

        // Act
        List<PostResponse> actualResponses = postService.findAll();

        // Assert
        assertThat(actualResponses).hasSize(2);
        assertThat(actualResponses.get(0).authorUsername()).isEqualTo("Jiro");
        verify(postRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("新規作成のテスト-正常系")
    void testCreatePost_shouldReturnCorrectPost() {
        // Arange
        PostCreateRequest testRequest = new PostCreateRequest("テストです");
        UserDetails userDetails = mock(UserDetails.class);
        User user = new User(1L, "testuser", "pass");
        // ★★★ Postエンティティの正しいコンストラクタに合わせて修正 ★★★
        Post savedPost = new Post(1L, user, "テストです", LocalDateTime.now());

        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        // Act
        PostResponse actualResponse = postService.createPost(testRequest, userDetails);

        // Assert
        ArgumentCaptor<Post> postCapture = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCapture.capture());
        Post capturedPost = postCapture.getValue();

        assertThat(capturedPost.getUser().getUsername()).isEqualTo("testuser");
        assertThat(capturedPost.getContent()).isEqualTo("テストです");
        assertThat(actualResponse.authorUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("投稿のID検索でIDが見つかる場合-正常系")
    void testFindById_shouldReturnCorrectPost_whenIdFound() {
        // Arange
        User user = new User(1L, "A", "pass");
        // ★★★ Postエンティティの正しいコンストラクタに合わせて修正 ★★★
        Post expectedPost = new Post(1L, user, "テスト", LocalDateTime.now());
        when(postRepository.findById(1L)).thenReturn(Optional.of(expectedPost));

        // Act
        PostResponse actualResponse = postService.findById(1L);

        // Assert
        assertThat(actualResponse.id()).isEqualTo(1L);
        assertThat(actualResponse.authorUsername()).isEqualTo("A");
        verify(postRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("投稿のID検索でIDが見つからなかった場合-異常系")
    void testFindById_shouldThrowResourceNotFoundException_whenIdNotFound() {
        // Arange
        when(postRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> postService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Post not found with id: 99");
    }

    @Test
    @DisplayName("IDが存在し、所有者による削除が成功する場合")
    void testDeleteById_shouldCallDelete_whenIdExistsAndUserIsOwner() {
        // Arrange
        UserDetails userDetails = mock(UserDetails.class);
        User owner = new User(1L, "owner", "pass");
        // ★★★ Postエンティティの正しいコンストラクタに合わせて修正 ★★★
        Post post = new Post(1L, owner, "content", LocalDateTime.now());

        when(userDetails.getUsername()).thenReturn("owner");
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        doNothing().when(postRepository).delete(any(Post.class));

        // Act
        postService.deleteById(1L, userDetails);

        // Assert
        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).delete(any(Post.class));
    }

    @Test
    @DisplayName("投稿の所有者でない場合に削除しようとするとAccessDeniedExceptionをスローする-異常系")
    void testDeleteById_shouldThrowAccessDeniedException_whenUserIsNotOwner() {
        // Arrange
        UserDetails userDetails = mock(UserDetails.class);
        User owner = new User(1L, "owner", "pass");
        // ★★★ Postエンティティの正しいコンストラクタに合わせて修正 ★★★
        Post post = new Post(1L, owner, "content", LocalDateTime.now());

        when(userDetails.getUsername()).thenReturn("not_owner"); // 別のユーザー
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // Act & Assert
        assertThatThrownBy(() -> postService.deleteById(1L, userDetails))
                .isInstanceOf(AccessDeniedException.class);

        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("IDが見つかり、所有者による更新が成功する場合")
    void testUpdatePost_shouldReturnUpdatedPost_whenIdExistsAndUserIsOwner() {
        // Arange
        UserDetails userDetails = mock(UserDetails.class);
        User owner = new User(1L, "owner", "pass");
        // ★★★ Postエンティティの正しいコンストラクタに合わせて修正 ★★★
        Post existingPost = new Post(1L, owner, "更新前", LocalDateTime.now());
        PostCreateRequest requestDto = new PostCreateRequest("更新後");

        when(userDetails.getUsername()).thenReturn("owner");
        when(postRepository.findById(1L)).thenReturn(Optional.of(existingPost));
        // saveが呼ばれたら、渡された引数をそのまま返すように設定
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        postService.updatePost(1L, requestDto, userDetails);

        // Assert
        ArgumentCaptor<Post> postCapture = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(postCapture.capture());
        Post capturedPost = postCapture.getValue();

        assertThat(capturedPost.getContent()).isEqualTo("更新後");
        assertThat(capturedPost.getUser().getUsername()).isEqualTo("owner");
    }

    @Test
    @DisplayName("投稿の所有者でない場合に更新しようとするとAccessDeniedExceptionをスローする-異常系")
    void testUpdatePost_shouldThrowAccessDeniedException_whenUserIsNotOwner() {
        // Arrange
        UserDetails userDetails = mock(UserDetails.class);
        User owner = new User(1L, "owner", "pass");
        // ★★★ Postエンティティの正しいコンストラクタに合わせて修正 ★★★
        Post post = new Post(1L, owner, "content", LocalDateTime.now());
        PostCreateRequest requestDto = new PostCreateRequest("更新しようとする");

        when(userDetails.getUsername()).thenReturn("not_owner"); // 別のユーザー
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // Act & Assert
        assertThatThrownBy(() -> postService.updatePost(1L, requestDto, userDetails))
                .isInstanceOf(AccessDeniedException.class);

        verify(postRepository, never()).save(any(Post.class));
    }
}