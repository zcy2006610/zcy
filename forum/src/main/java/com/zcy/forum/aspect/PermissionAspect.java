package com.zcy.forum.aspect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zcy.forum.annotation.RequirePermission;
import com.zcy.forum.constant.LoginConstant;
import com.zcy.forum.constant.ResultConstant;
import com.zcy.forum.domain.entity.Users;
import com.zcy.forum.mapper.UserMapper;
import com.zcy.forum.utils.JwtTokenUtil;
import com.zcy.forum.utils.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class PermissionAspect {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Pointcut("@annotation(com.zcy.forum.annotation.RequirePermission)")
    public void permissionPointcut() {}

    @Around("permissionPointcut()")
    public Object aroundPermissionCheck(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return buildForbiddenResponse();
        }
        HttpServletResponse response = attributes.getResponse();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
        if (!requirePermission.required()) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();
        boolean hasPermission = checkPermission(request);
        if (!hasPermission) {
            return buildForbiddenResponse(response);
        }

        return joinPoint.proceed();
    }

    private boolean checkPermission(HttpServletRequest request) {

        String header = request.getHeader("Authorization");
        String token = jwtTokenUtil.extractTokenFromHeader(header);
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("请求头中未包含有效的JWT令牌");
        }


        if (!jwtTokenUtil.validateToken(token)) {
            throw new RuntimeException("JWT令牌无效或已过期");
        }

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

        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<Users>()
                .select(Users::getRole)
                .eq(Users::getId, userId);
        Users user = userMapper.selectOne(wrapper);
        if (user == null) {
            return false;
        }

        Integer role = user.getRole();
        return role != null && role > 0;
    }

    private Object buildForbiddenResponse(HttpServletResponse response) {
        try {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(403);
            PrintWriter writer = response.getWriter();
            writer.write("{\"code\":" + ResultConstant.FORBIDDEN.getCode() + ",\"msg\":\"" + ResultConstant.FORBIDDEN.getMsg() + "\",\"data\":null}");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object buildForbiddenResponse() {
        return "{\"code\":" + ResultConstant.FORBIDDEN.getCode() + ",\"msg\":\"" + ResultConstant.FORBIDDEN.getMsg() + "\",\"data\":null}";
    }
}
