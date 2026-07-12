package org.example.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ProcessInstanceVO {

    private String processInstanceId;
    private String processDefinitionId;
    private String processDefinitionKey;
    private String processDefinitionName;
    private Integer processDefinitionVersion;
    private String businessKey;
    private String startUserId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    /** RUNNING / COMPLETED / TERMINATED */
    private String flowStatus;
    private String currentTaskName;
    private Map<String, Object> processVariables;
}
