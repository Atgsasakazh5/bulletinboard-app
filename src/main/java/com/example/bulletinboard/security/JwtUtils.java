package com.example.bulletinboard.security;

import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    // トークンを発行する
    public String generateToken(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        String token = Jwts.builder().subject(userPrincipal.getUsername()).issuedAt(now).expiration(expiryDate)
                .signWith(key()) // この中でkey()メソッドが呼ばれる
                .compact();

        return token;
    }

    private SecretKey key() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);
        return secretKey;
    }

    // トークンからユーザー名を抽出するメソッド
    public String getUsernameFromToken(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload().getSubject();
    }

    // トークンの有効性を検証するメソッド
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key()).build().parse(token);
            return true;
        } catch (Exception e) {
            // 例外が発生した場合（無効なトークンなど）はfalseを返す
            System.err.println("Invalid JWT token: " + e.getMessage());
        }
        return false;
    }
}