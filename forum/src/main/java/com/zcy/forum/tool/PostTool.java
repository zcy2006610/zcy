package com.zcy.forum.tool;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zcy.forum.domain.entity.Posts;
import com.zcy.forum.domain.vo.HotPostVO;
import com.zcy.forum.mapper.primary.PostMapper;
import com.zcy.forum.utils.ToolResultHolder;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class PostTool {
    @Autowired
    private PostMapper postMapper;

    @Tool(description = "根据天数查询最近热门帖子")
    public List<HotPostVO> queryHotPosts(@ToolParam(description = "近几天") Integer days, ToolContext toolContext){
        LocalDateTime dateTime = LocalDate.now().atStartOfDay();
        LocalDateTime localDateTime = dateTime.minusDays(days);
        LambdaQueryWrapper<Posts> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(Posts::getCreatedAt,localDateTime);
        wrapper.eq(Posts::getIsHot,1);
        wrapper.last("limit 0,4");
        List<Posts> posts = postMapper.selectList(wrapper);
        if(posts==null){
           return new ArrayList<>();
        }
        List<HotPostVO> hotPostVOS = posts.stream().map(p -> {
            HotPostVO hotPostVO = new HotPostVO();
            BeanUtils.copyProperties(p, hotPostVO);
            return hotPostVO;
        }).toList();
        String requestId = Convert.toStr(toolContext.getContext().get("requestId"));
        ToolResultHolder.put(requestId,"posts",hotPostVOS);
        return hotPostVOS;

    }


}
