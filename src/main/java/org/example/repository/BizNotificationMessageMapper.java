package org.example.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.model.entity.BizNotificationMessage;

@Mapper
public interface BizNotificationMessageMapper extends BaseMapper<BizNotificationMessage> {

    @Select("SELECT COUNT(*) FROM biz_notification_message WHERE username = #{username} AND is_read = 0 AND deleted = 0")
    long countUnread(@Param("username") String username);

    @Update("UPDATE biz_notification_message SET is_read = 1 WHERE id = #{id}")
    void markAsRead(@Param("id") Long id);
}
