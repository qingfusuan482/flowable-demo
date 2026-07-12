package org.example.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.common.PageResult;
import org.example.common.Result;
import org.example.model.entity.BizNotificationMessage;
import org.example.security.SecurityUtils;
import org.example.service.MessageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /** 消息列表 */
    @GetMapping
    @PreAuthorize("hasAuthority('message:list')")
    public Result<PageResult<BizNotificationMessage>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                            @RequestParam(defaultValue = "10") int pageSize) {
        String username = SecurityUtils.getCurrentUsername();
        Page<BizNotificationMessage> page = messageService.page(pageNum, pageSize, username);
        return Result.ok(PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords()));
    }

    /** 未读消息数 */
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        String username = SecurityUtils.getCurrentUsername();
        return Result.ok(Map.of("unreadCount", messageService.unreadCount(username)));
    }

    /** 标记已读 */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return Result.ok();
    }
}
