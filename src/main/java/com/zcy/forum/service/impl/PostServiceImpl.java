package com.zcy.forum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zcy.forum.annotation.CacheEvict;
import com.zcy.forum.annotation.CacheResult;
import com.zcy.forum.common.PageResult;
import com.zcy.forum.domain.dto.*;
import com.zcy.forum.domain.entity.Categories;
import com.zcy.forum.domain.entity.PostDrafts;
import com.zcy.forum.domain.entity.Posts;
import com.zcy.forum.domain.entity.Users;
import com.zcy.forum.domain.vo.PostDetailVo;
import com.zcy.forum.domain.vo.PostDraftsVo;
import com.zcy.forum.domain.vo.PostsVo;
import com.zcy.forum.mapper.primary.CategoryMapper;
import com.zcy.forum.mapper.primary.PostDraftsMapper;
import com.zcy.forum.mapper.primary.PostMapper;
import com.zcy.forum.mapper.primary.UserMapper;
import com.zcy.forum.service.PostService;
import com.zcy.forum.utils.DbTimeToLongUtil;
import com.zcy.forum.utils.RabbitMqUtil;
import com.zcy.forum.utils.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Posts> implements PostService {
    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private PostDraftsMapper postDraftsMapper;

    @Autowired
    private RabbitMqUtil mqUtil;



    @Override
    @CacheResult(key = "#id",prefix = "post:info:cache:",expire = 130)
    public PostDetailVo detail(Long id) {
        Posts posts = query().eq("id", id).one();
        PostDetailVo postDetailVo = new PostDetailVo();
        BeanUtil.copyProperties(posts,postDetailVo);
        Long userId = posts.getUserId();
        Users users = userMapper.selectById(userId);
        if(users!=null){
            postDetailVo.setPublisherAvatar(users.getAvatar());
            postDetailVo.setPublisherName(users.getNickname());
        }

        return postDetailVo;
    }

    @Override
    public Long publish(PostPublishDTO publishDTO) {
        // 1. 获取当前登录用户ID
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        // 2. 验证分类ID是否存在（简化版，实际应查询数据库）
        if (publishDTO.getCategoryId() == null || publishDTO.getCategoryId() <= 0) {
            throw new RuntimeException("分类ID无效");
        }
        Long categoryId = publishDTO.getCategoryId();
        Categories byId = categoryMapper.selectById(categoryId);
        if(byId==null||byId.getParentId()==0){
            throw new RuntimeException("分类ID无效");
        }
        // 3. 创建帖子对象
        Posts posts = new Posts();
        BeanUtil.copyProperties(publishDTO, posts);
        
        // 4. 设置默认值和业务字段
        posts.setUserId(userId);
        posts.setViewCount(0);
        posts.setLikeCount(0);
        posts.setCommentCount(0);
        posts.setCollectCount(0);
        posts.setShareCount(0);
        posts.setIsTop(0);
        posts.setIsEssence(0);
        posts.setIsHot(0);
        posts.setIsLock(0);
        posts.setAuditStatus(0); // 假设默认审核通过
        posts.setStatus(1);
        posts.setCreatedAt(new java.sql.Date(System.currentTimeMillis()));
        posts.setUpdatedAt(new java.sql.Date(System.currentTimeMillis()));
        
        // 5. 生成帖子别名(slug)：将标题转换为URL友好的格式
        String slug = generateSlug(publishDTO.getTitle());
        posts.setSlug(slug);
        
        // 6. 处理摘要：如果为空，自动从内容中提取
        if (org.springframework.util.StringUtils.isEmpty(publishDTO.getExcerpt())) {
            String excerpt = generateExcerpt(publishDTO.getContent());
            posts.setExcerpt(excerpt);
        }
        
        // 7. 保存到数据库
        postMapper.insert(posts);
        
        log.info("用户 {} 发布帖子成功，帖子ID: {} 待审核", userId, posts.getId());
        PostReviewDTO reviewDTO = new PostReviewDTO();
        BeanUtil.copyProperties(posts,reviewDTO);
        reviewDTO.setId(posts.getId());
        String s = JSONUtil.toJsonStr(reviewDTO);
        mqUtil.sendPostMsg(s);
        return posts.getId();
    }
    
    /**
     * 生成帖子别名(slug)
     */
    private String generateSlug(String title) {
        // 简单实现：转换为小写，替换空格为连字符，移除特殊字符
        return title.toLowerCase()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-|$-", "");
    }
    
    /**
     * 生成帖子摘要
     */
    private String generateExcerpt(String content) {
        // 简单实现：移除HTML标签，截取前100个字符
        String plainText = content.replaceAll("<[^>]*>", "");
        return plainText.length() > 100 ? plainText.substring(0, 20) + "..." : plainText;
    }

    @Override
    @CacheEvict(key = "#updateDTO.id",prefix = "post:info:cache:")
    public void updatePost(PostUpdateDTO updateDTO) {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        // 查询帖子是否存在
        Posts post = postMapper.selectById(updateDTO.getId());
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        // 验证是否是帖子作者
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改他人帖子");
        }

        // 验证分类ID是否存在
        if (updateDTO.getCategoryId() != null) {
            Categories category = categoryMapper.selectById(updateDTO.getCategoryId());
            if (category == null || category.getParentId() == 0) {
                throw new RuntimeException("分类ID无效");
            }
        }

        // 更新帖子信息
        BeanUtil.copyProperties(updateDTO, post);
        post.setUpdatedAt(new java.sql.Date(System.currentTimeMillis()));
        
        // 重新生成slug
        if (updateDTO.getTitle() != null && !updateDTO.getTitle().isEmpty()) {
            post.setSlug(generateSlug(updateDTO.getTitle()));
        }

        postMapper.updateById(post);
        log.info("用户 {} 更新帖子成功，帖子ID: {}", userId, post.getId());
    }

    @Override
    public PostUpdateDTO trace(Long id) {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Posts post = postMapper.selectById(id);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        // 验证是否是帖子作者
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权查看他人帖子");
        }

        PostUpdateDTO dto = new PostUpdateDTO();
        BeanUtil.copyProperties(post, dto);
        return dto;
    }

    @Override
    @CacheEvict(key = "#id",prefix = "post:info:cache:")
    public void logicDelete(Long id) {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        Posts post = postMapper.selectById(id);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        // 验证是否是帖子作者
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除他人帖子");
        }

        // 逻辑删除：将状态改为0（软删除）
        post.setStatus(0);
        post.setUpdatedAt(new java.sql.Date(System.currentTimeMillis()));
        postMapper.updateById(post);
        log.info("用户 {} 逻辑删除帖子成功，帖子ID: {}", userId, id);
    }

    @Override
    @CacheResult(prefix = "my:post:cache:",key ="#userId")
    public List<PostsVo> queryMyPost(Long userId) {
        // 查询用户发布的所有帖子（状态为正常的）
        QueryWrapper<Posts> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .eq("status", 1)
                   .orderByDesc("created_at");

        List<Posts> postsList = postMapper.selectList(queryWrapper);
        
        return postsList.stream().map(post -> {
            PostsVo vo = new PostsVo();
            BeanUtil.copyProperties(post, vo);
            vo.setCreatedAt(DbTimeToLongUtil.dbDateToSecondTimestamp(post.getCreatedAt()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(key = "#editDTO.id",prefix = "post:info:cache:")
    public void editMyPost(PostEditDTO editDTO) {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        if (editDTO.getId() == null) {
            throw new RuntimeException("帖子ID不能为空");
        }

        Posts post = postMapper.selectById(editDTO.getId());
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        // 验证是否是帖子作者
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权编辑他人帖子");
        }

        // 更新帖子内容
        if (editDTO.getTitle() != null) {
            post.setTitle(editDTO.getTitle());
            post.setSlug(generateSlug(editDTO.getTitle()));
        }
        if (editDTO.getContent() != null) {
            post.setContent(editDTO.getContent());
        }
        
        post.setUpdatedAt(new java.sql.Date(System.currentTimeMillis()));
        postMapper.updateById(post);
        log.info("用户 {} 编辑帖子成功，帖子ID: {}", userId, post.getId());
    }

    @Override
    @Transactional
    public Long save2draft(Post2DraftDTO draftDTO) {
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        PostDrafts draft = new PostDrafts();
        BeanUtil.copyProperties(draftDTO, draft);
        draft.setUserId(userId);
        draft.setPostId(0L); // 0表示新帖
        draft.setCreatedAt(LocalDateTime.now());
        draft.setUpdatedAt(LocalDateTime.now());

        postDraftsMapper.insert(draft);
        log.info("用户 {} 保存草稿成功，草稿ID: {}", userId, draft.getId());
        return draft.getId();
    }

    @Override
    @CacheResult(
            key = "#pageNum + ':' + #pageSize + ':' + (#params['categoryId']?:0)",
            prefix = "post:page:"
    )
    public PageResult<PostsVo> getPage(Integer pageNum, Integer pageSize, Map<String, Object> params) {
        // 1. 参数校验和默认值设置
        if (pageNum == null || pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize <= 0 || pageSize >= 20) {
            pageSize = 10;
        }

        // 2. 构建查询条件
        LambdaQueryWrapper<Posts> wrapper = new LambdaQueryWrapper<>();
        
        // 分类ID查询
        Long categoryId = Convert.toLong(params.get("categoryId"), null);
        if (categoryId != null) {
            wrapper.eq(Posts::getCategoryId, categoryId);
        }
        
        // 关键词搜索（优化：同时搜索标题和内容）
        String key = Convert.toStr(params.get("key"), null);
        if (StrUtil.isNotBlank(key)) {
            wrapper.and(w -> w.like(Posts::getTitle, key).or().like(Posts::getContent, key));
        }
        
        // 用户ID查询
        Long userId = Convert.toLong(params.get("userId"), null);
        if (userId != null) {
            wrapper.eq(Posts::getUserId, userId);
        }
        
        // 状态过滤
        wrapper.eq(Posts::getStatus, 1L);
        
        // 排序优化：按创建时间倒序，确保最新的帖子排在前面
        wrapper.orderByDesc(Posts::getCreatedAt);

        // 3. 执行分页查询
        Page<Posts> page = new Page<>(pageNum, pageSize);
        postMapper.selectPage(page, wrapper);

        // 4. 转换为VO对象
        List<PostsVo> records = page.getRecords().stream()
                .map(post -> {
                    PostsVo vo = new PostsVo();
                    BeanUtil.copyProperties(post, vo);
                    // 转换创建时间为时间戳
                    if (post.getCreatedAt() != null) {
                        vo.setCreatedAt(DbTimeToLongUtil.dbDateToSecondTimestamp(post.getCreatedAt()));
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        // 5. 构建分页结果
        PageResult<PostsVo> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setPages(page.getPages());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setRecords(records);

        return result;
    }

    @Override
    public PostDraftsVo getDraft(Long id) {
        return null;
    }
}
