package com.example.bulletinboard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.*;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * セキュリティルールを定義するBean
     * @param http HttpSecurityオブジェクト
     * @return 設定済みのSecurityFilterChain
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // 1. CSRF保護を無効化
            .authorizeHttpRequests(auth -> auth
                // 2. GETリクエストのルール
                .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/**").permitAll()
                // 3. それ以外のリクエストのルール
                .anyRequest().authenticated()
            )
            // 4. HTTP Basic認証を有効化
            .httpBasic(Customizer.withDefaults());
        
        return http.build();
    }
    
    /**
     * パスワードをハッシュ化するためのPasswordEncoderのBean
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
   
    /**
     * 認証処理の要であるAuthenticationManagerをBeanとして登録
     * @param authConfig
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    
}