package org.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.common.PageResult;
import org.example.common.Result;
import org.example.model.entity.FlowDefinition;
import org.example.model.entity.FlowDefinitionVersion;
import org.example.security.SecurityUtils;
import org.example.service.ProcessDefinitionService;
import org.example.service.SysUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/process-definitions")
@RequiredArgsConstructor
public class ProcessDefinitionController {

    private final ProcessDefinitionService definitionService;
    private final SysUserService sysUserService;

    @GetMapping
    @PreAuthorize("hasAuthority('process:definition:list')")
    public Result<PageResult<FlowDefinition>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "10") int pageSize,
                                                    @RequestParam(required = false) String keyword) {
        Page<FlowDefinition> page = definitionService.page(pageNum, pageSize, keyword);
        return Result.ok(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('process:definition:list')")
    public Result<FlowDefinition> getById(@PathVariable Long id) {
        return Result.ok(definitionService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('process:definition:list')")
    public Result<FlowDefinition> create(@RequestBody FlowDefinition def) {
        return Result.ok(definitionService.create(def));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('process:definition:list')")
    public Result<Void> update(@PathVariable Long id, @RequestBody FlowDefinition def) {
        definitionService.update(id, def);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('process:definition:list')")
    public Result<Void> delete(@PathVariable Long id) {
        definitionService.delete(id);
        return Result.ok();
    }

    /** 发布新版本 */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('process:definition:publish')")
    public Result<FlowDefinitionVersion> publish(@PathVariable Long id) {
        // 从 token 获取当前用户 ID（简化：通过用户名查）
        String username = SecurityUtils.getCurrentUsername();
        var user = sysUserService.getById(1L); // 实际项目需查当前用户
        return Result.ok(definitionService.publish(id, 1L));
    }

    /** 获取版本历史 */
    @GetMapping("/{id}/versions")
    @PreAuthorize("hasAuthority('process:definition:list')")
    public Result<List<FlowDefinitionVersion>> versions(@PathVariable Long id) {
        return Result.ok(definitionService.getVersions(id));
    }

    /** 挂起 */
    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasAuthority('process:definition:suspend')")
    public Result<Void> suspend(@PathVariable Long id) {
        definitionService.suspend(id);
        return Result.ok();
    }

    /** 激活 */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('process:definition:suspend')")
    public Result<Void> activate(@PathVariable Long id) {
        definitionService.activate(id);
        return Result.ok();
    }

    /**
     * 获取 BPMN XML 模板（用于新建流程时填充默认 BPMN）
     * 返回所有可用的预置模板列表
     */
    @GetMapping("/templates")
    @PreAuthorize("hasAuthority('process:definition:list')")
    public Result<List<Map<String, String>>> templates() {
        return Result.ok(List.of(
                Map.of("key", "leave-process", "name", "请假流程"),
                Map.of("key", "contract-process", "name", "合同签订流程")
        ));
    }

    /**
     * 获取指定模板的 BPMN XML 内容
     */
    @GetMapping("/templates/{key}")
    @PreAuthorize("hasAuthority('process:definition:list')")
    public Result<String> getTemplate(@PathVariable String key) {
        try {
            var is = getClass().getClassLoader().getResourceAsStream("bpmn/" + key + ".bpmn20.xml");
            if (is == null) {
                return Result.fail("模板不存在: " + key);
            }
            String xml = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return Result.ok(xml);
        } catch (Exception e) {
            return Result.fail("读取模板失败: " + e.getMessage());
        }
    }
}
