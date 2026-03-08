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

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author fan
 * @since 2026-01-09
 */
@Getter
@Setter
@TableName("sys_user")
@ApiModel(value = "SysUser对象", description = "用户表")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户名/学号")
    private String username;

    @ApiModelProperty("密码 (实际项目建议加密存储)")
    private String password;

    @ApiModelProperty("邮箱 (用于找回密码)")
    private String email;

    @ApiModelProperty("角色: student-学生, admin-管理员")
    private String role;

    @ApiModelProperty("注册时间")
    private LocalDateTime createTime;
}
