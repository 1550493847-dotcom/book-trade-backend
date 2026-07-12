package com.MySpringBoot.my_first_app.controler;

import com.MySpringBoot.my_first_app.entity.Book;
import com.MySpringBoot.my_first_app.entity.Order;
import com.MySpringBoot.my_first_app.entity.User;
import com.MySpringBoot.my_first_app.service.BookService;
import com.MySpringBoot.my_first_app.service.OrderService;
import com.MySpringBoot.my_first_app.service.UserService;
import com.MySpringBoot.my_first_app.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

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

    /** 为订单列表填充书籍 + 用户信息 */
    private List<Map<String, Object>> enrichOrders(List<Order> orders) {
        return orders.stream().map(order -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", order.getId());
            item.put("orderNo", order.getOrderNo());
            item.put("buyerId", order.getBuyerId());
            item.put("sellerId", order.getSellerId());
            item.put("bookId", order.getBookId());
            item.put("totalPrice", order.getTotalPrice());
            item.put("status", order.getStatus());
            item.put("createTime", order.getCreateTime());
            item.put("payTime", order.getPayTime());
            item.put("shipTime", order.getShipTime());
            item.put("confirmTime", order.getConfirmTime());

            // 填充书籍信息
            Book book = bookService.getBookById(order.getBookId());
            if (book != null) {
                item.put("bookTitle", book.getTitle());
                item.put("bookImage", book.getImages());
                item.put("bookAuthor", book.getAuthor());
                item.put("bookDescription", book.getDescription());
            }

            // 填充卖家信息
            User seller = userService.getUserById(order.getSellerId());
            if (seller != null) {
                Map<String, Object> sellerInfo = new HashMap<>();
                sellerInfo.put("id", seller.getId());
                sellerInfo.put("username", seller.getUsername());
                sellerInfo.put("nickname", seller.getNickname());
                sellerInfo.put("avatar", seller.getAvatar());
                item.put("seller", sellerInfo);
            }

            // 填充买家信息
            User buyer = userService.getUserById(order.getBuyerId());
            if (buyer != null) {
                Map<String, Object> buyerInfo = new HashMap<>();
                buyerInfo.put("id", buyer.getId());
                buyerInfo.put("username", buyer.getUsername());
                buyerInfo.put("nickname", buyer.getNickname());
                buyerInfo.put("avatar", buyer.getAvatar());
                item.put("buyer", buyerInfo);
            }

            return item;
        }).collect(Collectors.toList());
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
        response.put("data", enrichOrders(orders));
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
        response.put("data", enrichOrders(orders));
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
        response.put("data", enrichOrders(Collections.singletonList(order)).get(0));
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
        orderService.confirm(id);
        response.put("code", 200);
        response.put("message", "评价成功");
        return response;
    }
}