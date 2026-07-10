package com.MySpringBoot.my_first_app.service;

import com.MySpringBoot.my_first_app.entity.Book;
import com.MySpringBoot.my_first_app.mapper.BookMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookMapper bookMapper;

    public boolean publish(Book book) {
        return bookMapper.insert(book) > 0;
    }

    public List<Book> getAllBooks() {
        return bookMapper.findAll();
    }

    public Book getBookById(Integer id) {
        return bookMapper.findById(id);
    }

    public List<Book> getMyBooks(Integer userId) {
        return bookMapper.findByUserId(userId);
    }

    public boolean onShelf(Integer id) {
        return bookMapper.onShelf(id) > 0;
    }

    public boolean offShelf(Integer id) {
        return bookMapper.offShelf(id) > 0;
    }

    public boolean sellBook(Integer id) {
        return bookMapper.sellBook(id) > 0;
    }

    public boolean updateBook(Book book) {
        return bookMapper.update(book) > 0;
    }

    public boolean deleteBook(Integer id) {
        return bookMapper.deleteById(id) > 0;
    }

    public void incrementViewCount(Integer id) {
        bookMapper.incrementViewCount(id);
    }
}
