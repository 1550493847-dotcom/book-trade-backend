package com.MySpringBoot.my_first_app.mapper;

import com.MySpringBoot.my_first_app.entity.Book;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BookMapper {

    @Insert("INSERT INTO books(user_id, title, author, isbn, publisher, book_condition, category, original_price, sell_price, " +
            "description, images, status, view_count, create_time) " +
            "VALUES(#{userId}, #{title}, #{author}, #{isbn}, #{publisher}, #{bookCondition}, #{category}, #{originalPrice}, #{sellPrice}, " +
            "#{description}, #{images}, 0, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Book book);

    @Select("SELECT * FROM books WHERE status = 0 ORDER BY create_time DESC")
    List<Book> findAll();

    @Select("SELECT * FROM books WHERE id = #{id}")
    Book findById(Integer id);

    @Select("SELECT * FROM books WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Book> findByUserId(Integer userId);

    @Update("UPDATE books SET status = 2 WHERE id = #{id}")
    int offShelf(Integer id);

    @Update("UPDATE books SET status = 0 WHERE id = #{id}")
    int onShelf(Integer id);

    @Update("UPDATE books SET status = 1 WHERE id = #{id}")
    int sellBook(Integer id);

    @Update("UPDATE books SET title=#{title}, author=#{author}, isbn=#{isbn}, publisher=#{publisher}, book_condition=#{bookCondition}, " +
            "category=#{category}, original_price=#{originalPrice}, sell_price=#{sellPrice}, description=#{description}, " +
            "images=#{images} WHERE id=#{id}")
    int update(Book book);

    @Update("UPDATE books SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(Integer id);

    @Update("UPDATE books SET view_count = #{viewCount} WHERE id = #{id}")
    int updateViewCountById(@Param("id") Integer id, @Param("viewCount") Integer viewCount);

    @Select("SELECT COUNT(*) FROM books WHERE user_id = #{userId} AND status = 1")
    int countSoldByUserId(Integer userId);

    @Delete("DELETE FROM books WHERE id = #{id}")
    int deleteById(Integer id);
}
