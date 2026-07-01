package com.zcy.forum.domain.dto;

import lombok.Data;
import org.apache.ibatis.annotations.Select;

@Data
public class UserPwdUpdateDTO {
    private  String oldPwd;
    private  String newPwd;
}
