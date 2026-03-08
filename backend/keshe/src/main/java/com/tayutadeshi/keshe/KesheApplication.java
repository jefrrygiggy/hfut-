package com.tayutadeshi.keshe;

import org.mybatis.spring.annotation.MapperScan; // 1. Add this import
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;


@MapperScan("com.tayutadeshi.keshe.mapper") // 2. Add this annotation
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class KesheApplication {

    public static void main(String[] args) {
        SpringApplication.run(KesheApplication.class, args);
    }
}