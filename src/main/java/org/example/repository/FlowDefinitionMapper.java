package org.example.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.model.entity.FlowDefinition;

@Mapper
public interface FlowDefinitionMapper extends BaseMapper<FlowDefinition> {
}
