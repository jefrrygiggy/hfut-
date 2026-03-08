package com.tayutadeshi.keshe.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tayutadeshi.keshe.common.Result;
import com.tayutadeshi.keshe.pojo.SysUser;
import com.tayutadeshi.keshe.service.ISysUserService;
import com.tayutadeshi.keshe.service.ISysUserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/user")
public class SysUserController {

    @Autowired private ISysUserService sysUserService;
    @Autowired(required = false) private JavaMailSender mailSender;

    // 简单的验证码缓存
    private static Map<String, String> codeCache = new ConcurrentHashMap<>();
    private static final String SECRET = "KesheSecretKey123456";

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody SysUser loginUser) {
        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginUser.getUsername()));

        if (user == null || !user.getPassword().equals(loginUser.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        // 生成 Token
        String token = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1天过期
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();

        Map<String, Object> data = new HashMap<>();
        data.put("token", "Bearer " + token); // 前端通常需要 Bearer 前缀
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("role", user.getRole());

        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody SysUser user) {
        if (sysUserService.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername())) > 0) {
            return Result.error("用户已存在");
        }
        user.setRole("student");
        sysUserService.save(user);
        return Result.success("注册成功");
    }

    @PostMapping("/send-code")
    public Result<String> sendCode(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("2075431043@qq.com"); // 需修改
            message.setTo(email);
            message.setSubject("验证码");
            message.setText("验证码：" + code);
            mailSender.send(message);
            codeCache.put(email, code);
            return Result.success("发送成功");
        } catch (Exception e) {
            // 开发环境如果没有配置邮箱，可以在控制台打印验证码方便测试
            System.out.println("模拟发送验证码至 " + email + ": " + code);
            codeCache.put(email, code);
            return Result.success("发送成功(模拟)");
        }
    }

    @PostMapping("/reset-password")
    public Result<String> resetPwd(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        String code = params.get("code");
        String newPassword = params.get("newPassword");

        if (!code.equals(codeCache.get(email))) return Result.error("验证码错误");

        SysUser user = sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, email));
        if (user == null) return Result.error("邮箱未注册");

        user.setPassword(newPassword);
        sysUserService.updateById(user);
        return Result.success("重置成功");
    }
}