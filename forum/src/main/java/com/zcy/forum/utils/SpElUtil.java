package com.zcy.forum.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * SpEL通用解析工具
 */
@Component
public class SpElUtil {
    private final ExpressionParser parser = new SpelExpressionParser();

    public String parseKey(String spEl, ProceedingJoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        EvaluationContext context = new StandardEvaluationContext();
        
        String[] paramNames = signature.getParameterNames();
        Object[] args = point.getArgs();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        Object value = parser.parseExpression(spEl).getValue(context);
        return value == null ? "" : value.toString();
    }
}