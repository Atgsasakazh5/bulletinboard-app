package com.example.bulletinboard.repository;

import java.util.*;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.*;

import com.example.bulletinboard.entity.*;

@Repository // Repository層のコンポーネントであることを示す
public interface PostRepository extends JpaRepository<Post, Long> {

    // Spring Data JPAにメソッド名を解析させ、適切なSQLを生成させる
    List<Post> findAllByOrderByCreatedAtDesc();
}
