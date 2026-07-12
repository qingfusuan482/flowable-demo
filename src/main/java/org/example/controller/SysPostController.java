package org.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.common.PageResult;
import org.example.common.Result;
import org.example.model.entity.SysPost;
import org.example.service.SysPostService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class SysPostController {

    private final SysPostService postService;

    @GetMapping
    @PreAuthorize("hasAuthority('system:post:list')")
    public Result<PageResult<SysPost>> list(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) String keyword) {
        Page<SysPost> page = postService.page(pageNum, pageSize, keyword);
        return Result.ok(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    @GetMapping("/all")
    public Result<List<SysPost>> all() {
        return Result.ok(postService.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:post:list')")
    public Result<SysPost> getById(@PathVariable Long id) {
        return Result.ok(postService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:post:add')")
    public Result<Void> add(@RequestBody SysPost post) {
        postService.save(post);
        return Result.ok();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:post:edit')")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysPost post) {
        post.setPostId(id);
        postService.update(post);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:post:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return Result.ok();
    }
}
