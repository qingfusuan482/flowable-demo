package org.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.common.PageResult;
import org.example.common.Result;
import org.example.model.dto.ProcessStartRequest;
import org.example.model.vo.ProcessDiagramVO;
import org.example.model.vo.ProcessInstanceVO;
import org.example.security.SecurityUtils;
import org.example.service.ProcessInstanceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/process-instances")
@RequiredArgsConstructor
public class ProcessInstanceController {

    private final ProcessInstanceService processInstanceService;

    /** 启动流程 */
    @PostMapping
    @PreAuthorize("hasAuthority('process:instance:start')")
    public Result<ProcessInstanceVO> start(@Valid @RequestBody ProcessStartRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        return Result.ok(processInstanceService.startProcess(username, request));
    }

    /** 流程实例列表 */
    @GetMapping
    @PreAuthorize("hasAuthority('process:instance:list')")
    public Result<PageResult<ProcessInstanceVO>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                       @RequestParam(defaultValue = "10") int pageSize,
                                                       @RequestParam(required = false) String keyword) {
        Page<ProcessInstanceVO> page = processInstanceService.page(pageNum, pageSize, keyword);
        return Result.ok(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    /** 流程实例详情 */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('process:instance:list')")
    public Result<ProcessInstanceVO> getById(@PathVariable String id) {
        return Result.ok(processInstanceService.getById(id));
    }

    /** 流程图追踪数据 */
    @GetMapping("/{id}/diagram")
    @PreAuthorize("hasAuthority('process:instance:list')")
    public Result<ProcessDiagramVO> diagram(@PathVariable String id) {
        return Result.ok(processInstanceService.getDiagram(id));
    }

    /** 流程变量 */
    @GetMapping("/{id}/variables")
    @PreAuthorize("hasAuthority('process:instance:list')")
    public Result<Map<String, Object>> variables(@PathVariable String id) {
        return Result.ok(processInstanceService.getVariables(id));
    }

    /** 终止流程 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('process:instance:delete')")
    public Result<Void> terminate(@PathVariable String id, @RequestParam(defaultValue = "手动终止") String reason) {
        processInstanceService.terminate(id, reason);
        return Result.ok();
    }
}
