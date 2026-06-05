package com.zcy.forum.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.zcy.forum.domain.entity.Categories;
import com.zcy.forum.domain.vo.CategoriesVo;

import java.util.List;
import java.util.Map;

public interface CategoryService extends IService<Categories> {

    List<CategoriesVo> listAllModel();

    List<CategoriesVo> listAllChildren(Long id);

    List<Map<Long,Integer>> countsAll();

    Long countById(Long id);

    List<Map<Long,Integer>> countWithCategory();
}
