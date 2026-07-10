package com.MySpringBoot.my_first_app.entity;
import java.time.LocalDateTime;

public class ChatMessage {
    private Integer id;
    private Integer fromUserId;
    private Integer toUserId;
    private String content;
    private LocalDateTime createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getFromUserId() { return fromUserId; }
    public void setFromUserId(Integer fromUserId) { this.fromUserId = fromUserId; }
    public Integer getToUserId() { return toUserId; }
    public void setToUserId(Integer toUserId) { this.toUserId = toUserId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
