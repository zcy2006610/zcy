package com.zcy.forum.common;

import lombok.Data;
import java.util.List;

/**
 * 通用分页返回结果
 */
@Data
public class PageResult<T> {
    private long total;        // 总条数
    private long pages;        // 总页数
    private long current;      // 当前页码
    private long size;         // 每页条数
    private List<T> records;   // 当前页数据列表
}