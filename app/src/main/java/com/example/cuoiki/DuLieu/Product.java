package com.example.cuoiki.DuLieu;

public class Product {
    private int id;
    private String name;
    private int quantity;
    private String init;
    private double price;
    private int imageResId;   // ảnh có sẵn trong drawable
    private String imageUri;  // ảnh lấy từ file hoặc thư viện
    private String category;  // loại sản phẩm (Bia, Rượu, Nước ngọt...)

    // Constructor đầy đủ
    public Product(int id, String name, int quantity, String init, double price,
                   int imageResId, String imageUri, String category) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.init = init;
        this.price = price;
        this.imageResId = imageResId;
        this.imageUri = imageUri;
        this.category = category;
    }

    //  Constructor khi thêm mới sản phẩm (chưa có id)
    public Product(String name, int quantity, String init, double price,
                   int imageResId, String imageUri, String category) {
        this.name = name;
        this.quantity = quantity;
        this.init = init;
        this.price = price;
        this.imageResId = imageResId;
        this.imageUri = imageUri;
        this.category = category;
    }

    //  Getter & Setter
    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getInit() { return init; }

    public void setInit(String init) { this.init = init; }

    public double getPrice() { return price; }

    public void setPrice(double price) { this.price = price; }

    public int getImageResId() { return imageResId; }

    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

    public String getImageUri() { return imageUri; }

    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    public String getCategory() { return category; }

    public void setCategory(String category) { this.category = category; }
}
