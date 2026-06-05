package com.zcy.forum.controller;

import com.zcy.forum.common.Result;
import com.zcy.forum.domain.vo.CategoriesVo;
import com.zcy.forum.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/category")
@Tag(name = "分类模块", description = "查询所有板块")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    @GetMapping("/list")
    @Operation(summary = "查询所有板块")
    public Result<List<CategoriesVo>> queryTotalCategories(){
        return Result.ok(categoryService.listAllModel());
    }
    @GetMapping("/post/statics")
    @Operation(summary = "统计所有板块下的帖子个数")
    public Result<List<Map<Long,Integer>>> countPosts(){
        return Result.ok(categoryService.countWithCategory());
    }


    //TODO
    //后续根据业务决定是否启用
   /* @GetMapping("/children/{id}")
    @Operation(summary = "查询板块子板块")
    public Result<List<CategoriesVo>> queryChildren(@PathVariable Long id){
        return Result.ok(categoryService.listAllChildren(id));
    }*/

    //TODO
    //同上
    /*@GetMapping("/post/statics")
    @Operation(summary = "统计所有父板块下的帖子个数")
    public Result<List<Map<Long,Integer>>> countPostsOfCategories(){
        return Result.ok(categoryService.countsAll());
    }
    */

    //TODO
    /*@GetMapping("/children/statics/{id}")
    @Operation(summary = "统计某个子板块帖子数")
    public Result<Long> countById(@PathVariable Long id){
        return Result.ok(categoryService.countById(id));
    }
    */











}
