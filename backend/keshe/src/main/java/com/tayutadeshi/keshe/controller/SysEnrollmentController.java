package com.tayutadeshi.keshe.controller;

import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tayutadeshi.keshe.common.Result;
import com.tayutadeshi.keshe.pojo.ExamItem;
import com.tayutadeshi.keshe.pojo.SysEnrollment;
import com.tayutadeshi.keshe.service.IExamItemService;
import com.tayutadeshi.keshe.service.ISysEnrollmentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollment")
public class SysEnrollmentController {

    @Autowired
    private ISysEnrollmentService enrollmentService;
    @Autowired
    private IExamItemService examService;

    // =========================================================================
    // 🟢 1. 发起支付宝支付 (前端点击"支付宝报名"时调用)
    // =========================================================================
    @PostMapping("/alipay/pay")
    public Result<String> goAlipay(@RequestBody Map<String, Integer> params) {
        Integer userId = params.get("userId");
        Integer examId = params.get("examId");

        // 1. 基础校验
        if (userId == null || examId == null) {
            return Result.error("参数不完整");
        }

        // 2. 查询考试信息（主要是为了获取价格和名称）
        ExamItem exam = examService.getById(examId);
        if (exam == null) {
            return Result.error("考试科目不存在");
        }

        // 3. 检查是否已经支付过
        SysEnrollment exist = enrollmentService.getOne(
                new LambdaQueryWrapper<SysEnrollment>()
                        .eq(SysEnrollment::getUserId, userId)
                        .eq(SysEnrollment::getExamId, examId)
        );
        if (exist != null && exist.getStatus() == 1) {
            return Result.error("您已报名该科目，无需重复支付");
        }

        // 4. 构造唯一的订单号 (out_trade_no)
        // 格式：userId_examId_时间戳 (例如: 101_5_1705555555)
        // 回调时我们可以解析这个字符串知道是谁报了哪门课
        String outTradeNo = userId + "_" + examId + "_" + System.currentTimeMillis();

        // 5. 获取金额 (必须转为 String，保留2位小数)
        // 假设 ExamItem 里有一个 getFee() 方法返回 BigDecimal 或者 Double
        // 如果你的 fee 是 Integer，也可以直接 toString
        String totalAmount = exam.getFee() != null ? exam.getFee().toString() : "100.00";
        String subject = "考试报名费-" + exam.getName();

        try {
            // 6. 调用支付宝 SDK 生成支付表单
            // returnUrl: 支付成功后浏览器跳转回前端的地址 (可选，不填则停留在支付宝页面)
            // 建议填前端页面地址，如: http://localhost:5173/exam-list
            String returnUrl = "http://localhost:5173/";

            AlipayTradePagePayResponse response = Factory.Payment.Page()
                    .pay(subject, outTradeNo, totalAmount, returnUrl);

            // 7. 将生成的 HTML 表单返回给前端
            return Result.success(response.body);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("调用支付宝失败：" + e.getMessage());
        }
    }

    // =========================================================================
    // 🟢 2. 支付宝异步通知回调 (支付宝服务器自动调用此接口)
    // =========================================================================
    @PostMapping("/notify")
    public String payNotify(HttpServletRequest request) {
        // 1. 解析请求参数
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();

        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        // 2. 验签 (防止黑客伪造请求)
        try {
            // 调用 SDK 验证签名
            if (Factory.Payment.Common().verifyNotify(params)) {
                // 验证成功
                String tradeStatus = params.get("trade_status"); // 交易状态
                String outTradeNo = params.get("out_trade_no");  // 我们自己生成的订单号

                // 只有状态为 SUCCESS 或 FINISHED 时才算真正支付成功
                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {

                    System.out.println(">>> 支付宝回调成功，订单号: " + outTradeNo);

                    // 3. 解析订单号，提取 userId 和 examId
                    // 格式: userId_examId_timestamp
                    String[] parts = outTradeNo.split("_");
                    if (parts.length >= 2) {
                        Integer userId = Integer.parseInt(parts[0]);
                        Integer examId = Integer.parseInt(parts[1]);

                        // 4. 更新数据库状态 (改为 1-已支付)
                        LambdaUpdateWrapper<SysEnrollment> updateWrapper = new LambdaUpdateWrapper<>();
                        updateWrapper.eq(SysEnrollment::getUserId, userId)
                                .eq(SysEnrollment::getExamId, examId)
                                .set(SysEnrollment::getStatus, 1); // 设置为已支付

                        enrollmentService.update(updateWrapper);
                    }
                }
                return "success"; // 必须返回 success，否则支付宝会一直重发通知
            } else {
                System.out.println(">>> 支付宝验签失败");
                return "failure";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "failure";
        }
    }

    // =========================================================================
    // 🟡 3. 报名申请 (创建未支付订单) - 保持不变
    // =========================================================================
    @PostMapping("/apply")
    public Result<String> apply(@RequestBody Map<String, Integer> params) {
        Integer userId = params.get("userId");
        Integer examId = params.get("examId");

        SysEnrollment exist = enrollmentService.getOne(
                new LambdaQueryWrapper<SysEnrollment>()
                        .eq(SysEnrollment::getUserId, userId)
                        .eq(SysEnrollment::getExamId, examId)
        );

        if (exist != null) {
            if (exist.getStatus() == 1) {
                return Result.error("您已成功报名该科目");
            } else {
                return Result.success("订单已存在，请继续支付");
            }
        }

        SysEnrollment reg = new SysEnrollment();
        reg.setUserId(userId);
        reg.setExamId(examId);
        reg.setStatus(0); // 0:未支付
        reg.setCreateTime(LocalDateTime.now());

        enrollmentService.save(reg);
        return Result.success("报名订单创建成功");
    }

    // =========================================================================
    // 🔵 4. 我的成绩/报名记录查询 - 保持不变
    // =========================================================================
    @GetMapping("/my-scores")
    public Result<List<Map<String, Object>>> myScores(@RequestParam Integer userId) {
        LambdaQueryWrapper<SysEnrollment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysEnrollment::getUserId, userId)
                .eq(SysEnrollment::getStatus, 1) // 只查已支付
                .orderByDesc(SysEnrollment::getCreateTime);

        List<SysEnrollment> enrollments = enrollmentService.list(queryWrapper);
        List<Map<String, Object>> result = new ArrayList<>();

        for (SysEnrollment en : enrollments) {
            ExamItem exam = examService.getById(en.getExamId());
            if (exam != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("examName", exam.getName());
                map.put("examTime", exam.getExamTime());
                map.put("score", en.getScore());
                if (en.getScore() != null) {
                    map.put("status", "已出分");
                    map.put("scoreDisplay", en.getScore() + " 分");
                } else {
                    map.put("status", "阅卷中");
                    map.put("scoreDisplay", "--");
                }
                result.add(map);
            }
        }
        return Result.success(result);
    }
}