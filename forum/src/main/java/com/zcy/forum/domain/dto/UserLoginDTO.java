package com.zcy.forum.domain.dto;

import lombok.Data;

@Data
public class UserLoginDTO {
    private String username;
    private String password;
    private String phoneNumber;
    private String code;

}
