package com.zcy.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zcy.forum.domain.dto.UserInfoUpdateDTO;
import com.zcy.forum.domain.dto.UserLoginDTO;
import com.zcy.forum.domain.dto.UserPhoneUpdateDTO;
import com.zcy.forum.domain.dto.UserPwdUpdateDTO;
import com.zcy.forum.domain.dto.UserRegistryDTO;
import com.zcy.forum.domain.entity.Users;
import com.zcy.forum.domain.vo.UserInfoVO;
import com.zcy.forum.domain.vo.UserRegistryVO;

public interface UserService extends IService<Users> {
    UserRegistryVO registry(UserRegistryDTO registryDTO);

    String generateCode(String phoneNumber);

    String loginWithPwd(UserLoginDTO userLoginDTO);

    String loginWithCode(UserLoginDTO userLoginDTO);

    UserInfoVO getUserInfo(Long userId);

    void logout();

    void updateInfo(UserInfoUpdateDTO updateDTO,Long userId);

    void updatePhone(UserPhoneUpdateDTO updateDTO);

    void updatePwd(UserPwdUpdateDTO updateDTO);
}
