package org.example.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("flow_definition_version")
public class FlowDefinitionVersion {

    @TableId(type = IdType.AUTO)
    private Long versionId;

    private Long definitionId;

    private Integer version;

    private String bpmnXml;

    /** 对应 Flowable 的 ACT_RE_PROCDEF.ID_ */
    private String processDefinitionId;

    /** 对应 Flowable 的 ACT_RE_DEPLOYMENT.ID_ */
    private String deploymentId;

    /** 是否当前主版本 */
    private Integer isMain;

    private LocalDateTime publishTime;

    private Long publishUserId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
