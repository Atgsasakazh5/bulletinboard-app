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

    // application.propertiesから秘密鍵を読み込む
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    // application.propertiesから有効期限を読み込む
    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    // 認証情報からJWTを生成するメソッド
    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userPrincipal.getUsername()) // ユーザー名を主題に設定
                .issuedAt(now) // 発行日時
                .expiration(expiryDate) // 有効期限
                .signWith(key()) // 秘密鍵で署名
                .compact(); // JWT文字列を生成
    }

    // Base64エンコードされた秘密鍵をデコードしてSecretKeyオブジェクトを生成
    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // ここにJWTの検証や、トークンからユーザー名を取得するメソッドを後で追加していきます
}