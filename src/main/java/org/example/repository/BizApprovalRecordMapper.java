package org.example.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.model.entity.BizApprovalRecord;

import java.util.List;

@Mapper
public interface BizApprovalRecordMapper extends BaseMapper<BizApprovalRecord> {

    @Select("SELECT * FROM biz_approval_record WHERE process_instance_id = #{piId} AND deleted = 0 ORDER BY create_time ASC")
    List<BizApprovalRecord> selectByProcessInstanceId(@Param("piId") String processInstanceId);
}
