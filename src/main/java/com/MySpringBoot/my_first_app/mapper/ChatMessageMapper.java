package com.MySpringBoot.my_first_app.mapper;

import com.MySpringBoot.my_first_app.entity.ChatMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChatMessageMapper {

    @Insert("INSERT INTO chat_messages(from_user_id, to_user_id, content) VALUES(#{fromUserId}, #{toUserId}, #{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChatMessage message);

    @Select("SELECT * FROM chat_messages WHERE (from_user_id=#{userId} AND to_user_id=#{otherId}) " +
            "OR (from_user_id=#{otherId} AND to_user_id=#{userId}) ORDER BY create_time ASC")
    List<ChatMessage> findConversation(@Param("userId") Integer userId, @Param("otherId") Integer otherId);

    @Select("SELECT m.* FROM chat_messages m WHERE m.from_user_id=#{userId} OR m.to_user_id=#{userId} " +
            "ORDER BY m.create_time DESC")
    List<ChatMessage> findByUserId(Integer userId);
}
