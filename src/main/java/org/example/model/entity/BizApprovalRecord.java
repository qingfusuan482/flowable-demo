package org.example.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_approval_record")
public class BizApprovalRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String processInstanceId;

    private String taskId;

    private String taskKey;

    private String taskName;

    private String executionId;

    private String assignee;

    /** APPROVE / REJECT / DELEGATE / ADD_SIGN */
    private String approvalType;

    private String taskComment;

    private String delegateAssignee;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long durationMs;

    private Long createUserId;

    private String createUsername;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
