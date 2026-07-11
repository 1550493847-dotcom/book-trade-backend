package com.MySpringBoot.my_first_app.service;

import com.MySpringBoot.my_first_app.entity.Book;
import com.MySpringBoot.my_first_app.mapper.BookMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class BookService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    // Redis key 前缀
    private static final String CACHE_KEY_ALL_BOOKS = "book:list:all";
    private static final String CACHE_KEY_BOOK_DETAIL = "book:detail:";
    private static final String CACHE_KEY_CATEGORIES = "book:categories";
    private static final String CACHE_KEY_SELLER = "user:seller:";
    private static final String CACHE_KEY_VIEW_COUNT = "book:view:";

    // TTL
    private static final long TTL_ALL_BOOKS = 5;      // 5 分钟
    private static final long TTL_BOOK_DETAIL = 30;   // 30 分钟
    private static final long TTL_SELLER = 10;        // 10 分钟
    private static final long TTL_CATEGORIES = 60;    // 60 分钟

    // ==================== 图书发布 ====================

    public boolean publish(Book book) {
        boolean ok = bookMapper.insert(book) > 0;
        if (ok) {
            evictAllBooksCache();
            evictCategoriesCache();
        }
        return ok;
    }

    // ==================== 图书列表（缓存） ====================

    @SuppressWarnings("unchecked")
    public List<Book> getAllBooks() {
        // 尝试从缓存获取
        if (redisTemplate != null) {
            Object cached = redisTemplate.opsForValue().get(CACHE_KEY_ALL_BOOKS);
            if (cached instanceof List) {
                return (List<Book>) cached;
            }
        }
        // 缓存未命中，从数据库查询
        List<Book> books = bookMapper.findAll();
        // 写入缓存
        if (redisTemplate != null && books != null) {
            redisTemplate.opsForValue().set(
                    CACHE_KEY_ALL_BOOKS, books, TTL_ALL_BOOKS, TimeUnit.MINUTES);
        }
        return books;
    }

    // ==================== 图书详情（缓存） ====================

    public Book getBookById(Integer id) {
        String cacheKey = CACHE_KEY_BOOK_DETAIL + id;
        // 尝试从缓存获取
        if (redisTemplate != null) {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof Book) {
                return (Book) cached;
            }
        }
        // 缓存未命中，从数据库查询
        Book book = bookMapper.findById(id);
        // 写入缓存
        if (redisTemplate != null && book != null) {
            redisTemplate.opsForValue().set(
                    cacheKey, book, TTL_BOOK_DETAIL, TimeUnit.MINUTES);
        }
        return book;
    }

    // ==================== 我的图书 ====================

    public List<Book> getMyBooks(Integer userId) {
        return bookMapper.findByUserId(userId);
    }

    // ==================== 上架/下架/售出 ====================

    public boolean onShelf(Integer id) {
        boolean ok = bookMapper.onShelf(id) > 0;
        if (ok) {
            evictBookDetailCache(id);
            evictAllBooksCache();
        }
        return ok;
    }

    public boolean offShelf(Integer id) {
        boolean ok = bookMapper.offShelf(id) > 0;
        if (ok) {
            evictBookDetailCache(id);
            evictAllBooksCache();
        }
        return ok;
    }

    public boolean sellBook(Integer id) {
        boolean ok = bookMapper.sellBook(id) > 0;
        if (ok) {
            evictBookDetailCache(id);
            evictAllBooksCache();
        }
        return ok;
    }

    // ==================== 编辑/删除 ====================

    public boolean updateBook(Book book) {
        boolean ok = bookMapper.update(book) > 0;
        if (ok) {
            evictBookDetailCache(book.getId());
            evictAllBooksCache();
        }
        return ok;
    }

    public boolean deleteBook(Integer id) {
        boolean ok = bookMapper.deleteById(id) > 0;
        if (ok) {
            evictBookDetailCache(id);
            evictAllBooksCache();
        }
        return ok;
    }

    // ==================== 浏览计数（Redis INCR + 回写 MySQL） ====================

    public void incrementViewCount(Integer id) {
        if (redisTemplate != null) {
            String cacheKey = CACHE_KEY_VIEW_COUNT + id;
            redisTemplate.opsForValue().increment(cacheKey);
            // 每 10 次浏览回写一次 MySQL
            Long count = redisTemplate.opsForValue().increment(CACHE_KEY_VIEW_COUNT + ":batch:" + id);
            if (count != null && count % 10 == 0) {
                Object val = redisTemplate.opsForValue().get(cacheKey);
                if (val instanceof Integer) {
                    bookMapper.updateViewCountById(id, (Integer) val);
                }
            }
        } else {
            bookMapper.incrementViewCount(id);
        }
    }

    // ==================== 获取浏览计数（Redis + MySQL 合并） ====================

    public int getViewCount(Integer bookId) {
        if (redisTemplate != null) {
            Object cached = redisTemplate.opsForValue().get(CACHE_KEY_VIEW_COUNT + bookId);
            if (cached instanceof Integer) {
                return (Integer) cached;
            }
        }
        Book book = bookMapper.findById(bookId);
        return book != null ? (book.getViewCount() != null ? book.getViewCount() : 0) : 0;
    }

    // ==================== 缓存失效 ====================

    public void evictAllBooksCache() {
        if (redisTemplate != null) {
            redisTemplate.delete(CACHE_KEY_ALL_BOOKS);
        }
    }

    public void evictBookDetailCache(Integer id) {
        if (redisTemplate != null) {
            redisTemplate.delete(CACHE_KEY_BOOK_DETAIL + id);
        }
    }

    public void evictSellerCache(Integer userId) {
        if (redisTemplate != null) {
            redisTemplate.delete(CACHE_KEY_SELLER + userId);
        }
    }

    public void evictCategoriesCache() {
        if (redisTemplate != null) {
            redisTemplate.delete(CACHE_KEY_CATEGORIES);
        }
    }
}
