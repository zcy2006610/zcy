package com.zcy.forum.consumer;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zcy.forum.config.RabbitMqConfig;
import com.zcy.forum.domain.dto.CommentLikeDTO;
import com.zcy.forum.domain.entity.Comments;
import com.zcy.forum.domain.entity.Likes;
import com.zcy.forum.mapper.primary.CommentMapper;
import com.zcy.forum.mapper.primary.LikeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 评论消息消费者
 */
@Component
@Slf4j
public class CommentConsumer {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private LikeMapper likeMapper;

    @RabbitListener(queues = RabbitMqConfig.COMMENT_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void receiveCommentMsg(Object msg) {
        try {
            CommentLikeDTO likeDTO = BeanUtil.toBean(msg, CommentLikeDTO.class);
            log.info("收到评论点赞消息：评论ID={}, 用户ID={}, 操作={}", 
                    likeDTO.getCommentId(), likeDTO.getUserId(), likeDTO.getIsLike() ? "点赞" : "取消点赞");

            // 更新评论点赞数
            Comments comment = commentMapper.selectById(likeDTO.getCommentId());
            if (comment != null) {
                comment.setLikeCount(likeDTO.getLikeCount());
                comment.setUpdatedAt(LocalDateTime.now());
                commentMapper.updateById(comment);
                log.debug("更新评论点赞数成功，评论ID: {}, 点赞数: {}", likeDTO.getCommentId(), likeDTO.getLikeCount());
            }

            // 处理点赞记录
            if (Boolean.TRUE.equals(likeDTO.getIsLike())) {
                // 检查是否已存在点赞记录
                LambdaQueryWrapper<Likes> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Likes::getUserId, likeDTO.getUserId())
                        .eq(Likes::getTargetType, 2) // 2表示评论
                        .eq(Likes::getTargetId, likeDTO.getCommentId());

                Likes existingLike = likeMapper.selectOne(wrapper);
                if (existingLike == null) {
                    // 新增点赞记录
                    Likes like = new Likes();
                    like.setUserId(likeDTO.getUserId());
                    like.setTargetType(2);
                    like.setTargetId(likeDTO.getCommentId());
                    like.setCreatedAt(LocalDateTime.now());
                    likeMapper.insert(like);
                    log.debug("新增点赞记录成功，评论ID: {}, 用户ID: {}", likeDTO.getCommentId(), likeDTO.getUserId());
                } else if (existingLike.getCanceledAt() != null) {
                    // 恢复已取消的点赞
                    existingLike.setCanceledAt(null);
                    existingLike.setCreatedAt(LocalDateTime.now());
                    likeMapper.updateById(existingLike);
                    log.debug("恢复点赞记录成功，评论ID: {}, 用户ID: {}", likeDTO.getCommentId(), likeDTO.getUserId());
                }
            } else {
                // 取消点赞 - 更新取消时间
                LambdaQueryWrapper<Likes> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Likes::getUserId, likeDTO.getUserId())
                        .eq(Likes::getTargetType, 2)
                        .eq(Likes::getTargetId, likeDTO.getCommentId())
                        .isNull(Likes::getCanceledAt);

                Likes existingLike = likeMapper.selectOne(wrapper);
                if (existingLike != null) {
                    existingLike.setCanceledAt(LocalDateTime.now());
                    likeMapper.updateById(existingLike);
                    log.debug("取消点赞记录成功，评论ID: {}, 用户ID: {}", likeDTO.getCommentId(), likeDTO.getUserId());
                }
            }

            log.info("评论点赞消息处理完成：评论ID={}", likeDTO.getCommentId());
        } catch (Exception e) {
            log.error("处理评论点赞消息失败", e);
            throw e; // 抛出异常，让消息重新入队
        }
    }
}
