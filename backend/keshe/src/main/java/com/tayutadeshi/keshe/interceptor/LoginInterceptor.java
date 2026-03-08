package com.tayutadeshi.keshe.interceptor;

import com.tayutadeshi.keshe.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 处理 OPTIONS 预检请求 (浏览器的探测请求，直接放行)
        if("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // ================== 核心修改开始 ==================

        // 2. 获取 Token (双重兼容逻辑)
        String token = request.getHeader("token"); // 先尝试获取自定义 header "token"
log.info(token);
        // 如果没拿到，再尝试获取标准的 "Authorization" header
        if (token == null || token.isEmpty()) {
            String authHeader = request.getHeader("Authorization");
            // 标准写法通常是 "Bearer xxxxx"，我们需要把 "Bearer " 去掉
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7); // 截取掉前7位
            }
        }
        log.info(token);

        // ================== 核心修改结束 ==================

        // 3. 校验 token
        try {
            if (token == null || token.isEmpty()) {
                throw new Exception("Token为空");
            }
            // 解析令牌，如果过期或篡改，这里会抛出异常
            Claims claims = JwtUtils.parseToken(token);

            // 把解析出来的 userId 放入 request，方便 Controller 用
            request.setAttribute("userId", claims.get("userId"));
            request.setAttribute("role", claims.get("role"));

            return true; // 放行

        } catch (Exception e) { // 直接捕获 Exception 即可，因为新版 JJWT 异常类层级变了
            e.printStackTrace(); // 打印报错
            log.error("JWT校验失败: {}", e.getMessage()); // 如果有 @Slf4j

            response.setStatus(401);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":401, \"msg\":\"身份验证失败，请重新登录\"}");
            return false;
        }}
}