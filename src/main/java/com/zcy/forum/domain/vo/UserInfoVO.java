package com.zcy.forum.domain.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserInfoVO {
    private Long id;

    private String username;

    private String email;

    private String avatar;

    private String nickname;

    private String bio;

    private Integer gender;

    private LocalDate birthday;

    private String location;

    private Integer role;

    private Integer level;

    private Integer points;

    private LocalDateTime lastLoginAt;

    private String lastLoginIp;
}
