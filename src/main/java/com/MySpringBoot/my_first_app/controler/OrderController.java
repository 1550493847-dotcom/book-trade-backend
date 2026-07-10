package com.MySpringBoot.my_first_app.controler;

import com.MySpringBoot.my_first_app.entity.Book;
import com.MySpringBoot.my_first_app.entity.Order;
import com.MySpringBoot.my_first_app.service.BookService;
import com.MySpringBoot.my_first_app.service.OrderService;
import com.MySpringBoot.my_first_app.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private BookService bookService;

    @Autowired
    private JwtUtil jwtUtil;

    private Integer getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        return null;
    }

    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestBody Map<String, Integer> params, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer buyerId = getUserIdFromRequest(request);
        if (buyerId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        Integer bookId = params.get("bookId");
        Book book = bookService.getBookById(bookId);
        if (book == null || book.getStatus() != 0) {
            response.put("code", 400);
            response.put("message", "商品已下架或不存在");
            return response;
        }
        if (book.getUserId().equals(buyerId)) {
            response.put("code", 400);
            response.put("message", "不能购买自己发布的商品");
            return response;
        }
        Order order = orderService.createOrder(buyerId, book.getUserId(), bookId, book.getSellPrice());
        if (order != null) {
            bookService.offShelf(bookId);
            response.put("code", 200);
            response.put("message", "订单创建成功");
            response.put("data", order);
        } else {
            response.put("code", 500);
            response.put("message", "创建失败");
        }
        return response;
    }

    @GetMapping("/my/buy")
    public Map<String, Object> getMyBuyOrders(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer buyerId = getUserIdFromRequest(request);
        if (buyerId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        List<Order> orders = orderService.getMyBuyOrders(buyerId);
        response.put("code", 200);
        response.put("data", orders);
        return response;
    }

    @GetMapping("/my/sell")
    public Map<String, Object> getMySellOrders(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer sellerId = getUserIdFromRequest(request);
        if (sellerId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        List<Order> orders = orderService.getMySellOrders(sellerId);
        response.put("code", 200);
        response.put("data", orders);
        return response;
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Integer id, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        Order order = orderService.getOrderById(id);
        if (order == null) {
            response.put("code", 404);
            response.put("message", "订单不存在");
            return response;
        }
        response.put("code", 200);
        response.put("data", order);
        return response;
    }

    @PutMapping("/{id}/pay")
    public Map<String, Object> pay(@PathVariable Integer id, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        response.put("code", orderService.pay(id) ? 200 : 400);
        response.put("message", orderService.pay(id) ? "付款成功" : "付款失败");
        return response;
    }

    @PutMapping("/{id}/ship")
    public Map<String, Object> ship(@PathVariable Integer id, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        response.put("code", orderService.ship(id) ? 200 : 400);
        response.put("message", orderService.ship(id) ? "发货成功" : "发货失败");
        return response;
    }

    @PutMapping("/{id}/confirm")
    public Map<String, Object> confirm(@PathVariable Integer id, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        boolean ok = orderService.confirm(id);
        if (ok) {
            // 确认收货后将该商品标记为已售出
            Order order = orderService.getOrderById(id);
            if (order != null) {
                bookService.sellBook(order.getBookId());
            }
        }
        response.put("code", ok ? 200 : 400);
        response.put("message", ok ? "确认收货成功" : "确认收货失败");
        return response;
    }

    @PutMapping("/{id}/cancel")
    public Map<String, Object> cancel(@PathVariable Integer id, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        response.put("code", orderService.cancel(id) ? 200 : 400);
        response.put("message", orderService.cancel(id) ? "取消成功" : "取消失败");
        return response;
    }

    @PostMapping("/{id}/review")
    public Map<String, Object> review(@PathVariable Integer id, @RequestBody Map<String, String> params, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        // 简单处理：订单状态改为4（已评价）
        orderService.confirm(id);
        response.put("code", 200);
        response.put("message", "评价成功");
        return response;
    }
}
