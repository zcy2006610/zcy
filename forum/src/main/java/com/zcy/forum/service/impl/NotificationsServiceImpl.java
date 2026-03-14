package com.zcy.forum.service.impl;

import com.zcy.forum.domain.entity.Notifications;
import com.zcy.forum.mapper.NotificationsMapper;
import com.zcy.forum.service.INotificationsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 消息通知表 服务实现类
 * </p>
 *
 * @author 张城逸
 * @since 2026-03-13
 */
@Service
public class NotificationsServiceImpl extends ServiceImpl<NotificationsMapper, Notifications> implements INotificationsService {

}
