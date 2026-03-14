package com.zcy.forum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zcy.forum.common.PageResult;
import com.zcy.forum.domain.dto.CommentLikeDTO;
import com.zcy.forum.domain.dto.CommentPublishDTO;
import com.zcy.forum.domain.entity.Comments;
import com.zcy.forum.domain.entity.Likes;
import com.zcy.forum.domain.entity.Posts;
import com.zcy.forum.domain.entity.Users;
import com.zcy.forum.domain.vo.CommentsVo;
import com.zcy.forum.mapper.CommentMapper;
import com.zcy.forum.mapper.LikeMapper;
import com.zcy.forum.mapper.PostMapper;
import com.zcy.forum.mapper.UserMapper;
import com.zcy.forum.service.CommentService;
import com.zcy.forum.utils.RabbitMqUtil;
import com.zcy.forum.utils.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comments> implements CommentService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RabbitMqUtil rabbitMqUtil;

    // Redis key前缀
    private static final String COMMENT_FIRST_LEVEL_KEY = "comment:first_level:";
    private static final String COMMENT_CHILDREN_KEY = "comment:children:";
    private static final String COMMENT_MY_KEY = "comment:my:";
    private static final String COMMENT_LIKE_COUNT_KEY = "comment:like_count:";
    private static final String COMMENT_LIKE_USER_KEY = "comment:like_user:";
    private static final long CACHE_EXPIRE_HOURS = 2;

    @Override
    @Transactional
    public Long publishComment(CommentPublishDTO commentDTO) {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Posts post = postMapper.selectById(commentDTO.getPostId());
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        Comments comment = new Comments();
        BeanUtil.copyProperties(commentDTO, comment);
        comment.setUserId(userId);
        comment.setParentId(commentDTO.getParentId() != null ? commentDTO.getParentId() : 0L);
        comment.setLikeCount(0);
        comment.setIsAuthorReply(0);
        comment.setIsShow(1);
        comment.setAuditStatus(1);
        comment.setStatus(1);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        if (comment.getParentId() != null && comment.getParentId() > 0) {
            Comments parentComment = baseMapper.selectById(comment.getParentId());
            if (parentComment != null) {
                comment.setReplyUserId(parentComment.getUserId());
            }
        }

        baseMapper.insert(comment);
        
        // 清除相关缓存
        clearCommentCache(commentDTO.getPostId(), commentDTO.getParentId(), userId);
        
        log.info("用户 {} 发布评论成功，评论ID: {}", userId, comment.getId());
        return comment.getId();
    }

    @Override
    public PageResult<CommentsVo> getFirstLevelComments(Long postId, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize <= 0 || pageSize >= 20) {
            pageSize = 10;
        }

        // 构建缓存key
        String cacheKey = COMMENT_FIRST_LEVEL_KEY + postId + ":" + pageNum + ":" + pageSize;
        
        // 尝试从缓存获取
        String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            log.debug("从缓存获取一级评论，postId: {}, pageNum: {}, pageSize: {}", postId, pageNum, pageSize);
            return JSONUtil.toBean(cachedData, PageResult.class);
        }

        LambdaQueryWrapper<Comments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comments::getPostId, postId)
                .eq(Comments::getParentId, 0L)
                .eq(Comments::getStatus, 1L)
                .eq(Comments::getIsShow, 1)
                .orderByDesc(Comments::getCreatedAt);

        Page<Comments> page = new Page<>(pageNum, pageSize);
        baseMapper.selectPage(page, wrapper);

        List<CommentsVo> records = page.getRecords().stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());

        PageResult<CommentsVo> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setRecords(records);

        // 存入缓存
        stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(result), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        log.debug("缓存一级评论，postId: {}, pageNum: {}, pageSize: {}", postId, pageNum, pageSize);

        return result;
    }

    @Override
    public PageResult<CommentsVo> getChildComments(Long parentId, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize <= 0 || pageSize >= 20) {
            pageSize = 10;
        }

        // 构建缓存key
        String cacheKey = COMMENT_CHILDREN_KEY + parentId + ":" + pageNum + ":" + pageSize;
        
        // 尝试从缓存获取
        String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            log.debug("从缓存获取子评论，parentId: {}, pageNum: {}, pageSize: {}", parentId, pageNum, pageSize);
            return JSONUtil.toBean(cachedData, PageResult.class);
        }

        LambdaQueryWrapper<Comments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comments::getParentId, parentId)
                .eq(Comments::getStatus, 1L)
                .eq(Comments::getIsShow, 1)
                .orderByAsc(Comments::getCreatedAt);

        Page<Comments> page = new Page<>(pageNum, pageSize);
        baseMapper.selectPage(page, wrapper);

        List<CommentsVo> records = page.getRecords().stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());

        PageResult<CommentsVo> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setRecords(records);

        // 存入缓存
        stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(result), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        log.debug("缓存子评论，parentId: {}, pageNum: {}, pageSize: {}", parentId, pageNum, pageSize);

        return result;
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Comments comment = baseMapper.selectById(id);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除他人评论");
        }

        comment.setStatus(0);
        comment.setUpdatedAt(LocalDateTime.now());
        baseMapper.updateById(comment);
        
        // 清除相关缓存
        clearCommentCache(comment.getPostId(), comment.getParentId(), userId);
        
        log.info("用户 {} 删除评论成功，评论ID: {}", userId, id);
    }

    @Override
    public void likeComment(Long commentId) {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Comments comment = baseMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        String likeUserSetKey = COMMENT_LIKE_USER_KEY + commentId;
        String likeCountKey = COMMENT_LIKE_COUNT_KEY + commentId;
        
        // 检查用户是否已经点赞（使用Redis Set）
        Boolean hasLiked = stringRedisTemplate.opsForSet().isMember(likeUserSetKey, userId.toString());
        
        // 获取当前点赞数（如果不存在则从数据库获取）
        String likeCountStr = stringRedisTemplate.opsForValue().get(likeCountKey);
        int likeCount = likeCountStr != null ? Integer.parseInt(likeCountStr) : comment.getLikeCount();
        
        int newLikeCount;
        boolean isLike;
        
        if (Boolean.TRUE.equals(hasLiked)) {
            // 取消点赞
            stringRedisTemplate.opsForSet().remove(likeUserSetKey, userId.toString());
            newLikeCount = Math.max(0, likeCount - 1);
            stringRedisTemplate.opsForValue().set(likeCountKey, String.valueOf(newLikeCount));
            isLike = false;
            log.info("用户 {} 取消点赞评论，评论ID: {}", userId, commentId);
        } else {
            // 点赞
            stringRedisTemplate.opsForSet().add(likeUserSetKey, userId.toString());
            newLikeCount = likeCount + 1;
            stringRedisTemplate.opsForValue().set(likeCountKey, String.valueOf(newLikeCount));
            isLike = true;
            log.info("用户 {} 点赞评论成功，评论ID: {}", userId, commentId);
        }
        
        // 设置过期时间
        stringRedisTemplate.expire(likeUserSetKey, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        stringRedisTemplate.expire(likeCountKey, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        
        // 发送RabbitMQ消息，异步处理数据库更新
        CommentLikeDTO likeDTO = new CommentLikeDTO();
        likeDTO.setCommentId(commentId);
        likeDTO.setUserId(userId);
        likeDTO.setIsLike(isLike);
        likeDTO.setLikeCount(newLikeCount);
        likeDTO.setTimestamp(System.currentTimeMillis());
        String jsonStr = JSONUtil.toJsonStr(likeDTO);
        rabbitMqUtil.sendCommentMsg(jsonStr);
        log.debug("发送评论点赞消息到队列，评论ID: {}, 用户ID: {}, 操作: {}", commentId, userId, isLike ? "点赞" : "取消点赞");
    }

    @Override
    public PageResult<CommentsVo> getMyComments(Integer pageNum, Integer pageSize) {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        if (pageNum == null || pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize <= 0 || pageSize >= 20) {
            pageSize = 10;
        }

        // 构建缓存key
        String cacheKey = COMMENT_MY_KEY + userId + ":" + pageNum + ":" + pageSize;
        
        // 尝试从缓存获取
        String cachedData = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            log.debug("从缓存获取我的评论，userId: {}, pageNum: {}, pageSize: {}", userId, pageNum, pageSize);
            return JSONUtil.toBean(cachedData, PageResult.class);
        }

        LambdaQueryWrapper<Comments> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comments::getUserId, userId)
                .eq(Comments::getStatus, 1L)
                .orderByDesc(Comments::getCreatedAt);

        Page<Comments> page = new Page<>(pageNum, pageSize);
        baseMapper.selectPage(page, wrapper);

        List<CommentsVo> records = page.getRecords().stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());

        PageResult<CommentsVo> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setRecords(records);

        // 存入缓存
        stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(result), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        log.debug("缓存我的评论，userId: {}, pageNum: {}, pageSize: {}", userId, pageNum, pageSize);

        return result;
    }

    /**
     * 清除评论相关缓存
     */
    private void clearCommentCache(Long postId, Long parentId, Long userId) {
        try {
            // 清除一级评论缓存
            if (postId != null) {
                Set<String> firstLevelKeys = stringRedisTemplate.keys(COMMENT_FIRST_LEVEL_KEY + postId + ":*");
                if (firstLevelKeys != null && !firstLevelKeys.isEmpty()) {
                    stringRedisTemplate.delete(firstLevelKeys);
                    log.debug("清除一级评论缓存，postId: {}", postId);
                }
            }
            
            // 清除子评论缓存
            if (parentId != null && parentId > 0) {
                Set<String> childrenKeys = stringRedisTemplate.keys(COMMENT_CHILDREN_KEY + parentId + ":*");
                if (childrenKeys != null && !childrenKeys.isEmpty()) {
                    stringRedisTemplate.delete(childrenKeys);
                    log.debug("清除子评论缓存，parentId: {}", parentId);
                }
            }
            
            // 清除我的评论缓存
            if (userId != null) {
                Set<String> myKeys = stringRedisTemplate.keys(COMMENT_MY_KEY + userId + ":*");
                if (myKeys != null && !myKeys.isEmpty()) {
                    stringRedisTemplate.delete(myKeys);
                    log.debug("清除我的评论缓存，userId: {}", userId);
                }
            }
        } catch (Exception e) {
            log.error("清除评论缓存失败", e);
        }
    }

    private CommentsVo convertToVo(Comments comment) {
        CommentsVo vo = new CommentsVo();
        BeanUtil.copyProperties(comment, vo);

        Users user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            vo.setCommenterAvatar(user.getAvatar());
            vo.setCommenterName(user.getNickname());
        }

        if (comment.getReplyUserId() != null) {
            Users replyUser = userMapper.selectById(comment.getReplyUserId());
            if (replyUser != null) {
                vo.setReplyUserName(replyUser.getNickname());
            }
        }

        // 从Redis获取最新的点赞数
        String likeCountKey = COMMENT_LIKE_COUNT_KEY + comment.getId();
        String likeCountStr = stringRedisTemplate.opsForValue().get(likeCountKey);
        if (likeCountStr != null) {
            vo.setLikeCount(Integer.parseInt(likeCountStr));
        }

        return vo;
    }
}
