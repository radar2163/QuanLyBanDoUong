package com.example.cuoiki.DuLieu;

public class Banner {
    private int id;
    private String imageUri;
    private int imageResId;
    private int displayOrder; // Thứ tự hiển thị
    private boolean isActive; // Có đang hoạt động không

    public Banner() {
    }

    public Banner(int id, String imageUri, int imageResId, int displayOrder, boolean isActive) {
        this.id = id;
        this.imageUri = imageUri;
        this.imageResId = imageResId;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}

