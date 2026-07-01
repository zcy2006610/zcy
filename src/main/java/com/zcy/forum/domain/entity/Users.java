package com.zcy.forum.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户核心表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("users")
public class Users implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 登录用户名（唯一）
     */
    private String username;

    /**
     * 密码（bcrypt加密）
     */
    private String password;

    /**
     * 邮箱（唯一，验证后生效）
     */
    private String email;

    /**
     * 邮箱是否验证：0-否，1-是
     */
    private Integer emailVerified;

    /**
     * 手机号（唯一，脱敏存储）
     */
    private String mobile;

    /**
     * 手机号是否验证：0-否，1-是
     */
    private Integer mobileVerified;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 昵称（可重复）
     */
    private String nickname;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 所在地
     */
    private String location;

    /**
     * 个人网站
     */
    private String website;

    /**
     * 角色：0-普通，1-版主，2-管理员，3-超级管理员
     */
    private Integer role;

    /**
     * 用户等级（1-100）
     */
    private Integer level;

    /**
     * 积分
     */
    private Integer points;

    /**
     * 状态：0-禁用，1-正常，2-封禁7天，3-永久封禁
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginAt;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 软删除时间
     */
    private LocalDateTime deletedAt;


}
