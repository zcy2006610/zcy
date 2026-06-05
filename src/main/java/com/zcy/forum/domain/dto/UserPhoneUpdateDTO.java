package com.zcy.forum.domain.dto;

import lombok.Data;

@Data
public class UserPhoneUpdateDTO {
    private String oldPhoneNumber;
    private String newPhoneNumber;
    private String code;
}
