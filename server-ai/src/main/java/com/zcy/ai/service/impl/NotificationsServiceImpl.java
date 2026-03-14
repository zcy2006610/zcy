package com.zcy.ai.service.impl;

import com.zcy.ai.domain.entity.Notifications;
import com.zcy.ai.mapper.NotificationsMapper;
import com.zcy.ai.service.INotificationsService;
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
