package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import java.util.List;

public interface CourseCategoryService {
    /**
     * 查询分类的树型列表
     * @param id
     * @return
     */
    public List<CourseCategoryTreeDto> queryTreeNodes(String id);
}
