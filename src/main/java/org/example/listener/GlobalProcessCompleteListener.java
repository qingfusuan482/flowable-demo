package org.example.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.model.entity.*;
import org.example.repository.*;
import org.example.util.SpringContextHolder;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.event.impl.FlowableEntityEventImpl;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 流程完成监听器：更新业务状态 + 发送流程完成通知
 */
@Component
public class GlobalProcessCompleteListener implements FlowableEventListener {

    @Override
    public void onEvent(FlowableEvent event) {
        if (!FlowableEngineEventType.PROCESS_COMPLETED.equals(event.getType())) return;

        FlowableEntityEventImpl entityEvent = (FlowableEntityEventImpl) event;
        ExecutionEntityImpl execution = (ExecutionEntityImpl) entityEvent.getEntity();
        String piId = execution.getProcessInstanceId();
        if (piId == null) return;

        FlowWorkOrderMapper workOrderMapper = SpringContextHolder.getBean(FlowWorkOrderMapper.class);
        BizLeaveMapper leaveMapper = SpringContextHolder.getBean(BizLeaveMapper.class);
        BizContractMapper contractMapper = SpringContextHolder.getBean(BizContractMapper.class);
        BizNotificationMessageMapper messageMapper = SpringContextHolder.getBean(BizNotificationMessageMapper.class);
        RuntimeService runtimeService = SpringContextHolder.getBean(RuntimeService.class);
        SysUserMapper userMapper = SpringContextHolder.getBean(SysUserMapper.class);

        // 1. 更新工单状态
        FlowWorkOrder order = workOrderMapper.selectOne(
                new LambdaQueryWrapper<FlowWorkOrder>().eq(FlowWorkOrder::getProcessInstanceId, piId));
        if (order != null) {
            order.setFlowStatus("COMPLETED");
            order.setCurrentTaskName(null);
            order.setCurrentTaskId(null);
            workOrderMapper.updateById(order);
        }

        // 2. 更新业务表状态
        BizLeave leave = leaveMapper.selectOne(
                new LambdaQueryWrapper<BizLeave>().eq(BizLeave::getProcessInstanceId, piId));
        if (leave != null) {
            leave.setFlowStatus("APPROVED");
            leaveMapper.updateById(leave);
        } else {
            BizContract contract = contractMapper.selectOne(
                    new LambdaQueryWrapper<BizContract>().eq(BizContract::getProcessInstanceId, piId));
            if (contract != null) {
                contract.setFlowStatus("APPROVED");
                contractMapper.updateById(contract);
            }
        }

        // 3. 发送流程完成通知给发起人
        String starter = (String) runtimeService.getVariable(piId, "starter");
        if (starter != null) {
            SysUser user = userMapper.selectByUsername(starter);
            BizNotificationMessage msg = new BizNotificationMessage();
            msg.setUserId(user != null ? user.getUserId() : null);
            msg.setUsername(starter);
            msg.setMessageType("NOTIFY");
            msg.setTitle("流程已完成");
            msg.setContent("您的流程申请已全部审批通过");
            msg.setProcessInstanceId(piId);
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
}
