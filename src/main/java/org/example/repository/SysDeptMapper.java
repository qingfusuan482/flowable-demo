package org.example.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.model.entity.SysDept;

import java.util.List;

@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    @Select("SELECT * FROM sys_dept WHERE deleted = 0 ORDER BY sort ASC")
    List<SysDept> selectAll();
}
