package com.MySpringBoot.my_first_app.service;

import com.MySpringBoot.my_first_app.entity.Order;
import com.MySpringBoot.my_first_app.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    public Order createOrder(Integer buyerId, Integer sellerId, Integer bookId, Double totalPrice) {
        Order order = new Order();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        order.setBuyerId(buyerId);
        order.setSellerId(sellerId);
        order.setBookId(bookId);
        order.setTotalPrice(totalPrice);
        order.setStatus(0);

        int result = orderMapper.insert(order);
        if (result > 0) return order;
        return null;
    }

    public List<Order> getMyBuyOrders(Integer buyerId) {
        return orderMapper.findByBuyerId(buyerId);
    }

    public List<Order> getMySellOrders(Integer sellerId) {
        return orderMapper.findBySellerId(sellerId);
    }

    public Order getOrderById(Integer id) {
        return orderMapper.findById(id);
    }

    public boolean pay(Integer id) {
        return orderMapper.pay(id) > 0;
    }

    public boolean ship(Integer id) {
        return orderMapper.ship(id) > 0;
    }

    public boolean confirm(Integer id) {
        return orderMapper.confirm(id) > 0;
    }

    public boolean cancel(Integer id) {
        return orderMapper.cancel(id) > 0;
    }
}
