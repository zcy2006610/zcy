package com.zcy.forum.controller;

import com.zcy.forum.annotation.CacheEvict;
import com.zcy.forum.annotation.RateLimit;
import com.zcy.forum.annotation.RequireLogin;
import com.zcy.forum.common.Result;
import com.zcy.forum.constant.ResultConstant;
import com.zcy.forum.domain.dto.*;
import com.zcy.forum.domain.vo.UserInfoVO;
import com.zcy.forum.domain.vo.UserRegistryVO;
import com.zcy.forum.service.UserService;
import com.zcy.forum.utils.MinioUtil;
import com.zcy.forum.utils.UserContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequestMapping("/user/auth")
@Tag(name = "用户模块", description = "用户登录、注册、信息查询")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private MinioUtil minioUtil;

    @PostMapping("/registry")
    @Operation(summary = "用户注册接口")
    @RateLimit(period = 300,count = 1)
    public Result<UserRegistryVO> registry(@RequestBody @Validated UserRegistryDTO registryDTO){
        return Result.ok(userService.registry(registryDTO));
    }

    @GetMapping("/code/{phoneNumber}")
    @Operation(summary = "验证码接口")
    @RateLimit(count = 1)
    public Result<String> getCode(@PathVariable String phoneNumber){
        //TODO
        //后续需要集成第三方服务将验证码发送到用户手机
        return Result.ok(userService.generateCode(phoneNumber));
    }

    @PostMapping("/login/v1")
    @Operation(summary="账号密码登录接口")
    @RateLimit(count = 3)
    public Result<String> loginv1(@RequestBody UserLoginDTO userLoginDTO){
        return Result.ok(userService.loginWithPwd(userLoginDTO));
    }

    @PostMapping("/login/v2")
    @Operation(summary = "验证码登录")
    public Result<String> loginv2(@RequestBody UserLoginDTO userLoginDTO){
        return Result.ok(userService.loginWithCode(userLoginDTO));
    }

    @GetMapping("/info")
    @RateLimit
    @RequireLogin
    @Operation(summary = "获取登录用户信息")
    public Result<UserInfoVO> getUserInfo(){
        Long userId = UserContextHolder.getUserId();
        if(userId==null){
            return Result.fail("未登录",9999);
        }
        return Result.ok(userService.getUserInfo(userId));
    }

    @PostMapping("/logout")
    @Operation(summary = "登出")
    @RequireLogin
    public Result<String> logout(){
        userService.logout();
        return Result.ok();
    }

    @PostMapping("/upload/avatar")
    @Operation(summary = "上传头像")
    @RequireLogin(required = true)
    @RateLimit(count = 3)
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 1. 调用工具类上传文件，获取 URL
            String avatarUrl = minioUtil.uploadAvatar(file);
            Long userId = UserContextHolder.getUserId();
            userService.update()
                    .set("avatar",avatarUrl)
                    .eq("id",userId)
                    .update();
            return Result.ok(avatarUrl);
        } catch (Exception e) {
            return Result.fail(ResultConstant.UPLOAD_AVATAR_FAIL.getMsg(), ResultConstant.UPLOAD_AVATAR_FAIL.getCode());
        }
    }

    @PostMapping
    @Operation(summary = "更换头像")
    @RequireLogin(required = true)
    @RateLimit(count = 4)
    public Result<String> updateAvatar(@RequestParam("file") MultipartFile file,@RequestParam String oldUrl){
        try {
            Long userId = UserContextHolder.getUserId();
            
            if (oldUrl != null && !oldUrl.isEmpty()) {
                minioUtil.deleteOldAvatar(oldUrl);
            }
            
            String newAvatarUrl = minioUtil.uploadAvatar(file);
            
            userService.update()
                    .set("avatar", newAvatarUrl)
                    .eq("id", userId)
                    .update();
            
            return Result.ok(newAvatarUrl);
        } catch (Exception e) {
            return Result.fail(ResultConstant.UPLOAD_AVATAR_FAIL.getMsg(), ResultConstant.UPLOAD_AVATAR_FAIL.getCode());
        }
    }

    @PostMapping("/update/info")
    @Operation(summary = "更改个人信息")
    @RequireLogin(required = true)
    @RateLimit(count = 1)
    public Result<String> updateInfo(@RequestBody UserInfoUpdateDTO updateDTO){
        Long userId = UserContextHolder.getUserId();
        if(userId==null){
            return Result.fail("未登录",9999);
        }

        userService.updateInfo(updateDTO,userId);
        return Result.ok();
    }



    @PostMapping("/update/phone")
    @Operation(summary = "换绑手机号")
    @RequireLogin(required = true)
    //TODO
    //后续需要添加验证码接口发送验证码换绑
    public Result<String> updatePhone(@RequestBody UserPhoneUpdateDTO UpdateDTO){
        userService.updatePhone(UpdateDTO);
        return Result.ok();
    }

    @PostMapping("/update/pwd")
    @Operation(summary = "修改密码")
    @RequireLogin(required = true)
    @RateLimit(count = 1)
    public Result<String> updatePwd(@RequestBody UserPwdUpdateDTO updateDTO){
        userService.updatePwd(updateDTO);
        return Result.ok();
    }









}
