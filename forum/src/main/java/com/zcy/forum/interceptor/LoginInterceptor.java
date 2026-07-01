package com.zcy.forum.interceptor;

import com.zcy.forum.constant.LoginConstant;
import com.zcy.forum.utils.JwtTokenUtil;
import com.zcy.forum.utils.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");

        // 2. 提取Token（第一步校验：请求头是否有Token）
        String token = jwtTokenUtil.extractTokenFromHeader(authHeader);
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("请求头中未包含有效的JWT令牌");
        }

        // 3. 校验Token有效性（第二步校验：签名、过期时间等）
        if (!jwtTokenUtil.validateToken(token)) {
            throw new RuntimeException("JWT令牌无效或已过期");
        }

        // 4. 安全获取用户ID（第三步：提取并转换userId）
        Long userId = jwtTokenUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new RuntimeException("JWT令牌中未包含有效的用户ID");
        }
        String redisToken = redisTemplate.opsForValue().get(LoginConstant.USER_LOGIN_KEY + userId);
        if(!StringUtils.hasText(redisToken)){
            log.info("redis中的jwt令牌已失效");
            return false;
        }
        UserContextHolder.setUserId(userId);
        return true;

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContextHolder.remove();
    }
}
