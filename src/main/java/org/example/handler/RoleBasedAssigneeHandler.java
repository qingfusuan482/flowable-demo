package org.example.handler;

import lombok.RequiredArgsConstructor;
import org.example.model.entity.SysDept;
import org.example.model.entity.SysUser;
import org.example.repository.SysDeptMapper;
import org.example.repository.SysUserMapper;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleBasedAssigneeHandler {

    private final SysUserMapper userMapper;
    private final SysDeptMapper deptMapper;

    /**
     * 根据角色编码解析审批人
     * @param roleCode  角色编码: dept_manager / hr_role
     * @param execution Flowable 执行上下文
     * @return 审批人用户名
     */
    public String resolve(String roleCode, DelegateExecution execution) {
        // 获取流程发起人
        String starter = (String) execution.getVariable("starter");

        // 部门经理角色：查发起人所在部门的负责人
        if ("dept_manager".equals(roleCode) && starter != null) {
            SysUser starterUser = userMapper.selectByUsername(starter);
            if (starterUser != null && starterUser.getDeptId() != null) {
                SysDept dept = deptMapper.selectById(starterUser.getDeptId());
                if (dept != null && dept.getLeader() != null) {
                    return dept.getLeader();
                }
            }
        }

        // 通用：查拥有该角色的第一个启用用户
        SysUser user = userMapper.findFirstByRoleCode(roleCode);
        if (user != null) {
            return user.getUsername();
        }

        // 兜底返回 starter
        return starter;
    }
}
