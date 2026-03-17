package com.zcy.forum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.houbb.heaven.util.util.CollectionUtil;
import com.zcy.forum.domain.entity.ConversationMessage;
import com.zcy.forum.domain.entity.UserConversation;
import com.zcy.forum.domain.entity.Users;
import com.zcy.forum.domain.vo.UserConversationVO;
import com.zcy.forum.mapper.UserConversationMapper;
import com.zcy.forum.mapper.primary.ConversationMessageMapper;
import com.zcy.forum.mapper.primary.UserConversationMapper;
import com.zcy.forum.mapper.primary.UserMapper;
import com.zcy.forum.service.IUserConversationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zcy.forum.utils.UserContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户会话表 服务实现类
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-17
 */
@Service
public class UserConversationServiceImpl extends ServiceImpl<UserConversationMapper, UserConversation> implements IUserConversationService {

    @Autowired
    private UserConversationMapper convMapper;

    @Autowired
    private UserMapper userMapper;
    @Override
    public List<UserConversationVO> getConversationList(Long userId) {
        if(!Objects.equals(userId, UserContextHolder.getUserId())||userId==null){
            throw new RuntimeException("用户id为空或者错误");
        }
        LambdaQueryWrapper<UserConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserConversation::getUserId,userId);

        List<UserConversation> userConversations = convMapper.selectList(wrapper);
        if(CollectionUtil.isEmpty(userConversations)){
            return Collections.emptyList();
        }
        List<UserConversationVO> vos = userConversations.stream().map(c -> {
            UserConversationVO vo = new UserConversationVO();
            BeanUtil.copyProperties(c, vo);
            return vo;
        }).toList();
        List<Long> collect = userConversations.stream()
                .map(UserConversation::getTargetId)
                .filter(Objects::nonNull)
                .toList();
        List<Users> usersList = userMapper.selectBatchIds(collect);
        if(CollectionUtil.isNotEmpty(usersList)){
            Map<Long, String> userMap = usersList.stream()
                    .collect(Collectors.toMap(Users::getId, Users::getAvatar));
            vos.forEach(vo->{
                vo.setTargetUrl(userMap.get(vo.getTargetId()));
            });
        }
        return vos;

    }

    @Override
    public void clearUnread(Long userId, String conversationId) {
        if(userId==null||conversationId==null){
            throw new RuntimeException("未指定会话id");
        }
        boolean update = update().set("unread_count", 0)
                .eq("user_id", userId)
                .eq("conversation_id", conversationId)
                .update();
        if(!update){
            throw new RuntimeException("更改失败,请稍后重试");
        }


    }

    @Override
    public void deleteConversation(Long userId, String conversationId) {
        if(userId==null||conversationId==null){
            throw new RuntimeException("未指定会话id");
        }
        boolean update = update().set("is_deleted", 1)
                .eq("user_id", userId)
                .eq("conversation_id", conversationId)
                .update();
        if(!update){
            throw new RuntimeException("删除失败,请稍后重试");
        }
    }




}
