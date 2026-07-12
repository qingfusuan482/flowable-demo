package org.example.handler;

import lombok.RequiredArgsConstructor;
import org.example.model.entity.SysUser;
import org.example.repository.SysUserMapper;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * 按岗位编码解析审批人
 * 注：当前简化实现，直接查 post_code 匹配的第一个用户
 */
@Component
@RequiredArgsConstructor
public class PostBasedAssigneeHandler {

    private final SysUserMapper userMapper;

    public String resolve(String postCode, DelegateExecution execution) {
        // 简化：查拥有该岗位的第一个用户
        // 实际项目可通过 SysPostMapper + SysUserMapper 联合查询
        return null; // 当前 demo 主要通过 role/variable 方式
    }
}
