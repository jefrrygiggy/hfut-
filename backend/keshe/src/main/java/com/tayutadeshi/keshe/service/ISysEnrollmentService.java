package com.tayutadeshi.keshe.service;

import com.tayutadeshi.keshe.pojo.SysEnrollment;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author fan
 * @since 2026-01-09
 */
public interface ISysEnrollmentService extends IService<SysEnrollment> {

    boolean updateStatus(Integer userId, Integer examId);
}
