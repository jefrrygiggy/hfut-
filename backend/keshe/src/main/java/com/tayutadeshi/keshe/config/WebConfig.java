package com.tayutadeshi.keshe.config;

import com.tayutadeshi.keshe.interceptor.LoginInterceptor;
import com.tayutadeshi.keshe.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")  // 拦截所有路径
                .excludePathPatterns(
                        "/api/user/login",     // 放行登录
                        "/api/user/register",  // 放行注册
                        "/api/user/send-code", // 放行验证码
                        "/api/user/reset-password", // 放行重置密码
                        "/error"               // 放行错误页
                );
    }
}