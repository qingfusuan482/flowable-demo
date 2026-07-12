package org.example.listener;

import org.example.model.entity.BizNotificationMessage;
import org.example.model.entity.FlowWorkOrder;
import org.example.model.entity.SysUser;
import org.example.repository.BizNotificationMessageMapper;
import org.example.repository.FlowWorkOrderMapper;
import org.example.repository.SysUserMapper;
import org.example.util.SpringContextHolder;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;

/**
 * 任务创建监听器：生成待办消息 + 更新工单当前任务
 * 所有依赖通过 SpringContextHolder 延迟获取，避免与 Flowable 引擎的循环依赖
 */
@Component
public class GlobalTaskCreateListener implements FlowableEventListener {

    @Override
    public void onEvent(FlowableEvent event) {
        if (!FlowableEngineEventType.TASK_CREATED.equals(event.getType())) return;

        FlowableEngineEntityEvent entityEvent = (FlowableEngineEntityEvent) event;
        TaskEntity task = (TaskEntity) entityEvent.getEntity();

        String assignee = task.getAssignee();
        if (assignee == null || assignee.isEmpty()) return;

        SysUserMapper userMapper = SpringContextHolder.getBean(SysUserMapper.class);
        BizNotificationMessageMapper messageMapper = SpringContextHolder.getBean(BizNotificationMessageMapper.class);
        FlowWorkOrderMapper workOrderMapper = SpringContextHolder.getBean(FlowWorkOrderMapper.class);

        Long userId = getUserId(assignee, userMapper);

        // 1. 插入待办消息
        BizNotificationMessage msg = new BizNotificationMessage();
        msg.setUserId(userId);
        msg.setUsername(assignee);
        msg.setMessageType("TODO");
        msg.setTitle("新的待办任务");
        msg.setContent(String.format("您有一条待办任务：%s，来自流程实例：%s",
                task.getName(), task.getProcessInstanceId()));
        msg.setProcessInstanceId(task.getProcessInstanceId());
        msg.setTaskId(task.getId());
        msg.setTaskName(task.getName());
        msg.setIsRead(0);
        msg.setCreateTime(LocalDateTime.now());
        messageMapper.insert(msg);

        // 2. 更新工单当前任务信息
        FlowWorkOrder order = workOrderMapper.selectOne(
                new LambdaQueryWrapper<FlowWorkOrder>().eq(FlowWorkOrder::getProcessInstanceId, task.getProcessInstanceId()));
        if (order != null) {
            order.setCurrentTaskId(task.getId());
            order.setCurrentTaskName(task.getName());
            order.setCurrentTaskDefinitionKey(task.getTaskDefinitionKey());
            workOrderMapper.updateById(order);
        }
    }

    @Override
    public boolean isFailOnException() { return false; }

    @Override
    public String getOnTransaction() { return "committed"; }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() { return false; }

    private Long getUserId(String username, SysUserMapper userMapper) {
        SysUser user = userMapper.selectByUsername(username);
        return user != null ? user.getUserId() : null;
    }
}
