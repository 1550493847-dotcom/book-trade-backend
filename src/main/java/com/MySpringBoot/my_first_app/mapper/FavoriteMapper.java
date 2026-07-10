package com.MySpringBoot.my_first_app.mapper;

import com.MySpringBoot.my_first_app.entity.Favorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    @Insert("INSERT INTO favorites(user_id, book_id) VALUES(#{userId}, #{bookId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Favorite favorite);

    @Delete("DELETE FROM favorites WHERE id = #{id}")
    int deleteById(Integer id);

    @Delete("DELETE FROM favorites WHERE user_id = #{userId} AND book_id = #{bookId}")
    int deleteByUserAndBook(@Param("userId") Integer userId, @Param("bookId") Integer bookId);

    @Select("SELECT f.*, b.title, b.author, b.sell_price AS sellPrice, b.images " +
            "FROM favorites f LEFT JOIN books b ON f.book_id = b.id " +
            "WHERE f.user_id = #{userId} ORDER BY f.create_time DESC")
    List<Favorite> findByUserId(Integer userId);

    @Select("SELECT COUNT(*) FROM favorites WHERE user_id = #{userId} AND book_id = #{bookId}")
    int checkExists(@Param("userId") Integer userId, @Param("bookId") Integer bookId);
}
