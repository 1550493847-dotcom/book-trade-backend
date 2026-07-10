package com.MySpringBoot.my_first_app.mapper;

import com.MySpringBoot.my_first_app.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationMapper {

    @Select("SELECT * FROM notifications WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Notification> findByUserId(Integer userId);

    @Select("SELECT * FROM notifications WHERE user_id = #{userId} AND type = #{type} ORDER BY create_time DESC")
    List<Notification> findByUserIdAndType(@Param("userId") Integer userId, @Param("type") String type);

    @Update("UPDATE notifications SET is_read = 1 WHERE id = #{id}")
    int markRead(Integer id);

    @Insert("INSERT INTO notifications(user_id, type, title, content) VALUES(#{userId}, #{type}, #{title}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Notification notification);
}
