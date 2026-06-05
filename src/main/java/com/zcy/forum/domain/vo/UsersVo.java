package com.zcy.forum.domain.vo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class UsersVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String email;

    private Integer emailVerified;

    private String mobile;

    private Integer mobileVerified;

    private String avatar;

    private String nickname;

    private String bio;

    private Integer gender;

    private LocalDate birthday;

    private String location;

    private String website;

    private Integer role;

    private Integer level;

    private Integer points;

    private Integer status;

    private LocalDateTime lastLoginAt;

    private String lastLoginIp;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
