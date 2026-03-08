package com.tayutadeshi.keshe.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField; // ✅ 必须引入这个
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 考试项目表
 * </p>
 *
 * @author fan
 * @since 2026-01-09
 */
@Getter
@Setter
@TableName("exam_item")
@ApiModel(value = "ExamItem对象", description = "考试项目表")
public class ExamItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("考试名称")
    private String name;

    @ApiModelProperty("考试说明")
    private String description;

    @ApiModelProperty("报名费 (必须用Decimal保证金额精度)")
    private BigDecimal fee;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8") //  小写的 yyyy 和 dd
    @ApiModelProperty("考试时间")
    private LocalDateTime examTime;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8") //  小写的 yyyy 和 dd
    private LocalDateTime createTime;
    //  新增部分：非数据库字段
    @TableField(exist = false) // 关键：告诉MP这不是数据库列
    @ApiModelProperty("当前用户是否已报名 (仅前端展示用)")
    private Boolean isRegistered;
}