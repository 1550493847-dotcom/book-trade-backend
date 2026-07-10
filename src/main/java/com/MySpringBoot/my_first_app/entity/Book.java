package com.MySpringBoot.my_first_app.entity;
import java.time.LocalDateTime;

public class Book {
    private Integer id;
    private Integer userId;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private String bookCondition;
    private String category;
    private Double originalPrice;
    private Double sellPrice;
    private String description;
    private String images;
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public String getBookCondition() { return bookCondition; }
    public void setBookCondition(String bookCondition) { this.bookCondition = bookCondition; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(Double originalPrice) { this.originalPrice = originalPrice; }
    public Double getSellPrice() { return sellPrice; }
    public void setSellPrice(Double sellPrice) { this.sellPrice = sellPrice; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
