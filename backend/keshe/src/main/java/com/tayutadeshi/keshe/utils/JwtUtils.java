package com.tayutadeshi.keshe.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.Map;

public class JwtUtils {

    // 随便写个字符串就行，0.9.1 不嫌短
    private static final String SECRET = "KesheSecretKey123456";
    // 12小时
    private static final long EXPIRE = 1000 * 60 * 60 * 12;

    /**
     * 生成 Token
     */
    public static String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))
                // 旧版写法：直接传算法和字符串
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    /**
     * 解析 Token
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                // 旧版写法：直接 setSigningKey
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }
}