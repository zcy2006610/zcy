package com.zcy.forum.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户登录日志表
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_login_logs")
public class UserLoginLogs implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 登录方式：1-密码，2-验证码，3-第三方（微信），4-记住登录
     */
    private Integer loginType;

    /**
     * 登录IP
     */
    private String ip;

    /**
     * 设备信息（如Chrome/Windows 10）
     */
    private String device;

    /**
     * IP属地（如北京）
     */
    private String location;

    /**
     * 登录结果：0-失败，1-成功
     */
    private Integer status;

    /**
     * 失败原因
     */
    private String errorMsg;

    private LocalDateTime createdAt;


}
