package org.example.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class MenuVO {

    private Long menuId;

    private Long parentId;

    private String menuName;

    private String menuType;

    private String path;

    private String component;

    private String icon;

    private String permission;

    private Integer sort;

    private Integer visible;

    private List<MenuVO> children;
}
