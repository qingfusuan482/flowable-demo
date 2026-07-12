package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.common.Result;
import org.example.model.entity.SysMenu;
import org.example.service.SysMenuService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<List<SysMenu>> tree() {
        return Result.ok(menuService.treeList());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<List<SysMenu>> list() {
        return Result.ok(menuService.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:list')")
    public Result<SysMenu> getById(@PathVariable Long id) {
        return Result.ok(menuService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:menu:add')")
    public Result<Void> add(@RequestBody SysMenu menu) {
        menuService.save(menu);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:edit')")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysMenu menu) {
        menu.setMenuId(id);
        menuService.update(menu);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:menu:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.ok();
    }
}
