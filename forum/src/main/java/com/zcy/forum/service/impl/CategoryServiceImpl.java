package com.zcy.forum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zcy.forum.domain.entity.Categories;
import com.zcy.forum.domain.vo.CategoriesVo;
import com.zcy.forum.mapper.primary.CategoryMapper;
import com.zcy.forum.mapper.primary.PostMapper;
import com.zcy.forum.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper,Categories> implements CategoryService {


    @Autowired
    private StringRedisTemplate redisTemplate;

    // 建议把缓存key抽成常量，便于维护
    private static final String CATEGORY_KEY = "cache:categories";
    // 缓存过期时间（根据业务调整，比如30分钟）
    private static final long CACHE_EXPIRE_MINUTES = 10L;

    // 抽离常量，便于维护
    private static final String CACHE_POST_COUNT_KEY = "cache:postCount";
    private static final long COUNT_EXPIRE_MINUTES = 5L;

    private static final String CACHE_C_POST_COUNT_KEY="cache:children:postCount";

    @Autowired
    private PostMapper postMapper;
    @Override
    public List<CategoriesVo> listAllModel() {
        // 1. 从Redis获取缓存（修复空指针问题：判断非空List）
        List<String> redisJsonList = redisTemplate.opsForList().range(CATEGORY_KEY, 0, -1);
        if (CollectionUtils.isNotEmpty(redisJsonList)) {
            // 2. 修复反序列化：JSON字符串→CategoriesVo（核心修复）
            return redisJsonList.stream()
                    .map(jsonStr -> JSONUtil.toBean(jsonStr, CategoriesVo.class))
                    .collect(Collectors.toList());
        }

        // 3. 缓存为空，查数据库（中小项目无需分布式锁，简化逻辑）
        List<CategoriesVo> dbList = query().eq("parent_id", 0L)
                .eq("status", 1)
                .list()
                .stream()
                .map(l -> BeanUtil.copyProperties(l, CategoriesVo.class))
                .collect(Collectors.toList()); // 替换toList()，避免不可变List问题

        // 4. 批量存入Redis（leftPushAll已是批量，性能最优）
        if (CollectionUtils.isNotEmpty(dbList)) {
            List<String> jsonList = dbList.stream()
                    .map(JSONUtil::toJsonStr)
                    .collect(Collectors.toList());
            redisTemplate.opsForList().leftPushAll(CATEGORY_KEY, jsonList);
            // 5. 新增缓存过期时间，避免数据永久有效导致脏数据
            redisTemplate.expire(CATEGORY_KEY, COUNT_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }

        return dbList;

    }

    @Override
    public List<CategoriesVo> listAllChildren(Long id) {
        return query().eq("parent_id",id).list().stream().map(c->{
            CategoriesVo categoriesVo = new CategoriesVo();
            BeanUtil.copyProperties(c,categoriesVo);
            return categoriesVo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<Long, Integer>> countsAll() {
        // 1. 查缓存
        Map<Object, Object> redisHash = redisTemplate.opsForHash().entries(CACHE_POST_COUNT_KEY);
        if (CollectionUtil.isNotEmpty(redisHash)) {
            Map<Long, Integer> resultMap = new HashMap<>();
            redisHash.forEach((k, v) -> {
                Long parentId = Convert.toLong(k, null);
                Integer count = Convert.toInt(v, 0);
                if (parentId != null) {
                    resultMap.put(parentId, count);
                }
            });
            return Collections.singletonList(resultMap);
        }

        // 2. 查询分类
        List<Categories> categories = query().eq("status", 1).list();
        if (CollectionUtil.isEmpty(categories)) {
            redisTemplate.opsForHash().put(CACHE_POST_COUNT_KEY, "EMPTY_FLAG", 0);
            redisTemplate.expire(CACHE_POST_COUNT_KEY, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            return new ArrayList<>();
        }

        // 3. 父ID -> 子ID集合
//        Map<Long, List<Long>> id2idmap = categories.stream()
//                .collect(Collectors.groupingBy(Categories::getParentId))
//                .entrySet().stream()
//                .filter(entry -> CollectionUtil.isNotEmpty(entry.getValue()))
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey,
//                        entry -> entry.getValue().stream()
//                                .map(Categories::getId)
//                                .filter(Objects::nonNull)
//                                .collect(Collectors.toList())
//                ));
        List<Long> fatherIds = categories.stream()
                .filter(c -> c.getParentId() == 0)
                .map(Categories::getId)
                .toList();
        Map<Long, List<Categories>> map1 = categories
                .stream()
                .collect(Collectors.groupingBy(Categories::getParentId));
        Map<Long,List<Long>> id2idmap=new HashMap<>();
        for(Long l:fatherIds){
            id2idmap.put(l,map1.getOrDefault(l,List.of()).stream().map(Categories::getId).collect(Collectors.toList()));
        }

        // 4. 查询帖子统计（这里返回 List<Map<String, Object>>）
        List<Map<String, Object>> postCountList = postMapper.countAll();

        // 5. 【修复】安全转换：BigInteger → Integer
        Map<Long, Integer> childPostCount = new HashMap<>();
        for (Map<String, Object> map : postCountList) {
            Object categoryIdObj = map.get("category_id");
            Object countObj = map.get("statics");

            Long categoryId = Convert.toLong(categoryIdObj, null);
            Integer count = Convert.toInt(countObj, 0);

            if (categoryId != null) {
                childPostCount.put(categoryId, count);
            }
        }

        // 6. 统计父板块帖子数
       Map<Long, Integer> parentPostCountMap = id2idmap.entrySet().stream()
               .collect(Collectors.toMap(
                       Map.Entry::getKey,
                       entry -> entry.getValue().stream()
                               .mapToInt(childId -> childPostCount.getOrDefault(childId, 0))
                               .sum(),
                       (v1, v2) -> v1,
                       HashMap::new
               ));


        // 7. 写入缓存
        if (CollectionUtil.isNotEmpty(parentPostCountMap)) {
            Map<String, String> saveMap = new HashMap<>();
            parentPostCountMap.forEach((k, v) -> saveMap.put(k.toString(), v.toString()));
            // 存入
            redisTemplate.opsForHash().putAll(CACHE_POST_COUNT_KEY, saveMap);
        } else {

            redisTemplate.opsForHash().putAll(CACHE_POST_COUNT_KEY, new HashMap<>());
        }
        redisTemplate.expire(CACHE_POST_COUNT_KEY, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return Collections.singletonList(parentPostCountMap);
    }

    @Override
    public Long countById(Long id) {
        // 1. 非空校验（防止id=null导致缓存key异常）
        if (id == null) {
            throw new RuntimeException("板块id为空");
        }

        String key = CACHE_C_POST_COUNT_KEY + id;

        // 2. 查缓存
        String cacheValue = redisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(cacheValue)) {
            // 安全转换，防止缓存脏数据
            return Long.valueOf(cacheValue);
        }

        // 3. 查数据库
        Long count = postMapper.countById(id);
        // 兜底 null 变成 0，避免存入null到Redis
        count = count == null ? 0L : count;

        // 4. 存入缓存 + 设置过期（原子操作，比分开set+expire好）
        redisTemplate.opsForValue().set(key, count.toString(), CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return count;
    }


}
