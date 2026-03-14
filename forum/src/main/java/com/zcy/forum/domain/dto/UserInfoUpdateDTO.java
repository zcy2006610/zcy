package com.zcy.forum.domain.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserInfoUpdateDTO {
    private String username;


    private String nickname;

    private Integer gender;

    private LocalDate birthday;


    private String location;
}
