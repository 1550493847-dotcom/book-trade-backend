package com.MySpringBoot.my_first_app.entity;
import java.time.LocalDateTime;

public class Favorite {
    private Integer id;
    private Integer userId;
    private Integer bookId;
    private LocalDateTime createTime;

    // 关联字段（列表查询时使用）
    private String title;
    private String author;
    private Double sellPrice;
    private String images;
    private String bookTitle;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public Double getSellPrice() { return sellPrice; }
    public void setSellPrice(Double sellPrice) { this.sellPrice = sellPrice; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
}
