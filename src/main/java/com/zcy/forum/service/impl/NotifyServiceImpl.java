package com.zcy.forum.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zcy.forum.domain.entity.Notifications;
import com.zcy.forum.mapper.primary.NotifyMapper;
import com.zcy.forum.service.NotifyService;
import org.springframework.stereotype.Service;

@Service
public class NotifyServiceImpl extends ServiceImpl<NotifyMapper, Notifications> implements NotifyService {
}
