package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.common.Result;
import org.example.model.entity.BizApprovalRecord;
import org.example.security.SecurityUtils;
import org.example.service.FlowableTaskService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final FlowableTaskService taskService;

    /** 待办任务 */
    @GetMapping("/todo")
    @PreAuthorize("hasAuthority('task:todo:list')")
    public Result<List<Map<String, Object>>> todo() {
        String username = SecurityUtils.getCurrentUsername();
        return Result.ok(taskService.todoList(username));
    }

    /** 已办任务 */
    @GetMapping("/done")
    @PreAuthorize("hasAuthority('task:done:list')")
    public Result<List<Map<String, Object>>> done() {
        String username = SecurityUtils.getCurrentUsername();
        return Result.ok(taskService.doneList(username));
    }

    /** 完成任务 */
    @PostMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable String id,
                                  @RequestParam(defaultValue = "APPROVE") String approvalType,
                                  @RequestParam(defaultValue = "") String comment) {
        String username = SecurityUtils.getCurrentUsername();
        taskService.completeTask(id, username, approvalType, comment);
        return Result.ok();
    }

    /** 转办 */
    @PostMapping("/{id}/delegate")
    public Result<Void> delegate(@PathVariable String id, @RequestParam String toUser) {
        String username = SecurityUtils.getCurrentUsername();
        taskService.delegateTask(id, username, toUser);
        return Result.ok();
    }

    /** 审批留痕 */
    @GetMapping("/history/{processInstanceId}")
    public Result<List<BizApprovalRecord>> history(@PathVariable String processInstanceId) {
        return Result.ok(taskService.getHistory(processInstanceId));
    }
}
