package org.example.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.model.entity.BizNotificationMessage;
import org.example.repository.BizNotificationMessageMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final BizNotificationMessageMapper messageMapper;

    public Page<BizNotificationMessage> page(int pageNum, int pageSize, String username) {
        LambdaQueryWrapper<BizNotificationMessage> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isBlank()) {
            wrapper.eq(BizNotificationMessage::getUsername, username);
        }
        wrapper.orderByDesc(BizNotificationMessage::getCreateTime);
        return messageMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public long unreadCount(String username) {
        return messageMapper.countUnread(username);
    }

    public void markAsRead(Long id) {
        messageMapper.markAsRead(id);
    }
}
