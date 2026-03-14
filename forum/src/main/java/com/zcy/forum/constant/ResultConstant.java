package com.zcy.forum.constant;

import lombok.Getter;

@Getter
public enum  ResultConstant {
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或令牌过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    CODE_REPEAT(405,"不能重复发送验证码"),
    // 业务状态码（论坛专属，可扩展）
    USER_NAME_EXIST(1001, "用户名已存在"),
    USER_LOGIN_ERROR(1002, "用户名或密码错误"),
    POST_NOT_EXIST(2001, "帖子不存在"),
    COMMENT_NOT_EXIST(3001, "评论不存在"),
    NO_USER(3002,"账号错误"),

    PASSWORD_ERROR(3003,"密码错误"),
    NO_PHONE(3004,"手机号为空"),
    CODE_ERROR(3005,"验证码错误"),
    CODE_EMPTY(3006,"验证码为空"),
    CODE_EXPIRED(3007,"验证码已过期"),

    SENSITIVE_WORD(4001, "内容包含敏感词"),
    UPLOAD_AVATAR_FAIL(5001,"头像上传失败");


    private final Integer code;
    private final String msg;

    ResultConstant(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
