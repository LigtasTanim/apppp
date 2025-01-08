package com.example.ligtastanim;

public class Comment {
    private String id;
    private String userId;
    private String userName;
    private String text;
    private long timestamp;

    public Comment() {
    }

    public Comment(String id, String userId, String userName, String text, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
} 