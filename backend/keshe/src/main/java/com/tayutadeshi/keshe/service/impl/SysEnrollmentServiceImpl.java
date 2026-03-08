package com.tayutadeshi.keshe.service.impl;

import com.tayutadeshi.keshe.pojo.SysEnrollment;
import com.tayutadeshi.keshe.mapper.SysEnrollmentMapper;
import com.tayutadeshi.keshe.service.ISysEnrollmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fan
 * @since 2026-01-09
 */
@Service
public class SysEnrollmentServiceImpl extends ServiceImpl<SysEnrollmentMapper, SysEnrollment> implements ISysEnrollmentService {

    @Override
    public boolean updateStatus(Integer userId, Integer examId) {
        int rows = baseMapper.updateStatus(userId, examId);
        return rows > 0;
    }
}
