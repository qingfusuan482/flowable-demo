package org.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.BusinessException;
import org.example.model.dto.ProcessStartRequest;
import org.example.model.entity.*;
import org.example.model.vo.ProcessDiagramVO;
import org.example.model.vo.ProcessInstanceVO;
import org.example.repository.*;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessInstanceService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final RepositoryService repositoryService;
    private final HistoryService historyService;
    private final ManagementService managementService;

    private final FlowWorkOrderMapper workOrderMapper;
    private final BizLeaveMapper leaveMapper;
    private final BizContractMapper contractMapper;
    private final FlowDefinitionMapper definitionMapper;
    private final SysUserMapper userMapper;

    // ==================== 启动流程 ====================

    /**
     * 启动流程实例
     */
    @Transactional
    public ProcessInstanceVO startProcess(String submitUsername, ProcessStartRequest request) {
        String processKey = request.getProcessDefinitionKey();

        // 1. 查询最新已发布的流程定义
        ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .latestVersion()
                .singleResult();
        if (procDef == null) {
            throw new BusinessException("流程定义不存在或未发布: " + processKey);
        }

        // 2. 准备流程变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("starter", submitUsername);
        if (request.getVariables() != null) {
            variables.putAll(request.getVariables());
        }

        // 3. 创建业务记录 + 设置 businessKey
        String businessKey = null;
        Map<String, Object> formData = request.getFormData();
        if (formData != null) {
            if ("leave-process".equals(processKey)) {
                BizLeave leave = buildLeave(submitUsername, formData);
                leave.setFlowStatus("APPROVING");
                leaveMapper.insert(leave);
                businessKey = "BIZ_LEAVE_" + leave.getId();
                leave.setBusinessKey(businessKey);
                leaveMapper.updateById(leave);
                // 注入业务变量
                variables.put("days", leave.getDays());
                variables.put("leaveType", leave.getLeaveType());
            } else if ("contract-process".equals(processKey)) {
                BizContract contract = buildContract(submitUsername, formData);
                contract.setFlowStatus("APPROVING");
                contractMapper.insert(contract);
                businessKey = "BIZ_CONTRACT_" + contract.getId();
                contract.setBusinessKey(businessKey);
                contractMapper.updateById(contract);
                // 注入审批人变量
                if (contract.getPartyAName() != null) {
                    variables.put("partyA", contract.getPartyAName());
                }
                if (contract.getPartyBName() != null) {
                    variables.put("partyB", contract.getPartyBName());
                }
                if (contract.getDesignatedApproverName() != null) {
                    variables.put("designatedApprover", contract.getDesignatedApproverName());
                }
            }
        }

        // 4. 启动流程实例
        ProcessInstance pi = runtimeService.startProcessInstanceById(
                procDef.getId(), businessKey, variables);
        log.info("流程启动成功: instanceId={}, businessKey={}, starter={}", pi.getId(), businessKey, submitUsername);

        // 5. 创建工单
        FlowWorkOrder order = new FlowWorkOrder();
        order.setProcessDefinitionKey(processKey);
        order.setProcessDefinitionName(procDef.getName());
        order.setProcessDefinitionId(procDef.getId());
        order.setProcessInstanceId(pi.getId());
        order.setBusinessKey(pi.getBusinessKey());
        order.setFlowStatus("RUNNING");
        order.setSubmitUsername(submitUsername);

        // 查当前任务
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(pi.getId()).list();
        if (!tasks.isEmpty()) {
            Task currentTask = tasks.get(0);
            order.setCurrentTaskId(currentTask.getId());
            order.setCurrentTaskName(currentTask.getName());
            order.setCurrentTaskDefinitionKey(currentTask.getTaskDefinitionKey());
        }
        workOrderMapper.insert(order);

        // 6. 更新业务表的 processInstanceId
        updateBizProcessInstanceId(businessKey, processKey, pi.getId());

        // 7. 返回 VO
        return ProcessInstanceVO.builder()
                .processInstanceId(pi.getId())
                .processDefinitionId(procDef.getId())
                .processDefinitionKey(processKey)
                .processDefinitionName(procDef.getName())
                .processDefinitionVersion(procDef.getVersion())
                .businessKey(pi.getBusinessKey())
                .startUserId(submitUsername)
                .startTime(pi.getStartTime() != null ?
                        LocalDateTime.ofInstant(pi.getStartTime().toInstant(), ZoneId.systemDefault()) : null)
                .flowStatus("RUNNING")
                .currentTaskName(order.getCurrentTaskName())
                .build();
    }

    // ==================== 查询 ====================

    /** 分页查询流程实例列表 */
    public Page<ProcessInstanceVO> page(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<FlowWorkOrder> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(FlowWorkOrder::getProcessDefinitionName, keyword)
                    .or().like(FlowWorkOrder::getSubmitUsername, keyword));
        }
        wrapper.orderByDesc(FlowWorkOrder::getCreateTime);
        Page<FlowWorkOrder> orderPage = workOrderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        return (Page<ProcessInstanceVO>) orderPage.convert(this::toVO);
    }

    /** 查询详情 */
    public ProcessInstanceVO getById(String processInstanceId) {
        FlowWorkOrder order = workOrderMapper.selectOne(
                new LambdaQueryWrapper<FlowWorkOrder>().eq(FlowWorkOrder::getProcessInstanceId, processInstanceId));
        if (order == null) {
            throw new BusinessException("流程实例不存在: " + processInstanceId);
        }
        return toVO(order);
    }

    /** 获取流程图追踪数据 */
    public ProcessDiagramVO getDiagram(String processInstanceId) {
        // 查询历史流程实例获取 processDefinitionId
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        if (hpi == null) {
            throw new BusinessException("流程实例不存在");
        }

        // 获取 BPMN XML
        String bpmnXml = "";
        try {
            java.io.InputStream is = repositoryService.getProcessModel(hpi.getProcessDefinitionId());
            if (is != null) {
                bpmnXml = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.warn("读取 BPMN XML 失败: {}", e.getMessage());
        }
        if (bpmnXml.isEmpty()) {
            // 从我们的 flow_definition_version 表中获取
            bpmnXml = "";
        }

        // 当前活跃任务
        List<String> activeTaskIds = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list().stream().map(Task::getTaskDefinitionKey).collect(Collectors.toList());

        // 已完成任务
        List<String> finishedTaskIds = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .finished()
                .list().stream().map(HistoricTaskInstance::getTaskDefinitionKey)
                .distinct().collect(Collectors.toList());

        return ProcessDiagramVO.builder()
                .bpmnXml(bpmnXml)
                .activeTaskIds(activeTaskIds)
                .finishedTaskIds(finishedTaskIds)
                .build();
    }

    /** 获取流程变量 */
    public Map<String, Object> getVariables(String processInstanceId) {
        return runtimeService.getVariables(processInstanceId);
    }

    // ==================== 终止 ====================

    @Transactional
    public void terminate(String processInstanceId, String reason) {
        runtimeService.deleteProcessInstance(processInstanceId, reason);
        // 更新工单状态
        FlowWorkOrder order = workOrderMapper.selectOne(
                new LambdaQueryWrapper<FlowWorkOrder>().eq(FlowWorkOrder::getProcessInstanceId, processInstanceId));
        if (order != null) {
            order.setFlowStatus("TERMINATED");
            workOrderMapper.updateById(order);
        }
        // 更新业务表状态
        updateBizStatus(processInstanceId, "TERMINATED");
    }

    // ==================== 辅助方法 ====================

    private ProcessInstanceVO toVO(FlowWorkOrder order) {
        ProcessInstanceVO.ProcessInstanceVOBuilder builder = ProcessInstanceVO.builder()
                .processInstanceId(order.getProcessInstanceId())
                .processDefinitionId(order.getProcessDefinitionId())
                .processDefinitionKey(order.getProcessDefinitionKey())
                .processDefinitionName(order.getProcessDefinitionName())
                .businessKey(order.getBusinessKey())
                .startUserId(order.getSubmitUsername())
                .startTime(order.getCreateTime())
                .flowStatus(order.getFlowStatus())
                .currentTaskName(order.getCurrentTaskName());

        // 查 Flowable 运行时数据
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(order.getProcessInstanceId()).singleResult();
        if (pi != null) {
            builder.processVariables(runtimeService.getVariables(order.getProcessInstanceId()));
        } else {
            // 已完成的查历史
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(order.getProcessInstanceId()).singleResult();
            if (hpi != null) {
                builder.endTime(hpi.getEndTime() != null ?
                        LocalDateTime.ofInstant(hpi.getEndTime().toInstant(), ZoneId.systemDefault()) : null);
            }
        }

        // 查版本号
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(order.getProcessDefinitionId()).singleResult();
        if (pd != null) {
            builder.processDefinitionVersion(pd.getVersion());
        }

        return builder.build();
    }

    private BizLeave buildLeave(String username, Map<String, Object> formData) {
        BizLeave leave = new BizLeave();
        // 查用户信息
        SysUser user = userMapper.selectByUsername(username);
        leave.setApplicantId(user != null ? user.getUserId() : null);
        leave.setApplicantName(username);

        if (formData.containsKey("leaveType")) {
            leave.setLeaveType((String) formData.get("leaveType"));
        }
        if (formData.containsKey("reason")) {
            leave.setReason((String) formData.get("reason"));
        }
        if (formData.containsKey("startDate")) {
            leave.setStartDate(LocalDate.parse((String) formData.get("startDate")));
        }
        if (formData.containsKey("endDate")) {
            leave.setEndDate(LocalDate.parse((String) formData.get("endDate")));
        }
        // 计算天数
        if (leave.getStartDate() != null && leave.getEndDate() != null) {
            leave.setDays((int) (leave.getEndDate().toEpochDay() - leave.getStartDate().toEpochDay()) + 1);
        }
        return leave;
    }

    private BizContract buildContract(String username, Map<String, Object> formData) {
        BizContract contract = new BizContract();
        SysUser user = userMapper.selectByUsername(username);
        contract.setApplicantId(user != null ? user.getUserId() : null);
        contract.setApplicantName(username);

        if (formData.containsKey("contractName")) {
            contract.setContractName((String) formData.get("contractName"));
        }
        if (formData.containsKey("content")) {
            contract.setContent((String) formData.get("content"));
        }
        if (formData.containsKey("amount")) {
            contract.setAmount(new java.math.BigDecimal(formData.get("amount").toString()));
        }
        if (formData.containsKey("signDate")) {
            contract.setSignDate(LocalDate.parse((String) formData.get("signDate")));
        }
        // 审批人：从 formData 读取用户名
        if (formData.containsKey("partyAName")) contract.setPartyAName((String) formData.get("partyAName"));
        if (formData.containsKey("partyBName")) contract.setPartyBName((String) formData.get("partyBName"));
        if (formData.containsKey("designatedApproverName")) contract.setDesignatedApproverName((String) formData.get("designatedApproverName"));
        // ID 也试着设置
        if (formData.containsKey("partyAId")) contract.setPartyAId(Long.valueOf(formData.get("partyAId").toString()));
        if (formData.containsKey("partyBId")) contract.setPartyBId(Long.valueOf(formData.get("partyBId").toString()));
        if (formData.containsKey("designatedApproverId")) contract.setDesignatedApproverId(Long.valueOf(formData.get("designatedApproverId").toString()));

        return contract;
    }

    private void updateBizProcessInstanceId(String businessKey, String processKey, String piId) {
        if (businessKey == null) return;
        if ("leave-process".equals(processKey)) {
            BizLeave leave = leaveMapper.selectOne(
                    new LambdaQueryWrapper<BizLeave>().eq(BizLeave::getBusinessKey, businessKey));
            if (leave != null) {
                leave.setProcessInstanceId(piId);
                leaveMapper.updateById(leave);
            }
        } else if ("contract-process".equals(processKey)) {
            BizContract contract = contractMapper.selectOne(
                    new LambdaQueryWrapper<BizContract>().eq(BizContract::getBusinessKey, businessKey));
            if (contract != null) {
                contract.setProcessInstanceId(piId);
                contractMapper.updateById(contract);
            }
        }
    }

    private void updateBizStatus(String piId, String status) {
        BizLeave leave = leaveMapper.selectOne(
                new LambdaQueryWrapper<BizLeave>().eq(BizLeave::getProcessInstanceId, piId));
        if (leave != null) {
            leave.setFlowStatus(status);
            leaveMapper.updateById(leave);
            return;
        }
        BizContract contract = contractMapper.selectOne(
                new LambdaQueryWrapper<BizContract>().eq(BizContract::getProcessInstanceId, piId));
        if (contract != null) {
            contract.setFlowStatus(status);
            contractMapper.updateById(contract);
        }
    }
}
