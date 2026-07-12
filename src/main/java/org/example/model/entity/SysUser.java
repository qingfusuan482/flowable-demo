package org.example.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long userId;

    private String username;

    private String password;

    private String realName;

    private String email;

    private String phone;

    private Long deptId;

    private Long postId;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    /** 非表字段：部门名称 */
    @TableField(exist = false)
    private String deptName;

    /** 非表字段：岗位名称 */
    @TableField(exist = false)
    private String postName;

    /** 非表字段：角色列表 */
    @TableField(exist = false)
    private java.util.List<SysRole> roles;
}
