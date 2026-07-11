package com.MySpringBoot.my_first_app.controler;

import com.MySpringBoot.my_first_app.entity.Book;
import com.MySpringBoot.my_first_app.entity.User;
import com.MySpringBoot.my_first_app.mapper.BookMapper;
import com.MySpringBoot.my_first_app.mapper.OrderMapper;
import com.MySpringBoot.my_first_app.service.BookService;
import com.MySpringBoot.my_first_app.service.UserService;
import com.MySpringBoot.my_first_app.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/book")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private BookMapper bookMapper;

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

    @PostMapping("/publish")
    public Map<String, Object> publish(@RequestBody Book book, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        book.setUserId(userId);
        book.setStatus(0);
        book.setViewCount(0);
        boolean success = bookService.publish(book);
        if (success) {
            response.put("code", 200);
            response.put("message", "发布成功");
            response.put("data", book.getId());
        } else {
            response.put("code", 500);
            response.put("message", "发布失败");
        }
        return response;
    }

    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sortBy) {
        Map<String, Object> response = new HashMap<>();
        List<Book> books;
        if (keyword != null || category != null || sortBy != null) {
            books = bookService.searchBooks(keyword, category, sortBy);
        } else {
            books = bookService.getAllBooks();
        }
        response.put("code", 200);
        response.put("data", books);
        return response;
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        Book book = bookService.getBookById(id);
        if (book != null) {
            // 使用 Redis 计数
            bookService.incrementViewCount(id);
            int viewCount = bookService.getViewCount(id);
            book.setViewCount(viewCount);

            Map<String, Object> data = new HashMap<>();
            data.put("id", book.getId());
            data.put("userId", book.getUserId());
            data.put("title", book.getTitle());
            data.put("author", book.getAuthor());
            data.put("isbn", book.getIsbn());
            data.put("publisher", book.getPublisher());
            data.put("bookCondition", book.getBookCondition());
            data.put("category", book.getCategory());
            data.put("originalPrice", book.getOriginalPrice());
            data.put("sellPrice", book.getSellPrice());
            data.put("description", book.getDescription());
            data.put("images", book.getImages());
            data.put("status", book.getStatus());
            data.put("viewCount", book.getViewCount());
            data.put("createTime", book.getCreateTime());

            // 添加卖家信息
            User seller = userService.getUserById(book.getUserId());
            if (seller != null) {
                Map<String, Object> sellerInfo = new HashMap<>();
                sellerInfo.put("id", seller.getId());
                sellerInfo.put("username", seller.getUsername());
                sellerInfo.put("nickname", seller.getNickname());
                sellerInfo.put("avatar", seller.getAvatar());
                sellerInfo.put("createTime", seller.getCreateTime());
                sellerInfo.put("lastLoginTime", seller.getLastLoginTime());
                sellerInfo.put("lastLoginIp", seller.getLastLoginIp());
                int soldCount = orderMapper.countSoldBySellerId(seller.getId()) + bookMapper.countSoldByUserId(seller.getId());
                sellerInfo.put("soldCount", soldCount);
                data.put("seller", sellerInfo);
            }

            response.put("code", 200);
            response.put("data", data);
        } else {
            response.put("code", 404);
            response.put("message", "商品不存在");
        }
        return response;
    }

    @GetMapping("/my")
    public Map<String, Object> getMyBooks(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        List<Book> books = bookService.getMyBooks(userId);
        response.put("code", 200);
        response.put("data", books);
        return response;
    }

    @PutMapping("/{id}/offshelf")
    public Map<String, Object> offShelf(@PathVariable Integer id, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        Book book = bookService.getBookById(id);
        if (book == null || !book.getUserId().equals(userId)) {
            response.put("code", 403);
            response.put("message", "无权操作");
            return response;
        }
        response.put("code", bookService.offShelf(id) ? 200 : 500);
        response.put("message", bookService.offShelf(id) ? "下架成功" : "下架失败");
        return response;
    }

    @PutMapping("/{id}/onshelf")
    public Map<String, Object> onShelf(@PathVariable Integer id, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        Book book = bookService.getBookById(id);
        if (book == null || !book.getUserId().equals(userId)) {
            response.put("code", 403);
            response.put("message", "无权操作");
            return response;
        }
        response.put("code", bookService.onShelf(id) ? 200 : 500);
        response.put("message", bookService.onShelf(id) ? "上架成功" : "上架失败");
        return response;
    }

    @PutMapping("/{id}")
    public Map<String, Object> editBook(@PathVariable Integer id, @RequestBody Book book, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        Book exist = bookService.getBookById(id);
        if (exist == null || !exist.getUserId().equals(userId)) {
            response.put("code", 403);
            response.put("message", "无权操作");
            return response;
        }
        book.setId(id);
        book.setUserId(userId);
        response.put("code", bookService.updateBook(book) ? 200 : 500);
        response.put("message", bookService.updateBook(book) ? "修改成功" : "修改失败");
        return response;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteBook(@PathVariable Integer id, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromRequest(request);
        if (userId == null) {
            response.put("code", 401);
            response.put("message", "请先登录");
            return response;
        }
        Book book = bookService.getBookById(id);
        if (book == null || !book.getUserId().equals(userId)) {
            response.put("code", 403);
            response.put("message", "无权操作");
            return response;
        }
        response.put("code", bookService.deleteBook(id) ? 200 : 500);
        response.put("message", bookService.deleteBook(id) ? "删除成功" : "删除失败");
        return response;
    }
}
