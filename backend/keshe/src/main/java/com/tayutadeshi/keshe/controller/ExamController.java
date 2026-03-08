package com.tayutadeshi.keshe.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tayutadeshi.keshe.common.Result;
import com.tayutadeshi.keshe.pojo.ExamItem;
import com.tayutadeshi.keshe.pojo.SysEnrollment;
import com.tayutadeshi.keshe.service.IExamItemService;
import com.tayutadeshi.keshe.service.ISysEnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exam") // 对应前端 request.get('/exam/list')
public class ExamController {

    @Autowired
    private IExamItemService examService;

    @Autowired
    private ISysEnrollmentService enrollmentService;

    /**
     * 获取考试列表
     * @param userId (可选) 如果传了用户ID，会额外计算该用户是否已报名
     */
    @GetMapping("/list")
    public Result<List<ExamItem>> list(@RequestParam(required = false) Integer userId) {
        // 1. 获取所有发布的考试 (按时间排序可选)
        List<ExamItem> allExams = examService.list();

        // 2. 如果未登录(没传userId)，直接返回原始列表
        if (userId == null) {
            return Result.success(allExams);
        }

        // 3. 如果已登录，计算“是否已报名”
        // 3.1 查出该用户所有【已支付】的报名记录
        // SQL: select * from sys_enrollment where user_id = ? and status = 1
        List<SysEnrollment> myEnrollments = enrollmentService.list(
                new LambdaQueryWrapper<SysEnrollment>()
                        .eq(SysEnrollment::getUserId, userId)
                        .eq(SysEnrollment::getStatus, 1) //  重点：只看已支付的，没付钱的不算报名成功
        );

        // 3.2 提取出用户报过的 examId 集合，方便快速比对 (Set查询比List快)
        Set<Integer> registeredExamIds = myEnrollments.stream()
                .map(SysEnrollment::getExamId)
                .collect(Collectors.toSet());

        // 3.3 遍历所有考试，如果 ID 在集合里，就标记为 true
        for (ExamItem exam : allExams) {
            if (registeredExamIds.contains(exam.getId())) {
                exam.setIsRegistered(true); // 这里设置状态，前端按钮就会变灰
            }
        }

        return Result.success(allExams);
    }



    // 2. 新增考试 (管理员用)
    @PostMapping("/add")
    public Result add(@RequestBody ExamItem exam) {
        if (examService.save(exam)) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    // 3. 删除考试 (管理员用)
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        if (examService.removeById(id)) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    //  4. 修改考试 (可选)
    @PutMapping("/update")
    public Result update(@RequestBody ExamItem exam) {
        if (examService.updateById(exam)) {
            return Result.success("修改成功");
        }
        return Result.error("修改失败");
    }
}