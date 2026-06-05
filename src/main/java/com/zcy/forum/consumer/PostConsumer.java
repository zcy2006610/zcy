package com.zcy.forum.consumer;

import cn.hutool.core.bean.BeanUtil;
import com.zcy.forum.config.RabbitMqConfig;
import com.zcy.forum.domain.dto.PostReviewDTO;
import com.zcy.forum.domain.entity.Notifications;
import com.zcy.forum.domain.entity.Posts;
import com.zcy.forum.domain.vo.ReviewResultVO;
import com.zcy.forum.mapper.primary.NotifyMapper;
import com.zcy.forum.mapper.primary.PostMapper;
import com.zcy.forum.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDateTime;

/**
 * 帖子消息消费者
 */
@Component
@Slf4j
public class PostConsumer {

    @Autowired
    private AIService aiService;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private NotifyMapper notifyMapper;

    @RabbitListener(queues = RabbitMqConfig.POST_QUEUE_V1)
    @Transactional(rollbackFor = Exception.class)
    //AI审核v1版本
    public void receiveAndAuditPostV1(Object msg) throws InterruptedException {
        try {
            PostReviewDTO reviewDTO = BeanUtil.toBean(msg, PostReviewDTO.class);
            log.info("收到帖子审核消息：帖子ID={}, 用户ID={}", reviewDTO.getId(), reviewDTO.getUserId());
            
            // AI审核帖子
            boolean reviewResult = aiService.reviewPostV1(reviewDTO);
            
            // 构建审核结果
            ReviewResultVO resultVO = new ReviewResultVO();
            resultVO.setId(reviewDTO.getId());
            resultVO.setReviewer("论坛助手");
            resultVO.setReviewTime(LocalDateTime.now());
            resultVO.setUserId(reviewDTO.getUserId());
            resultVO.setMethod("AI审核");
            
            // 根据审核结果设置状态和消息
            int auditStatus;
            String resultMessage;
            if (reviewResult) {
                auditStatus = 1; // 审核通过
                resultMessage = "审核成功";
            } else {
                auditStatus = 2; // 审核拒绝
                resultMessage = "审核失败：内容不符合规范";
            }
            resultVO.setResult(resultMessage);
            Thread.sleep(5000);
            // 更新帖子审核状态
            Posts post = postMapper.selectById(reviewDTO.getId());
            if (post != null) {
                post.setAuditStatus(auditStatus);
                post.setAuditUserId(0L);
                post.setAuditTime(new Date(System.currentTimeMillis()));
                post.setUpdatedAt(new Date(System.currentTimeMillis()));
                postMapper.updateById(post);
                log.info("帖子审核状态更新成功：帖子ID={}, 审核状态={}", reviewDTO.getId(), auditStatus);
            } else {
                log.warn("帖子不存在，跳过审核：帖子ID={}", reviewDTO.getId());
                return;
            }
            
            // 发送通知
            Notifications notification = Notifications.builder()
                    .userId(reviewDTO.getUserId())
                    .senderId(0L)
                    .type(5)
                    .relatedId(reviewDTO.getId())
                    .title(resultMessage)
                    .content(resultMessage)
                    .isRead(0)
                    .isDelete(0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            notifyMapper.insert(notification);
            log.info("审核通知发送成功：帖子ID={}, 用户ID={}", reviewDTO.getId(), reviewDTO.getUserId());
            
            log.info("帖子审核完成：帖子ID={}, 结果={}", reviewDTO.getId(), resultMessage);
        } catch (Exception e) {
            log.error("处理帖子审核消息失败", e);
            throw e; // 抛出异常，让消息重新入队
        }
    }

    @RabbitListener(queues = RabbitMqConfig.POST_QUEUE_V2)
    public void receiveAndAuditPostV2(Object msg){

    }




}
