package com.example.bulletinboard.service;

import java.util.*;

import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;

import com.example.bulletinboard.dto.*;
import com.example.bulletinboard.entity.*;
import com.example.bulletinboard.exception.*;
import com.example.bulletinboard.repository.*;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(SignupRequest signupRequest) {

        // ユーザー名の重複チェック
        Optional<User> existingUser = userRepository.findByUsername(signupRequest.username());
        if (existingUser.isPresent()) {
            throw new UserAlreadyExistsException("このユーザー名は既に使用されています。");
        }

        User newUser = new User();
        newUser.setUsername(signupRequest.username());
        //パスワードは必ずハッシュ化してデータベースに保存
        String hashPassword = passwordEncoder.encode(signupRequest.password());
        newUser.setPassword(hashPassword);

        return userRepository.save(newUser);

    }

}
