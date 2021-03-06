package com.mohress.training.service.teacher;

import lombok.Data;

/**
 * 教师查询体
 * Created by qx.wang on 2017/8/15.
 */
@Data
public class TeacherQuery {

    private String name;

    private String agencyId;

    private String keyword;

    private Integer pageSize = 10;

    private Integer pageIndex = 0;

    private String teacherId;

}
