package com.zcy.forum.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;


import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT 工具类（生成、解析、校验、刷新令牌）
 * 适配 JJWT 0.12.x + Spring Boot 3.x
 */
@Slf4j
@Component
public class JwtTokenUtil {

    // 从配置文件读取密钥
    @Value("${forum.jwt.secret}")
    private String secret;

    // 令牌过期时间（小时）
    @Value("${forum.jwt.expire-hours:72}")
    private Integer expireHours;

    // 刷新令牌过期时间（小时）
    @Value("${forum.jwt.refresh-expire-hours:168}")
    private Integer refreshExpireHours;

    // 令牌前缀
    @Value("${forum.jwt.prefix:Bearer}")
    private String tokenPrefix;

    /**
     * 生成密钥（256位）
     */
    private SecretKey getSecretKey() {
        // 密钥需至少256位（32个字符），否则会抛出异常
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ====================== 核心方法：生成令牌 ======================
    /**
     * 生成访问令牌（默认载荷）
     * @param username 用户名（唯一标识）
     * @return JWT令牌
     */
    public String generateToken(String username) {
        return generateToken(new HashMap<>(), username, expireHours);
    }

    /**
     * 生成访问令牌（自定义载荷）
     * @param claims 自定义载荷（如用户ID、角色）
     * @param username 用户名
     * @return JWT令牌
     */
    public String generateToken(Map<String, Object> claims, String username) {
        return generateToken(claims, username, expireHours);
    }

    /**
     * 生成刷新令牌（过期时间更长）
     * @param username 用户名
     * @return 刷新令牌
     */
    public String generateRefreshToken(String username) {
        return generateToken(new HashMap<>(), username, refreshExpireHours);
    }

    /**
     * 通用生成令牌方法
     * @param claims 自定义载荷
     * @param subject 主题（用户名/用户ID）
     * @param expireHours 过期小时数
     * @return JWT令牌
     */
    private String generateToken(Map<String, Object> claims, String subject, int expireHours) {
        // 计算过期时间
        long expireTime = System.currentTimeMillis() + expireHours * 3600 * 1000L;

        // 生成JWT令牌
        return Jwts.builder()
                .claims(claims) // 自定义载荷
                .subject(subject) // 主题（唯一标识）
                .issuedAt(new Date()) // 签发时间
                .expiration(new Date(expireTime)) // 过期时间
                .signWith(getSecretKey(), Jwts.SIG.HS256) // 签名算法（HS256）
                .compact();
    }

    // ====================== 核心方法：解析令牌 ======================
    /**
     * 从令牌中获取用户名
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从令牌中获取指定载荷
     * @param token JWT令牌
     * @param claimsResolver 载荷解析器
     * @return 载荷值
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析令牌，获取所有载荷（校验签名和过期）
     * @param token JWT令牌
     * @return 载荷集合
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey()) // 验证签名
                    .build()
                    .parseSignedClaims(token) // 解析令牌
                    .getPayload(); // 获取载荷
        } catch (Exception e) {
            log.error("JWT令牌解析失败：{}", e.getMessage(), e);
            throw new SecurityException("令牌解析失败", e);
        }
    }

    // ====================== 核心方法：校验令牌 ======================
    /**
     * 校验令牌是否有效
     * @param token JWT令牌
     * @param userDetails 用户信息（用于对比用户名）
     * @return true=有效，false=无效
     */


    /**
     * 校验令牌是否有效（仅校验签名和过期）
     * @param token JWT令牌
     * @return true=有效，false=无效
     */
    public boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token); // 解析过程会自动校验签名和过期
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT令牌已过期：{}", e.getMessage());
        } catch (SecurityException | MalformedJwtException e) {
            log.error("JWT令牌签名无效/格式错误：{}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("不支持的JWT令牌：{}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT令牌参数为空：{}", e.getMessage());
        }
        return false;
    }

    /**
     * 判断令牌是否过期
     * @param token JWT令牌
     * @return true=已过期，false=未过期
     */
    public boolean isTokenExpired(String token) {
        final Date expiration = getClaimFromToken(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    // ====================== 辅助方法：处理请求头 ======================
    /**
     * 从请求头中提取令牌（去除前缀）
     * @param authHeader 请求头值（格式：Bearer {token}）
     * @return 纯令牌字符串
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(tokenPrefix + " ")) {
            return null;
        }
        // 去除前缀，返回纯令牌
        return authHeader.substring(tokenPrefix.length() + 1).trim();
    }

    /**
     * 刷新令牌（令牌未过期时生成新令牌）
     * @param oldToken 旧令牌
     * @return 新令牌
     */
    public String refreshToken(String oldToken) {
        if (!validateToken(oldToken)) {
            throw new SecurityException("旧令牌无效，无法刷新");
        }
        // 从旧令牌中获取用户名，生成新令牌
        String username = getUsernameFromToken(oldToken);
        return generateToken(username);
    }

    public Long getUserIdFromToken(String token) {
        try {
            // 从载荷中获取userId字段，转换为Long类型
            Object userIdObj = getClaimFromToken(token, claims -> claims.get("userId"));
            if (userIdObj == null) {
                throw new SecurityException("JWT令牌中未包含用户ID");
            }
            // 兼容不同类型的userId（Integer/Long/String）
            if (userIdObj instanceof Integer) {
                return ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof Long) {
                return (Long) userIdObj;
            } else if (userIdObj instanceof String) {
                return Long.parseLong((String) userIdObj);
            } else {
                throw new SecurityException("用户ID格式不支持：" + userIdObj.getClass().getName());
            }
        } catch (NumberFormatException e) {
            throw new SecurityException("用户ID解析失败（非数字）", e);
        }
    }
}