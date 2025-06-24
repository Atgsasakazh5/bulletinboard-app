package com.example.bulletinboard.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bulletinboard.dto.PostCreateRequest;
import com.example.bulletinboard.dto.PostResponse; // ★★★ importを追加
import com.example.bulletinboard.entity.Post;
import com.example.bulletinboard.entity.User;
import com.example.bulletinboard.exception.ResourceNotFoundException;
import com.example.bulletinboard.repository.PostRepository;
import com.example.bulletinboard.repository.UserRepository;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /**
     * 全ての投稿を、作成日時の降順で取得します。
     *
     * @return 投稿のレスポンスDTOのリスト
     */
    public List<PostResponse> findAll() { // ★★★ 戻り値の型を変更 ★★★
        return postRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(PostResponse::fromEntity) // ★★★ 各PostをPostResponseに変換 ★★★
                .toList();
    }

    /**
     * 新しい投稿を作成し、データベースに保存します。
     *
     * @param request     新規投稿の内容を持つDTO
     * @param userDetails 認証済みユーザーの情報
     * @return 作成された投稿のレスポンスDTO
     */
    public PostResponse createPost(PostCreateRequest request, UserDetails userDetails) { // ★★★ 戻り値の型を変更 ★★★
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + userDetails.getUsername()));

        Post newPost = new Post();
        newPost.setUser(user);
        newPost.setContent(request.content());
        newPost.setCreatedAt(LocalDateTime.now());

        Post savedPost = postRepository.save(newPost);

        return PostResponse.fromEntity(savedPost); // ★★★ PostResponseに変換して返す ★★★
    }

    /**
     * 指定されたIDに対応する投稿を1件取得します。
     *
     * @param id 検索する投稿のID
     * @return 見つかった投稿のレスポンスDTO
     * @throws ResourceNotFoundException 指定されたIDの投稿が存在しない場合
     */
    public PostResponse findById(Long id) { // ★★★ 戻り値の型を変更 ★★★
        return postRepository.findById(id)
                .map(PostResponse::fromEntity) // ★★★ 見つかった場合にPostResponseに変換 ★★★
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
    }

    /**
     * 指定されたIDの投稿を、リクエストDTOの内容で更新します。
     *
     * @param id          更新する投稿のID
     * @param request     更新後の内容を持つDTO
     * @param userDetails 認証済みユーザーの情報
     * @return 更新された投稿のレスポンスDTO
     * @throws ResourceNotFoundException 指定されたIDの投稿が存在しない場合
     * @throws AccessDeniedException     投稿の所有者でない場合
     */
    @Transactional
    public PostResponse updatePost(Long id, PostCreateRequest request, UserDetails userDetails) { // ★★★ 戻り値の型を変更 ★★★
        Post targetPost = findByIdInternal(id); // 内部用のfindByIdメソッドを呼び出す
        String requestUsername = userDetails.getUsername();

        if (!targetPost.getUser().getUsername().equals(requestUsername)) {
            throw new AccessDeniedException("この投稿を編集する権限がありません。");
        }

        targetPost.setContent(request.content());
        Post updatedPost = postRepository.save(targetPost);

        return PostResponse.fromEntity(updatedPost); // ★★★ PostResponseに変換して返す ★★★
    }

    /**
     * 指定されたIDの投稿を削除します。
     *
     * @param id          削除する投稿のID
     * @param userDetails 認証済みユーザーの情報
     * @throws ResourceNotFoundException 指定されたIDの投稿が存在しない場合
     * @throws AccessDeniedException     投稿の所有者でない場合
     */
    @Transactional
    public void deleteById(Long id, UserDetails userDetails) {
        Post targetPost = findByIdInternal(id); // 内部用のfindByIdメソッドを呼び出す
        String requestUsername = userDetails.getUsername();

        if (!targetPost.getUser().getUsername().equals(requestUsername)) {
            throw new AccessDeniedException("この投稿を削除する権限がありません。");
        }

        postRepository.delete(targetPost);
    }

    // updatePostとdeleteByIdから呼び出される、内部用のfindById
    private Post findByIdInternal(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
    }
}