package org.example.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_leave")
public class BizLeave {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String processInstanceId;

    private String businessKey;

    private Long applicantId;

    private String applicantName;

    /** ANNUAL / SICK / PERSONAL */
    private String leaveType;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer days;

    private String reason;

    /** DRAFT / APPROVING / APPROVED / REJECTED */
    private String flowStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
