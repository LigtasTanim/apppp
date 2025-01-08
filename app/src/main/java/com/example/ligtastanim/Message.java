package com.example.ligtastanim;

public class Message {
    private String messageId;
    private String message;
    private String senderName;
    private long timestamp;
    private boolean sentByUser;

    public Message() {} // Required for Firebase

    public Message(String messageId, String message, String senderName, long timestamp, boolean sentByUser) {
        this.messageId = messageId;
        this.message = message;
        this.senderName = senderName;
        this.timestamp = timestamp;
        this.sentByUser = sentByUser;
    }

    // Getters and setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public boolean isSentByUser() { return sentByUser; }
    public void setSentByUser(boolean sentByUser) { this.sentByUser = sentByUser; }
} 