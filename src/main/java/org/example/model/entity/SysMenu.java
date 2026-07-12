package org.example.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.AUTO)
    private Long menuId;

    private Long parentId;

    private String menuName;

    /** DIRECTORY / MENU / BUTTON */
    private String menuType;

    private String path;

    private String component;

    private String icon;

    private String permission;

    private Integer sort;

    private Integer visible;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private java.util.List<SysMenu> children;
}
