package org.example.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("flow_definition")
public class FlowDefinition {

    @TableId(type = IdType.AUTO)
    private Long definitionId;

    private String name;

    private String flowKey;

    private Long categoryId;

    private String description;

    private String bpmnXml;

    /** DRAFT / PUBLISHED / DISABLED */
    private String status;

    private Integer currentVersion;

    private LocalDateTime latestPublishTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Long createUserId;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Long updateUserId;

    @TableLogic
    private Integer deleted;

    /** 非表字段：分类名称 */
    @TableField(exist = false)
    private String categoryName;
}
