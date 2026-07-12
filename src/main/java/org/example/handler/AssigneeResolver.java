package org.example.handler;

import lombok.RequiredArgsConstructor;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * 审批人解析门面，供 BPMN 表达式直接调用
 *
 * 使用方式（在 UserTask 的 assignee 属性中）：
 *   ${assigneeResolver.resolveByRole('dept_manager', execution)}
 *   ${assigneeResolver.resolveByVariable('partyA', execution)}
 *   ${assigneeResolver.resolveFixed('admin', execution)}
 */
@Component("assigneeResolver")
@RequiredArgsConstructor
public class AssigneeResolver {

    private final RoleBasedAssigneeHandler roleHandler;
    private final PostBasedAssigneeHandler postHandler;
    private final FixedAssigneeHandler fixedHandler;
    private final VariableBasedAssigneeHandler variableHandler;

    /**
     * 按角色编码解析审批人
     * BPMN: ${assigneeResolver.resolveByRole('dept_manager', execution)}
     */
    public String resolveByRole(String roleCode, DelegateExecution execution) {
        return roleHandler.resolve(roleCode, execution);
    }

    /**
     * 按岗位编码解析审批人
     * BPMN: ${assigneeResolver.resolveByPost('finance_director', execution)}
     */
    public String resolveByPost(String postCode, DelegateExecution execution) {
        return postHandler.resolve(postCode, execution);
    }

    /**
     * 按流程变量解析审批人
     * BPMN: ${assigneeResolver.resolveByVariable('partyB', execution)}
     */
    public String resolveByVariable(String varName, DelegateExecution execution) {
        return variableHandler.resolve(varName, execution);
    }

    /**
     * 按指定用户名解析
     * BPMN: ${assigneeResolver.resolveFixed('admin', execution)}
     */
    public String resolveFixed(String userIds, DelegateExecution execution) {
        return fixedHandler.resolve(userIds, execution);
    }
}
