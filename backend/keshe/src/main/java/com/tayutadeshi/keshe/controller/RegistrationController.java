package com.tayutadeshi.keshe.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tayutadeshi.keshe.common.Result;
import com.tayutadeshi.keshe.pojo.ExamItem;
import com.tayutadeshi.keshe.pojo.Registration;
import com.tayutadeshi.keshe.service.IExamItemService;
import com.tayutadeshi.keshe.service.IExamItemService;
import com.tayutadeshi.keshe.service.IRegistrationService;
import com.tayutadeshi.keshe.service.IRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

    @Autowired private IRegistrationService regService;
    @Autowired private IExamItemService examService;




}