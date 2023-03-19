package com.xuecheng.model.dto;

import com.xuecheng.model.po.CourseCategory;

import java.util.List;

public class CourseCategoryTreeDto extends CourseCategory {
    List<CourseCategoryTreeDto> childrenTreeNodes;
}
