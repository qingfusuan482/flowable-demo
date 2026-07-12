package org.example.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("flow_work_order")
public class FlowWorkOrder {

    @TableId(type = IdType.AUTO)
    private Long workOrderId;

    private String processDefinitionKey;

    private String processDefinitionName;

    private String processDefinitionId;

    private String processInstanceId;

    private String businessKey;

    private String currentTaskId;

    private String currentTaskName;

    private String currentTaskDefinitionKey;

    /** RUNNING / COMPLETED / TERMINATED */
    private String flowStatus;

    private String submitUsername;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
