package org.example.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.model.entity.SysRole;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
}
