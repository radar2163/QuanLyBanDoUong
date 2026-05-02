package com.example.cuoiki.DuLieu;

public class Transaction {
    private int productId;
    private String productName;
    private String transactionType;
    private int quantity;
    private String transactionDate;
    private String userUsername;
    private String userFullName;
    private String userEmail;

    // Constructor đầy đủ
    public Transaction(int productId, String productName, String transactionType, int quantity, String transactionDate, String userUsername, String userFullName, String userEmail) {
        this.productId = productId;
        this.productName = productName;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.transactionDate = transactionDate;
        this.userUsername = userUsername;
        this.userFullName = userFullName;
        this.userEmail = userEmail;
    }

    // Constructor tương thích ngược (không có thông tin người dùng)
    public Transaction(int productId, String productName, String transactionType, int quantity, String transactionDate) {
        this(productId, productName, transactionType, quantity, transactionDate, null, null, null);
    }

    // Getter and Setter methods
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getUserUsername() {
        return userUsername;
    }

    public void setUserUsername(String userUsername) {
        this.userUsername = userUsername;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
