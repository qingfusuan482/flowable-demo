package org.example.handler;

import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * 按流程变量解析审批人
 * 从流程变量中直接读取审批人用户名
 */
@Component
public class VariableBasedAssigneeHandler {

    /**
     * @param varName   变量名，如 partyA / partyB / designatedApprover
     * @param execution Flowable 执行上下文
     * @return 审批人用户名
     */
    public String resolve(String varName, DelegateExecution execution) {
        Object value = execution.getVariable(varName);
        if (value != null) {
            return value.toString();
        }
        return null;
    }
}
