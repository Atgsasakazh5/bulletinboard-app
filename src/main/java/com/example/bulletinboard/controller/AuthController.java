package com.example.bulletinboard.controller;

import jakarta.validation.*;

import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.bulletinboard.dto.*;
import com.example.bulletinboard.entity.*;
import com.example.bulletinboard.security.*;
import com.example.bulletinboard.service.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthController(AuthService authService, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody SignupRequest signupRequest) {

        User createdUser = authService.registerUser(signupRequest);

        UserResponse response = new UserResponse(createdUser.getId(), createdUser.getUsername());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // AuthController.java の authenticateUser メソッド
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken(authentication);

        // ★★★ 認証情報からUserDetailsを取得し、ユーザー名を得る ★★★
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        // ★★★ JwtResponseにトークンとユーザー名をセットして返す ★★★
        JwtResponse jwtResponse = new JwtResponse(jwt, username);
        return ResponseEntity.ok(jwtResponse);
    }
}
