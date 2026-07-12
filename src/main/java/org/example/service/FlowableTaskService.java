package org.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.BusinessException;
import org.example.model.entity.BizApprovalRecord;
import org.example.model.entity.FlowWorkOrder;
import org.example.model.entity.SysUser;
import org.example.repository.BizApprovalRecordMapper;
import org.example.repository.FlowWorkOrderMapper;
import org.example.repository.SysUserMapper;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowableTaskService {

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final FlowWorkOrderMapper workOrderMapper;
    private final BizApprovalRecordMapper approvalRecordMapper;
    private final SysUserMapper userMapper;

    /** 查询待办任务 */
    public List<Map<String, Object>> todoList(String username) {
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(username)
                .orderByTaskCreateTime().desc()
                .list();

        return tasks.stream().map(task -> {
            Map<String, Object> map = new HashMap<>();
            map.put("taskId", task.getId());
            map.put("taskName", task.getName());
            map.put("taskDefinitionKey", task.getTaskDefinitionKey());
            map.put("processInstanceId", task.getProcessInstanceId());
            map.put("processDefinitionId", task.getProcessDefinitionId());
            map.put("assignee", task.getAssignee());
            map.put("createTime", task.getCreateTime());
            // 查工单获取流程名称
            FlowWorkOrder order = workOrderMapper.selectOne(
                    new LambdaQueryWrapper<FlowWorkOrder>().eq(FlowWorkOrder::getProcessInstanceId, task.getProcessInstanceId()));
            if (order != null) {
                map.put("processDefinitionName", order.getProcessDefinitionName());
                map.put("submitUsername", order.getSubmitUsername());
            }
            return map;
        }).collect(Collectors.toList());
    }

    /** 查询已办任务 */
    public List<Map<String, Object>> doneList(String username) {
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(username)
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc()
                .list();

        return tasks.stream().map(task -> {
            Map<String, Object> map = new HashMap<>();
            map.put("taskId", task.getId());
            map.put("taskName", task.getName());
            map.put("taskDefinitionKey", task.getTaskDefinitionKey());
            map.put("processInstanceId", task.getProcessInstanceId());
            map.put("processDefinitionId", task.getProcessDefinitionId());
            map.put("assignee", task.getAssignee());
            map.put("createTime", task.getCreateTime());
            map.put("endTime", task.getEndTime());
            map.put("durationInMillis", task.getDurationInMillis());
            // 查审批记录
            BizApprovalRecord record = approvalRecordMapper.selectOne(
                    new LambdaQueryWrapper<BizApprovalRecord>().eq(BizApprovalRecord::getTaskId, task.getId()));
            if (record != null) {
                map.put("approvalType", record.getApprovalType());
                map.put("taskComment", record.getTaskComment());
            }
            FlowWorkOrder order = workOrderMapper.selectOne(
                    new LambdaQueryWrapper<FlowWorkOrder>().eq(FlowWorkOrder::getProcessInstanceId, task.getProcessInstanceId()));
            if (order != null) {
                map.put("processDefinitionName", order.getProcessDefinitionName());
            }
            return map;
        }).collect(Collectors.toList());
    }

    /** 完成任务（审批） */
    @Transactional
    public void completeTask(String taskId, String username, String approvalType, String comment) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在或已处理");
        }
        if (!username.equals(task.getAssignee())) {
            throw new BusinessException("您不是该任务的审批人");
        }

        // 设置审批意见变量
        taskService.setVariable(taskId, "approvalComment", comment);

        if ("REJECT".equals(approvalType)) {
            // 驳回：直接删除流程（简化实现，实际应跳转到驳回节点）
            runtimeService.deleteProcessInstance(task.getProcessInstanceId(), "rejected");
        } else {
            // 审批通过
            taskService.complete(taskId);
        }
        log.info("任务完成: {} by {}, action={}", taskId, username, approvalType);
    }

    /** 转办 */
    @Transactional
    public void delegateTask(String taskId, String fromUser, String toUser) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        // 写留痕
        writeRecord(task, "DELEGATE", "转办给 " + toUser, fromUser);
        // 修改办理人
        taskService.setAssignee(taskId, toUser);
    }

    /** 获取审批留痕 */
    public List<BizApprovalRecord> getHistory(String processInstanceId) {
        return approvalRecordMapper.selectByProcessInstanceId(processInstanceId);
    }

    private void writeRecord(Task task, String type, String comment, String username) {
        BizApprovalRecord record = new BizApprovalRecord();
        record.setProcessInstanceId(task.getProcessInstanceId());
        record.setTaskId(task.getId());
        record.setTaskKey(task.getTaskDefinitionKey());
        record.setTaskName(task.getName());
        record.setExecutionId(task.getExecutionId());
        record.setAssignee(username);
        record.setApprovalType(type);
        record.setTaskComment(comment);
        record.setStartTime(task.getCreateTime() != null ?
                java.time.LocalDateTime.ofInstant(task.getCreateTime().toInstant(), java.time.ZoneId.systemDefault()) : null);
        record.setEndTime(java.time.LocalDateTime.now());
        record.setCreateUsername(username);
        Long userId = getUserId(username);
        record.setCreateUserId(userId);
        record.setCreateTime(java.time.LocalDateTime.now());
        approvalRecordMapper.insert(record);
    }

    private Long getUserId(String username) {
        SysUser user = userMapper.selectByUsername(username);
        return user != null ? user.getUserId() : null;
    }
}
