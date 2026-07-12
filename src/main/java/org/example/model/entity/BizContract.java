package org.example.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("biz_contract")
public class BizContract {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String processInstanceId;

    private String businessKey;

    private String contractName;

    private Long applicantId;

    private String applicantName;

    private Long partyAId;

    private String partyAName;

    private Long partyBId;

    private String partyBName;

    private Long designatedApproverId;

    private String designatedApproverName;

    private BigDecimal amount;

    private String content;

    private LocalDate signDate;

    private String flowStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
