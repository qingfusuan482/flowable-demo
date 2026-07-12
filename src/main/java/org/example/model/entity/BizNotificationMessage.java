package org.example.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("biz_notification_message")
public class BizNotificationMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    /** TODO / DONE / NOTIFY */
    private String messageType;

    private String title;

    private String content;

    private String processInstanceId;

    private String processDefinitionKey;

    private String processDefinitionName;

    private String taskId;

    private String taskName;

    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
