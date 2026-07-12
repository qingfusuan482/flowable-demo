package org.example.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserInfoVO {

    private Long userId;

    private String username;

    private String realName;

    private String email;

    private String phone;

    private Long deptId;

    private String deptName;

    private Long postId;

    private String postName;

    private Integer status;

    /** 菜单树 */
    private List<MenuVO> menus;

    /** 权限标识列表 */
    private List<String> permissions;
}
