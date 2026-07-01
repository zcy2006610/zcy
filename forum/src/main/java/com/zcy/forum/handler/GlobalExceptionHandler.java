package com.zcy.forum.handler;

import com.zcy.forum.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result<?> handleAllException(Exception e, HttpServletRequest request) {
        // 1. 日志记录（关键：保留请求信息+完整异常栈，便于排查）
        log.error("【全局异常】请求路径：{}，异常类型：{}，异常信息：{}",
                 request.getRequestURI(), e.getClass().getSimpleName(), e.getMessage(), e);

        // 2. 区分异常类型，返回友好提示
        String userMsg;
        int code;
        if (e instanceof org.springframework.validation.BindException || e instanceof jakarta.validation.ConstraintViolationException) {
            // 参数校验类异常
            code = 400;
            userMsg = "参数格式错误，请检查输入";
        } else if (e instanceof RuntimeException) {
            // 运行时异常
            code = 500;
            userMsg = e.getMessage();
        } else {
            // 其他异常
            code = 500;
            userMsg = "系统繁忙，请稍后重试";
        }

        // 3. 返回统一格式
        return Result.fail(userMsg, code);
    }
}

