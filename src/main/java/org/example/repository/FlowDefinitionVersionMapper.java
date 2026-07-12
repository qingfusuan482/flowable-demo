package org.example.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.model.entity.FlowDefinitionVersion;

import java.util.List;

@Mapper
public interface FlowDefinitionVersionMapper extends BaseMapper<FlowDefinitionVersion> {

    @Select("SELECT * FROM flow_definition_version WHERE definition_id = #{definitionId} AND deleted = 0 ORDER BY version DESC")
    List<FlowDefinitionVersion> selectByDefinitionId(@Param("definitionId") Long definitionId);

    @Select("SELECT MAX(version) FROM flow_definition_version WHERE definition_id = #{definitionId} AND deleted = 0")
    Integer selectMaxVersion(@Param("definitionId") Long definitionId);
}
