package org.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.common.PageResult;
import org.example.common.Result;
import org.example.model.entity.SysRole;
import org.example.service.SysRoleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<PageResult<SysRole>> list(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) String keyword) {
        Page<SysRole> page = roleService.page(pageNum, pageSize, keyword);
        return Result.ok(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @GetMapping("/all")
    public Result<List<SysRole>> all() {
        return Result.ok(roleService.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.ok(roleService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    public Result<Void> add(@RequestBody SysRole role) {
        roleService.save(role);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysRole role) {
        role.setRoleId(id);
        roleService.update(role);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.ok();
    }

    @GetMapping("/{id}/menus")
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<List<Long>> getMenus(@PathVariable Long id) {
        return Result.ok(roleService.getMenuIdsByRoleId(id));
    }

    @PutMapping("/{id}/menus")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return Result.ok();
    }
}
