package com.MySpringBoot.my_first_app.mapper;

import com.MySpringBoot.my_first_app.entity.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO orders(order_no, buyer_id, seller_id, book_id, total_price, status, create_time) " +
            "VALUES(#{orderNo}, #{buyerId}, #{sellerId}, #{bookId}, #{totalPrice}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Select("SELECT * FROM orders WHERE buyer_id = #{buyerId} ORDER BY create_time DESC")
    List<Order> findByBuyerId(Integer buyerId);

    @Select("SELECT * FROM orders WHERE seller_id = #{sellerId} ORDER BY create_time DESC")
    List<Order> findBySellerId(Integer sellerId);

    @Update("UPDATE orders SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    @Select("SELECT * FROM orders WHERE id = #{id}")
    Order findById(Integer id);

    @Update("UPDATE orders SET status=1, pay_time=NOW() WHERE id=#{id} AND status=0")
    int pay(Integer id);

    @Update("UPDATE orders SET status=2, ship_time=NOW() WHERE id=#{id} AND status=1")
    int ship(Integer id);

    @Update("UPDATE orders SET status=3, confirm_time=NOW() WHERE id=#{id} AND status=2")
    int confirm(Integer id);

    @Update("UPDATE orders SET status=-1 WHERE id=#{id} AND (status=0 OR status=1)")
    int cancel(Integer id);

    @Select("SELECT COUNT(*) FROM orders WHERE seller_id = #{sellerId} AND status = 3")
    int countSoldBySellerId(Integer sellerId);
}
