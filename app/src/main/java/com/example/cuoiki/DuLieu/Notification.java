package com.example.cuoiki.DuLieu;

public class Notification {
    private int id;
    private String title;
    private String message;
    private String senderUsername;
    private String receiverUsername;
    private boolean isRead;
    private String createdDate;

    public Notification(int id, String title, String message, String senderUsername, String receiverUsername, boolean isRead, String createdDate) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.isRead = isRead;
        this.createdDate = createdDate;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}

