package com.zcy.forum.domain.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRegistryDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度2-20位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 16, message = "密码长度8-16位")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,16}$", message = "密码需包含字母+数字")
    private String password;

    // 邮箱：可选，但填了就必须符合格式
    @Email(message = "邮箱格式错误")
    private String email;


    // 手机号：可选，但填了就必须符合格式
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    private String mobile;


    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 30, message = "昵称长度2-30位")
    private String nickname;


    private Integer gender;

    @NotNull
    private String code;
}
