package com.example.bulletinboard.service;

import java.util.*;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import com.example.bulletinboard.repository.*;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        super();
        this.userRepository = userRepository;
    }

    @Override
    @Transactional // データベースアクセスを伴うためトランザクションを有効にする
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // UserRepositoryを使ってユーザーを検索
        return userRepository.findByUsername(username)
                // ユーザーが見つかった場合、UserエンティティをUserDetailsオブジェクトに変換
                .map(user -> new org.springframework.security.core.userdetails.User(user.getUsername(),
                        user.getPassword(), new ArrayList<>()))
                // ユーザーが見つからなかった場合は例外をスロー
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

}
