package com.example.bulletinboard.security;

import java.io.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.*;
import org.springframework.util.*;
import org.springframework.web.filter.*;

import com.example.bulletinboard.service.*;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    public AuthTokenFilter(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsServiceImpl) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsServiceImpl;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // リクエストからJWTをパースする
            String jwt = parseJwt(request);

            // JWTが存在し、有効であれば
            if (jwt != null && jwtUtils.validateToken(jwt)) {
                // JWTからユーザー名を取得
                String username = jwtUtils.getUsernameFromToken(jwt);

                // ユーザー名からUserDtailsを取得
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 認証トークンを作成
                UsernamePasswordAuthenticationToken authenticatipn = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // securityContextに認証情報を設定
                SecurityContextHolder.getContext().setAuthentication(authenticatipn);
            }
        } catch (Exception e) {
            System.out.println("Cannot set user authentication: " + e.getMessage());
        }

        // 次のフィルターに処理を渡す
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {

        String headAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headAuth) && headAuth.startsWith("Bearer ")) {
            return headAuth.substring(7);
        }
        return null;
    }

}
