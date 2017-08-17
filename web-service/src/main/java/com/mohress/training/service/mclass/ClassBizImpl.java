package com.mohress.training.service.mclass;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mohress.training.dto.PageDto;
import com.mohress.training.dto.QueryDto;
import com.mohress.training.dto.mclass.ClassRequestDto;
import com.mohress.training.entity.mclass.TblClass;
import com.mohress.training.entity.mclass.TblClassMember;
import com.mohress.training.service.BaseManageService;
import com.mohress.training.service.ModuleBiz;
import com.mohress.training.util.Convert;
import com.mohress.training.util.JsonUtil;
import com.mohress.training.util.SequenceCreator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 班级管理
 * Created by qx.wang on 2017/8/16.
 */
@Slf4j
@Service
public class ClassBizImpl implements ModuleBiz {

    @Resource
    private BaseManageService classServiceImpl;

    @Override
    public void newModule(String o) {
        Preconditions.checkNotNull(o);
        ClassRequestDto classRequestDto = null;
        try {
            classRequestDto = JsonUtil.getInstance().convertToBean(ClassRequestDto.class, String.valueOf(o));
        } catch (Exception e) {
            log.error("新建机构反序列化失败 {}", o, e);
        }
        classServiceImpl.newModule(buildClass(classRequestDto));
    }

    @Override
    public void delete(List<String> ids) {
        Preconditions.checkArgument(!CollectionUtils.isEmpty(ids));
        classServiceImpl.delete(ids);
    }

    @Override
    public void update(String o) {
        Preconditions.checkNotNull(o);
        ClassRequestDto classRequestDto = null;
        try {
            classRequestDto = JsonUtil.getInstance().convertToBean(ClassRequestDto.class, String.valueOf(0));
        } catch (Exception e) {
            log.error("新建机构反序列化失败 {}", o, e);
        }
        classServiceImpl.update(buildUpdateTblClass(classRequestDto));
    }

    @Override
    public Object query(PageDto pageDto) {
        Preconditions.checkNotNull(pageDto);
        Preconditions.checkArgument(pageDto.getPage() > 0);
        Preconditions.checkArgument(pageDto.getPageSize() > 0);
        List<TblClass> tblClasses = classServiceImpl.query(buildClassQuery(pageDto));

        return Convert.convertClass(tblClasses);
    }

    private ClassQuery buildClassQuery(PageDto pageDto) {
        return new ClassQuery(pageDto.getPageSize(), pageDto.getPage());
    }

    private ClassStudent buildClass(ClassRequestDto classRequestDto) {
        TblClass tblClass = new TblClass();
        BeanUtils.copyProperties(classRequestDto, tblClass);
        tblClass.setClassId(SequenceCreator.getClassId());

        if (classRequestDto.getStudentIds() == null) {
            return new ClassStudent(tblClass, Collections.<TblClassMember>emptyList());
        }
        List<TblClassMember> classMembers = Lists.newArrayList();
        for (String id : classRequestDto.getStudentIds()) {
            TblClassMember tblClassMember = new TblClassMember();
            tblClassMember.setClassId(tblClass.getClassId());
            tblClassMember.setStudentId(id);
            classMembers.add(tblClassMember);
        }

        return new ClassStudent(tblClass, classMembers);
    }

    @Override
    public Object queryByKeyword(QueryDto queryDto) {
        throw new RuntimeException("暂不支持");
    }

    private TblClass buildUpdateTblClass(ClassRequestDto classRequestDto) {
        TblClass tblClass = new TblClass();
        BeanUtils.copyProperties(classRequestDto, tblClass);
        return tblClass;
    }
}
