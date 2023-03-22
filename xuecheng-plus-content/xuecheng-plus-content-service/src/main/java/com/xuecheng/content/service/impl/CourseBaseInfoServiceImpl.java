package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto) {
        //分页差寻
        LambdaQueryWrapper<CourseBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()), CourseBase::getName, queryCourseParamsDto.getCourseName());
        wrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParamsDto.getAuditStatus());

        pageParams.setPageNo(pageParams.getPageNo());
        pageParams.setPageSize(pageParams.getPageSize());

        Page<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, wrapper);
        List<CourseBase> records = pageResult.getRecords();
        long total = pageResult.getTotal();

        PageResult<CourseBase> courseBasePageResult = new PageResult<CourseBase>(records, total, pageParams.getPageNo(), pageParams.getPageSize());
        System.out.println(courseBasePageResult);
        return courseBasePageResult;
    }

    @Override
    @Transactional
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {
//        if(StringUtils.isEmpty(addCourseDto.getName())){
//            throw new XueChengPlusException("课程名为空");
//        }
//        if(StringUtils.isEmpty(addCourseDto.getMt())){
//            throw new XueChengPlusException("课程分类为空");
//        }
//        if(StringUtils.isEmpty(addCourseDto.getSt())){
//            throw new XueChengPlusException("课程分类为空");
//        }
//        if(StringUtils.isEmpty(addCourseDto.getGrade())){
//            throw new XueChengPlusException("课程等级为空");
//        }
//        if(StringUtils.isEmpty(addCourseDto.getUsers())){
//            throw new XueChengPlusException("适用人群为空");
//        }
//        if (StringUtils.isBlank(addCourseDto.getCharge())) {
//            throw new XueChengPlusException("收费价格不能为空！");
//        }

        //添加课程基本信息
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto,courseBase);
        //设置审核状态
        courseBase.setAuditStatus("202002");
        //设置发布状态
        courseBase.setStatus("203001");
        //机构id
        courseBase.setCompanyId(companyId);
        //添加时间
        courseBase.setCreateDate(LocalDateTime.now());
        int insert = courseBaseMapper.insert(courseBase);
        if(insert<0){
            throw new XueChengPlusException("课程基本信息添加失败");
        }

        //向课程营销表保存课程营销信息
        //课程营销信息
        CourseMarket courseMarketNew = new CourseMarket();
        //获取添加后课程id
        Long id = courseBase.getId();
        //获取这个课程
        BeanUtils.copyProperties(addCourseDto,courseMarketNew);
        courseMarketNew.setId(id);

        //添加
        int i = saveCourseMarket(courseMarketNew);
        return getCourseBaseInfo(id);
    }

    /**
     * 根据id获取课程信息,包括基本信息和营销信息
     * @param courseId
     * @return
     */
    @Override
    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        //拿到基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            return null;
        }
        //拿到营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //构建CourseBaseInfoDto
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if(courseMarket != null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }

        //查询分类名称
        CourseCategory courseCategory = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategory.getName());
        return courseBaseInfoDto;
    }

    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId,EditCourseDto editCourseDto) {
        //获取课程信息
        Long courseId = editCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //数据合法性校验
        //业务校验
        //本机构只能修改本机构的课程
//        if(!companyId.equals(courseBase.getCompanyId())){
//            throw  new XueChengPlusException("本机构只能修改本机构的课程");
//        }
        //封装数据
        BeanUtils.copyProperties(editCourseDto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        courseBase.setChangePeople("lixue");

        //修改数据库
        int i = courseBaseMapper.updateById(courseBase);
        if(i <=0 ){
            throw new XueChengPlusException("修改课程失败");
        }
        //修改课程营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto,courseMarket);
        saveCourseMarket(courseMarket);
        //查询最新的课程信息并返回
        CourseBaseInfoDto courseBaseInfoDto = getCourseBaseInfo(courseId);
        return courseBaseInfoDto;
    }


    private int saveCourseMarket(CourseMarket courseMarket){
        //校验
        //收费规则
        String charge = courseMarket.getCharge();
//        if(StringUtils.isBlank(charge)){
//            throw new RuntimeException("收费规则没有选择");
//        }
//        //收费规则为收费
//        if(charge.equals("201001")){
//            if(courseMarket.getPrice() == null || courseMarket.getPrice().floatValue()<=0){
//                throw new XueChengPlusException("课程为收费价格不能为空且必须大于0");
//            }
//        }
        //添加
        CourseMarket courseMarketObj = courseMarketMapper.selectById(courseMarket.getId());
        if(courseMarketObj == null){
            //添加
            return courseMarketMapper.insert(courseMarket);
        }else{
            //复制传进来的对象所有属性到已经查询出来的对象中
            BeanUtils.copyProperties(courseMarket,courseMarketObj);
            courseMarketObj.setId(courseMarket.getId());
            //修改
            return courseMarketMapper.updateById(courseMarketObj);
        }

    }




}
