package org.example.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.example.common.PageResult;
import org.example.common.Result;
import org.example.model.dto.UserRoleAssignDTO;
import org.example.model.entity.SysUser;
import org.example.service.SysUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<PageResult<SysUser>> list(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) String keyword) {
        IPage<SysUser> page = userService.page(pageNum, pageSize, keyword);
        return Result.ok(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    public Result<Void> add(@RequestBody SysUser user) {
        userService.save(user);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysUser user) {
        user.setUserId(id);
        userService.update(user);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return Result.ok();
    }
}
