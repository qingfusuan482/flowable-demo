package org.example.handler;

import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * 按指定用户ID/用户名解析审批人
 */
@Component
public class FixedAssigneeHandler {

    public String resolve(String userIds, DelegateExecution execution) {
        return userIds; // 直接返回传入的用户名
    }
}
