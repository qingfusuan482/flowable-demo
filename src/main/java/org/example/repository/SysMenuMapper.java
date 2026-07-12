package org.example.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.model.entity.SysMenu;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    @Select("SELECT * FROM sys_menu WHERE deleted = 0 ORDER BY sort ASC")
    List<SysMenu> selectAll();

    /** 查询用户拥有的菜单权限 */
    @Select("SELECT DISTINCT m.* FROM sys_menu m "
            + "INNER JOIN sys_role_menu rm ON m.menu_id = rm.menu_id "
            + "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id "
            + "WHERE ur.user_id = #{userId} AND m.deleted = 0 "
            + "ORDER BY m.sort ASC")
    List<SysMenu> selectByUserId(@Param("userId") Long userId);
}
