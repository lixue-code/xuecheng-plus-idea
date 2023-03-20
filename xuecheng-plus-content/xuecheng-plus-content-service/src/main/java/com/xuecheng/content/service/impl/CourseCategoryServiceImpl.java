package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        //将List<CourseCategoryTreeDto>转成map ,key 是id value 是 CourseCategoryTreeDto
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);
        /**
         * filter(item->!item.getId().equals("1")) : 过滤掉根节点
         */
        Map<String, CourseCategoryTreeDto> mapTmp = courseCategoryTreeDtos.stream().filter(item -> !id.equals(item.getId())).collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));
        //遍历List<CourseCategoryTreeDto>，将子节点放在父节点的childrenNodes属性中
        ArrayList<CourseCategoryTreeDto> courseCategoryTreeDtosList = new ArrayList<>();
        courseCategoryTreeDtos.stream().filter(item->!item.getId().equals("1")).forEach(item->{
            if(id.equals(item.getParentid())){
                courseCategoryTreeDtosList.add(item);
            }
            CourseCategoryTreeDto courseCategoryTreeDtoParent = mapTmp.get(item.getParentid());
            if(courseCategoryTreeDtoParent != null){
                if(courseCategoryTreeDtoParent.getChildrenTreeNodes() == null){
                    courseCategoryTreeDtoParent.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                courseCategoryTreeDtoParent.getChildrenTreeNodes().add(item);

            }
            //找到每个子节点，放在父节点中

        });
        return courseCategoryTreeDtosList;
    }
}
