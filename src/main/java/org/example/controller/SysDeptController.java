package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.common.Result;
import org.example.model.entity.SysDept;
import org.example.service.SysDeptService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/depts")
@RequiredArgsConstructor
public class SysDeptController {

    private final SysDeptService deptService;

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:dept:list')")
    public Result<List<SysDept>> tree() {
        return Result.ok(deptService.treeList());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('system:dept:list')")
    public Result<List<SysDept>> list() {
        return Result.ok(deptService.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dept:list')")
    public Result<SysDept> getById(@PathVariable Long id) {
        return Result.ok(deptService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:dept:add')")
    public Result<Void> add(@RequestBody SysDept dept) {
        deptService.save(dept);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dept:edit')")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysDept dept) {
        dept.setDeptId(id);
        deptService.update(dept);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:dept:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return Result.ok();
    }
}
