package com.tayutadeshi.keshe.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 报名记录表
 * </p>
 *
 * @author fan
 * @since 2026-01-09
 */
@Getter
@Setter
@ApiModel(value = "Registration对象", description = "报名记录表")
public class Registration implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("关联用户ID")
    private Long userId;

    @ApiModelProperty("关联考试ID")
    private Long examId;

    @ApiModelProperty("状态: 0-未支付, 1-已支付")
    private Byte status;

    @ApiModelProperty("成绩 (NULL表示未出分)")
    private BigDecimal score;

    @ApiModelProperty("报名时间")
    private LocalDateTime createTime;
}
