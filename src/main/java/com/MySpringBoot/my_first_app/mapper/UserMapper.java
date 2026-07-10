package com.MySpringBoot.my_first_app.mapper;

import com.MySpringBoot.my_first_app.entity.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Integer id);

    @Insert("INSERT INTO users(username, password, nickname, credit_score, create_time) " +
            "VALUES(#{username}, #{password}, #{nickname}, 100, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE users SET nickname=#{nickname}, phone=#{phone}, avatar=#{avatar}, school_name=#{schoolName} WHERE id=#{id}")
    int update(User user);

    @Update("UPDATE users SET password=#{password} WHERE id=#{id}")
    int updatePassword(@Param("id") Integer id, @Param("password") String password);

    @Update("UPDATE users SET last_login_time=#{lastLoginTime}, last_login_ip=#{lastLoginIp} WHERE id=#{id}")
    int updateLoginInfo(@Param("id") Integer id, @Param("lastLoginTime") LocalDateTime lastLoginTime, @Param("lastLoginIp") String lastLoginIp);

    @Update("UPDATE users SET last_login_time=NOW() WHERE id=#{id}")
    int updateHeartbeat(Integer id);
}
