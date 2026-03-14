package com.zcy.forum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zcy.forum.annotation.RateLimit;
import com.zcy.forum.config.PasswordConfig;
import com.zcy.forum.constant.LoginConstant;
import com.zcy.forum.constant.ResultConstant;
import com.zcy.forum.domain.dto.UserInfoUpdateDTO;
import com.zcy.forum.domain.dto.UserLoginDTO;
import com.zcy.forum.domain.dto.UserPhoneUpdateDTO;
import com.zcy.forum.domain.dto.UserPwdUpdateDTO;
import com.zcy.forum.domain.dto.UserRegistryDTO;
import com.zcy.forum.domain.entity.Users;
import com.zcy.forum.domain.vo.UserInfoVO;
import com.zcy.forum.domain.vo.UserRegistryVO;
import com.zcy.forum.mapper.UserMapper;
import com.zcy.forum.service.UserService;
import com.zcy.forum.utils.JwtTokenUtil;
import com.zcy.forum.utils.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper,Users> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Override
    public UserRegistryVO registry(UserRegistryDTO registryDTO) {
        String username = registryDTO.getUsername();
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<Users>().eq(Users::getUsername, username);
        Users user = userMapper.selectOne(wrapper);
        if(user!=null){
            throw new RuntimeException(ResultConstant.USER_NAME_EXIST.getMsg());
        }

        user = new Users();
        BeanUtil.copyProperties(registryDTO,user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(0);
        user.setLevel(1);
        user.setStatus(1);
        user.setPoints(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
        UserRegistryVO registryVO = new UserRegistryVO();
        registryVO.setUserid(user.getId());
        registryVO.setUsername(user.getUsername());
        return registryVO;
    }

    @Override
    public String generateCode(String phoneNumber) {
        Long expirePeriod = redisTemplate.getExpire(LoginConstant.CODE_KEY + phoneNumber, TimeUnit.SECONDS);
        if(expirePeriod!=null){
            if(LoginConstant.CODE_TIME_OUT -expirePeriod<60){
                throw new RuntimeException(ResultConstant.CODE_REPEAT.getMsg());
            }
        }
        String code = RandomUtil.randomNumbers(6);
        redisTemplate.opsForValue().set(LoginConstant.CODE_KEY+phoneNumber,code,
                LoginConstant.CODE_TIME_OUT, TimeUnit.SECONDS);
        log.info("生成验证码成功,手机号"+phoneNumber+",验证码+"+code);
        return code;
    }

    @Override
    public String loginWithPwd(UserLoginDTO userLoginDTO) {
        // 1. 前置空值校验（避免NPE，提前拦截无效请求）
        if (!StringUtils.hasText(userLoginDTO.getUsername())) {
            throw new RuntimeException(ResultConstant.PARAM_ERROR.getMsg());
        }
        if (!StringUtils.hasText(userLoginDTO.getPassword())) {
            throw new RuntimeException(ResultConstant.PARAM_ERROR.getMsg());
        }
        String username = userLoginDTO.getUsername();
        Users user = userMapper.selectOne(new QueryWrapper<Users>().eq("username", username));
        if(user==null){
            throw new RuntimeException(ResultConstant.NO_USER.getMsg());
        }
        boolean matches = passwordEncoder.matches(userLoginDTO.getPassword(), user.getPassword());
        if(!matches){
            throw new RuntimeException(ResultConstant.PASSWORD_ERROR.getMsg());
        }
        return getTokenAndSet2Redis(user);
    }

    @Override
    public String loginWithCode(UserLoginDTO userLoginDTO) {
        if(!StringUtils.hasText(userLoginDTO.getPhoneNumber())){
            throw new RuntimeException(ResultConstant.NO_PHONE.getMsg());
        }
        if(!StringUtils.hasText(userLoginDTO.getCode())){
            throw new RuntimeException(ResultConstant.CODE_EMPTY.getMsg());
        }
        String code = userLoginDTO.getCode();
        String redisCode = redisTemplate.opsForValue().get(LoginConstant.CODE_KEY + userLoginDTO.getPhoneNumber());
        // 3.1 验证码不存在（过期/未发送）
        if (redisCode == null) {
            throw new RuntimeException(ResultConstant.CODE_EXPIRED.getMsg());
        }
        if(!Objects.equals(redisCode,code)){
            throw new RuntimeException(ResultConstant.CODE_ERROR.getMsg());
        }
        Users user = userMapper.selectOne(new QueryWrapper<Users>().eq("mobile",userLoginDTO.getPhoneNumber()));
        if(user==null){
            throw new RuntimeException(ResultConstant.NO_USER.getMsg());
        }

        return getTokenAndSet2Redis(user);
    }

    @Override
    public UserInfoVO getUserInfo() {
        Long userId = UserContextHolder.getUserId();
        if(userId==null){
            throw new RuntimeException(ResultConstant.UNAUTHORIZED.getMsg());
        }
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getId,userId);
        Users users = userMapper.selectOne(wrapper);
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtil.copyProperties(users,userInfoVO);
        return userInfoVO;
    }

    @Override
    public void logout() {
        redisTemplate.delete(LoginConstant.USER_LOGIN_KEY+UserContextHolder.getUserId());
    }

    @Override
    public void updateInfo(UserInfoUpdateDTO updateDTO) {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new RuntimeException(ResultConstant.UNAUTHORIZED.getMsg());
        }

        // 检查用户名是否已被其他用户使用
        if (StringUtils.hasText(updateDTO.getUsername())) {
            LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<Users>()
                    .eq(Users::getUsername, updateDTO.getUsername())
                    .ne(Users::getId, userId);
            Users existUser = userMapper.selectOne(wrapper);
            if (existUser != null) {
                throw new RuntimeException(ResultConstant.USER_NAME_EXIST.getMsg());
            }
        }

        Users user = new Users();
        user.setId(userId);
        BeanUtil.copyProperties(updateDTO, user);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void updatePhone(UserPhoneUpdateDTO updateDTO) {
        String code = updateDTO.getCode();
        String redisCode = redisTemplate.opsForValue().get(LoginConstant.CODE_KEY + updateDTO.getOldPhoneNumber());
        if(!code.equals(redisCode)){
            throw new RuntimeException(ResultConstant.CODE_ERROR.getMsg());
        }
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new RuntimeException(ResultConstant.UNAUTHORIZED.getMsg());
        }

        // 验证旧手机号
        Users user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException(ResultConstant.NO_USER.getMsg());
        }
        if (!Objects.equals(user.getMobile(), updateDTO.getOldPhoneNumber())) {
            throw new RuntimeException("原手机号错误");
        }

        // 检查新手机号是否已被使用
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<Users>()
                .eq(Users::getMobile, updateDTO.getNewPhoneNumber())
                .ne(Users::getId, userId);
        Users existUser = userMapper.selectOne(wrapper);
        if (existUser != null) {
            throw new RuntimeException("新手机号已被绑定");
        }

        // 更新手机号
        user.setMobile(updateDTO.getNewPhoneNumber());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public void updatePwd(UserPwdUpdateDTO updateDTO) {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new RuntimeException(ResultConstant.UNAUTHORIZED.getMsg());
        }

        // 获取用户信息
        Users user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException(ResultConstant.NO_USER.getMsg());
        }

        // 验证旧密码
        boolean matches = passwordEncoder.matches(updateDTO.getOldPwd(), user.getPassword());
        if (!matches) {
            throw new RuntimeException("原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(updateDTO.getNewPwd()));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 修改密码后登出，需要重新登录
        redisTemplate.delete(LoginConstant.USER_LOGIN_KEY + userId);
    }

    private String getTokenAndSet2Redis(Users user){
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        claims.put("nickname", user.getNickname());
        String token = jwtTokenUtil.generateToken(claims, user.getUsername());
        redisTemplate.opsForValue().set(LoginConstant.USER_LOGIN_KEY+user.getId(),token,72L,TimeUnit.HOURS);
        return token;
    }



}
