package org.example.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
public class ProcessStartRequest {

    /** 流程标识Key (如 leave-process) */
    @NotNull
    private String processDefinitionKey;

    /** 业务表单数据 */
    private Map<String, Object> formData;

    /** 流程变量（审批人配置等） */
    private Map<String, Object> variables;
}
