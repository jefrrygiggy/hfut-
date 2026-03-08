package com.tayutadeshi.keshe.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName("sys_enrollment")
@ApiModel(value = "SysEnrollment对象", description = "学生报名及成绩表")
public class SysEnrollment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("用户ID")
    private Integer userId;

    @ApiModelProperty("科目/考试ID")
    private Integer examId;

    @ApiModelProperty("状态：0-未支付，1-已支付")
    private Integer status;

    @ApiModelProperty("考试成绩")
    private Integer score;

    private LocalDateTime createTime;
}