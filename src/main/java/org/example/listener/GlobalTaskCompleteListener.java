package org.example.listener;

import org.example.model.entity.BizApprovalRecord;
import org.example.model.entity.BizNotificationMessage;
import org.example.model.entity.SysUser;
import org.example.repository.BizApprovalRecordMapper;
import org.example.repository.BizNotificationMessageMapper;
import org.example.repository.SysUserMapper;
import org.example.util.SpringContextHolder;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.RuntimeService;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 任务完成监听器：写入审批留痕 + 发送已办消息
 */
@Component
public class GlobalTaskCompleteListener implements FlowableEventListener {

    @Override
    public void onEvent(FlowableEvent event) {
        if (!FlowableEngineEventType.TASK_COMPLETED.equals(event.getType())) return;

        FlowableEngineEntityEvent entityEvent = (FlowableEngineEntityEvent) event;
        TaskEntity task = (TaskEntity) entityEvent.getEntity();

        String assignee = task.getAssignee();
        if (assignee == null) return;

        // 延迟获取 Bean
        BizApprovalRecordMapper approvalRecordMapper = SpringContextHolder.getBean(BizApprovalRecordMapper.class);
        BizNotificationMessageMapper messageMapper = SpringContextHolder.getBean(BizNotificationMessageMapper.class);
        RuntimeService runtimeService = SpringContextHolder.getBean(RuntimeService.class);
        SysUserMapper userMapper = SpringContextHolder.getBean(SysUserMapper.class);

        Long userId = getUserId(assignee, userMapper);

        // === 1. 写入审批留痕 ===
        String comment = getVariable(task.getProcessInstanceId(), "approvalComment", runtimeService);
        BizApprovalRecord record = new BizApprovalRecord();
        record.setProcessInstanceId(task.getProcessInstanceId());
        record.setTaskId(task.getId());
        record.setTaskKey(task.getTaskDefinitionKey());
        record.setTaskName(task.getName());
        record.setExecutionId(task.getExecutionId());
        record.setAssignee(assignee);
        record.setApprovalType(comment != null && comment.contains("驳回") ? "REJECT" : "APPROVE");
        record.setTaskComment(comment);
        record.setStartTime(toLocalDateTime(task.getCreateTime()));
        record.setEndTime(LocalDateTime.now());
        if (task.getCreateTime() != null) {
            record.setDurationMs(System.currentTimeMillis() - task.getCreateTime().getTime());
        }
        record.setCreateUserId(userId);
        record.setCreateUsername(assignee);
        record.setCreateTime(LocalDateTime.now());
        approvalRecordMapper.insert(record);

        // === 2. 给流程发起人发已办消息 ===
        String starter = (String) runtimeService.getVariable(task.getProcessInstanceId(), "starter");
        if (starter != null && !starter.equals(assignee)) {
            BizNotificationMessage msg = new BizNotificationMessage();
            msg.setUserId(getUserId(starter, userMapper));
            msg.setUsername(starter);
            msg.setMessageType("DONE");
            msg.setTitle("审批结果通知");
            msg.setContent(String.format("节点【%s】已被【%s】%s",
                    task.getName(), assignee,
                    "REJECT".equals(record.getApprovalType()) ? "驳回" : "审批通过"));
            msg.setProcessInstanceId(task.getProcessInstanceId());
            msg.setTaskId(task.getId());
            msg.setTaskName(task.getName());
            msg.setIsRead(0);
            msg.setCreateTime(LocalDateTime.now());
            messageMapper.insert(msg);
        }
    }

    @Override
    public boolean isFailOnException() { return false; }

    @Override
    public String getOnTransaction() { return "committed"; }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() { return false; }

    private String getVariable(String piId, String varName, RuntimeService runtimeService) {
        try {
            Object val = runtimeService.getVariable(piId, varName);
            return val != null ? val.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Long getUserId(String username, SysUserMapper userMapper) {
        SysUser user = userMapper.selectByUsername(username);
        return user != null ? user.getUserId() : null;
    }

    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}
