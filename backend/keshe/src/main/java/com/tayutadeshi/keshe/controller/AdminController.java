package com.tayutadeshi.keshe.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tayutadeshi.keshe.common.Result;
import com.tayutadeshi.keshe.pojo.ExamItem;
import com.tayutadeshi.keshe.pojo.SysEnrollment; //  替换为正确的实体
import com.tayutadeshi.keshe.pojo.SysUser;
import com.tayutadeshi.keshe.service.IExamItemService;
import com.tayutadeshi.keshe.service.ISysEnrollmentService; //  替换为正确的 Service
import com.tayutadeshi.keshe.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    //  注入 SysEnrollment 的 Service
    @Autowired private ISysEnrollmentService enrollmentService;
    @Autowired private IExamItemService examService;
    @Autowired private ISysUserService userService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        List<SysEnrollment> allEnrollments = enrollmentService.list();
        List<ExamItem> allExams = examService.list();

        // 1. 计算总收入 (修复类型匹配)
        BigDecimal totalIncome = BigDecimal.ZERO;
        List<SysEnrollment> paidList = allEnrollments.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 1)
                .collect(Collectors.toList());

        for (SysEnrollment enrollment : paidList) {
            ExamItem match = allExams.stream()
                    .filter(e -> e.getId().longValue() == enrollment.getExamId().longValue()) // 统一转long比较
                    .findFirst().orElse(null);
            if (match != null && match.getFee() != null) {
                totalIncome = totalIncome.add(match.getFee());
            }
        }

        // 2. 统计科目热度并排序
        List<Map<String, Object>> popularExams = new ArrayList<>();
        int totalCount = allEnrollments.size();

        for (ExamItem exam : allExams) {
            long count = allEnrollments.stream()
                    .filter(r -> r.getExamId().longValue() == exam.getId().longValue()) // 统一转long比较
                    .count();

            if (count > 0) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", exam.getName());
                map.put("count", count);
                // 计算百分比 (转为double计算防止精度丢失)
                int percent = totalCount == 0 ? 0 : (int) ((double) count / totalCount * 100);
                map.put("percent", percent);

                // 动态设置前端进度条状态
                if (percent > 50) map.put("status", "exception"); // 火爆-红色
                else if (percent > 20) map.put("status", "warning"); // 热门-橙色
                else map.put("status", "success"); // 普通-蓝色

                popularExams.add(map);
            }
        }

        //  关键步骤：增加排序逻辑 (按 count 从大到小)
        popularExams.sort((a, b) -> ((Long) b.get("count")).compareTo((Long) a.get("count")));

        Map<String, Object> data = new HashMap<>();
        data.put("totalStudents", userService.count());
        data.put("totalIncome", totalIncome);
        data.put("popularExams", popularExams);

        return Result.success(data);
    }

    // ==================== ✏模块二：成绩管理 ====================

    /**
     * 根据考试ID，查询该科目的学生列表
     */
    @GetMapping("/score/student-list")
    public Result<List<Map<String, Object>>> getStudentList(@RequestParam Integer examId) {
        // 1. 查询已支付的报名记录
        LambdaQueryWrapper<SysEnrollment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysEnrollment::getExamId, examId);
        queryWrapper.eq(SysEnrollment::getStatus, 1);

        List<SysEnrollment> enrollments = enrollmentService.list(queryWrapper);
        List<Map<String, Object>> resultList = new ArrayList<>();

        // 2. 提取所有 userId
        Set<Integer> userIds = enrollments.stream()
                .map(SysEnrollment::getUserId)
                .collect(Collectors.toSet());

        if (!userIds.isEmpty()) {
            // 3. 批量查询用户信息
            List<SysUser> users = userService.listByIds(userIds);

            // ✨ 关键修正：确保 Map 的 Key 类型与 en.getUserId() 一致 (Integer)
            Map<Integer, SysUser> userMap = users.stream()
                    .collect(Collectors.toMap(u -> u.getId().intValue(), u -> u));

            for (SysEnrollment en : enrollments) {
                Map<String, Object> item = new HashMap<>();
                item.put("userId", en.getUserId());
                item.put("score", en.getScore());

                // ✨ 从 userMap 获取用户信息并放入返回结果
                SysUser u = userMap.get(en.getUserId());
                if (u != null) {
                    item.put("username", u.getUsername()); // 放入姓名
                    item.put("email", u.getEmail());       // 放入邮箱
                } else {
                    item.put("username", "未知用户");
                    item.put("email", "-");
                }
                resultList.add(item);
            }
        }
        return Result.success(resultList);
    }

    /**
     * 录入/修改成绩
     */
    @PostMapping("/score/update")
    public Result updateScore(@RequestBody Map<String, Integer> params) {
        Integer userId = params.get("userId");
        Integer examId = params.get("examId");
        Integer score = params.get("score");






        if (score == null || score < 0 || score > 100) {
            return Result.error("分数必须在 0-100 之间");
        }

        // 使用 MyBatis-Plus 更新 SysEnrollment 表
        LambdaUpdateWrapper<SysEnrollment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysEnrollment::getUserId, userId)
                .eq(SysEnrollment::getExamId, examId)
                .set(SysEnrollment::getScore, score); // 更新 score 字段

        boolean success = enrollmentService.update(updateWrapper);
        return success ? Result.success("成绩保存成功") : Result.error("保存失败");
    }
}