package com.tayutadeshi.keshe.mapper;

import com.tayutadeshi.keshe.pojo.SysEnrollment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author fan
 * @since 2026-01-09
 */
public interface SysEnrollmentMapper extends BaseMapper<SysEnrollment> {
    int updateStatus(@Param("userId") Integer userId, @Param("examId") Integer examId);
}
