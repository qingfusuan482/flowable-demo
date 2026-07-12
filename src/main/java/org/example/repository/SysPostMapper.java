package org.example.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.model.entity.SysPost;

@Mapper
public interface SysPostMapper extends BaseMapper<SysPost> {
}
