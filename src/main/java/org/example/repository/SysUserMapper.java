package org.example.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.model.entity.SysUser;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT u.*, d.dept_name FROM sys_user u "
            + "LEFT JOIN sys_dept d ON u.dept_id = d.dept_id "
            + "WHERE u.username = #{username} AND u.deleted = 0")
    SysUser selectByUsername(@Param("username") String username);

    /** 查询拥有指定角色编码的用户（用于审批人解析） */
    @Select("SELECT u.* FROM sys_user u "
            + "INNER JOIN sys_user_role ur ON u.user_id = ur.user_id "
            + "INNER JOIN sys_role r ON ur.role_id = r.role_id "
            + "WHERE r.role_code = #{roleCode} AND u.deleted = 0 AND u.status = 1 "
            + "LIMIT 1")
    SysUser findFirstByRoleCode(@Param("roleCode") String roleCode);
}
