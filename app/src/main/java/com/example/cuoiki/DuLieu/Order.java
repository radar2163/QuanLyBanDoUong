package com.example.cuoiki.DuLieu;

import java.util.List;

public class Order {
    private int id;
    private String customerName;
    private String phoneNumber;
    private String orderDate;
    private String address;
    private String paymentMethod;
    private double totalPrice;
    private String status; // "Đang chờ xử lý", "Đang xử lý", "Thành công", "Thất bại"
    private String username; // Username của khách hàng
    private List<OrderItem> items; // Danh sách sản phẩm trong đơn hàng

    public Order() {
    }

    public Order(int id, String customerName, String phoneNumber, String orderDate, 
                 String address, String paymentMethod, double totalPrice, String status, String username) {
        this.id = id;
        this.customerName = customerName;
        this.phoneNumber = phoneNumber;
        this.orderDate = orderDate;
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.totalPrice = totalPrice;
        this.status = status;
        this.username = username;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    // Inner class để lưu thông tin sản phẩm trong đơn hàng
    public static class OrderItem {
        private int productId;
        private String productName;
        private int quantity;
        private double price;

        public OrderItem(int productId, String productName, int quantity, double price) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
        }

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

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }
}

