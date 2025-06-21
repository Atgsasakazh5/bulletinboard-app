package com.example.bulletinboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 1. 全てのパスを対象にする
                .allowedOrigins("https://shoptransporter.sakura.ne.jp", "http://localhost:5500") // 2. 許可するオリジン（フロントエンドのURL）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 3. 許可するHTTPメソッド
                .allowedHeaders("*") // 4. 全てのHTTPヘッダーを許可
                .allowCredentials(true); // 5. クレデンシャル（Cookieなど）を許可
    }
}