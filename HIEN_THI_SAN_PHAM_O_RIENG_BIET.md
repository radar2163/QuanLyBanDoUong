# HIỂN THỊ SẢN PHẨM TRONG CÁC Ô RIÊNG BIỆT
## RecyclerView + GridLayoutManager + CardView - Ứng dụng Cuối Kỳ

---

## 1. TỔNG QUAN

### 1.1. Công nghệ sử dụng
Để hiển thị sản phẩm trong các ô nhỏ riêng biệt, ứng dụng sử dụng kết hợp 3 công nghệ:

1. **RecyclerView** - Hiển thị danh sách sản phẩm
2. **GridLayoutManager** - Sắp xếp dạng lưới (2 cột)
3. **CardView** - Tạo các card/ô riêng biệt cho mỗi sản phẩm

---

## 2. CÁC COMPONENT CHÍNH

### 2.1. RecyclerView
- **Chức năng**: Hiển thị danh sách có thể scroll
- **Ưu điểm**: 
  - Tái sử dụng View (performance tốt)
  - Hỗ trợ nhiều layout manager
  - Animation mượt mà

### 2.2. GridLayoutManager
- **Chức năng**: Sắp xếp items dạng lưới (grid)
- **Cấu hình**: 2 cột (spanCount = 2)
- **Kết quả**: Sản phẩm hiển thị 2 cột, mỗi sản phẩm 1 ô

### 2.3. CardView
- **Chức năng**: Tạo card/ô riêng biệt với:
  - Bo góc (corner radius)
  - Đổ bóng (elevation)
  - Background màu trắng
  - Margin giữa các card

---

## 3. CODE IMPLEMENTATION

### 3.1. Setup RecyclerView trong CustomerMainActivity

```java
private void setupRecyclerView() {
    productRecyclerView = findViewById(R.id.productRecyclerView);
    
    // GridLayoutManager với 2 cột
    productRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    
    // Tạo adapter
    adapter = new CustomerProductAdapter(this, productList, new CustomerProductAdapter.OnProductClickListener() {
        @Override
        public void onProductClick(Product product) {
            // Xem chi tiết sản phẩm
            Intent intent = new Intent(CustomerMainActivity.this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        }

        @Override
        public void onAddToCart(Product product) {
            // Thêm vào giỏ hàng
            addToCart(product);
        }
    });
    
    productRecyclerView.setAdapter(adapter);
}
```

### 3.2. Giải thích GridLayoutManager

```java
new GridLayoutManager(this, 2)
```

- **`this`**: Context (Activity)
- **`2`**: **Số cột** (spanCount) - Hiển thị 2 cột
- **Kết quả**: Mỗi hàng có 2 sản phẩm

---

## 4. LAYOUT ITEM - CARD RIÊNG BIỆT

### 4.1. File: `item_customer_product.xml`

```xml
<androidx.cardview.widget.CardView
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_margin="8dp"
    app:cardCornerRadius="12dp"
    app:cardElevation="4dp"
    app:cardBackgroundColor="#FFFFFF">

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="12dp">

        <!-- Ảnh sản phẩm -->
        <ImageView
            android:id="@+id/productImage"
            android:layout_width="0dp"
            android:layout_height="150dp"
            android:scaleType="centerCrop" />

        <!-- Tên sản phẩm -->
        <TextView
            android:id="@+id/productName"
            android:text="Tên sản phẩm"
            android:textSize="14sp"
            android:textStyle="bold" />

        <!-- Giá -->
        <TextView
            android:id="@+id/productPrice"
            android:text="₫1.800.000"
            android:textSize="16sp"
            android:textStyle="bold" />

        <!-- Nút thêm vào giỏ -->
        <ImageView
            android:id="@+id/btnAddToCart"
            android:src="@drawable/buy" />

    </androidx.constraintlayout.widget.ConstraintLayout>

</androidx.cardview.widget.CardView>
```

### 4.2. Giải thích CardView Properties

#### A. `app:cardCornerRadius="12dp"`
- **Chức năng**: Bo góc card (12dp)
- **Kết quả**: Card có góc tròn đẹp mắt

#### B. `app:cardElevation="4dp"`
- **Chức năng**: Đổ bóng (elevation)
- **Kết quả**: Card có bóng, tạo độ sâu

#### C. `app:cardBackgroundColor="#FFFFFF"`
- **Chức năng**: Màu nền card (trắng)
- **Kết quả**: Card nổi bật trên nền

#### D. `android:layout_margin="8dp"`
- **Chức năng**: Khoảng cách giữa các card
- **Kết quả**: Các card không dính nhau

---

## 5. ADAPTER - CustomerProductAdapter

### 5.1. Tạo ViewHolder

```java
public static class ProductViewHolder extends RecyclerView.ViewHolder {
    ImageView productImage, btnAddToCart, btnFavorite;
    TextView productName, productPrice, productQuantity, productUnit;

    public ProductViewHolder(@NonNull View itemView) {
        super(itemView);
        // Tìm các view trong item layout
        productImage = itemView.findViewById(R.id.productImage);
        productName = itemView.findViewById(R.id.productName);
        productPrice = itemView.findViewById(R.id.productPrice);
        btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        btnFavorite = itemView.findViewById(R.id.btnFavorite);
    }
}
```

### 5.2. Bind Data vào Card

```java
@Override
public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
    Product product = productList.get(position);
    
    // Hiển thị thông tin sản phẩm
    holder.productName.setText(product.getName());
    holder.productPrice.setText("₫" + currencyFormat.format(product.getPrice()));
    
    // Load ảnh với Glide
    if (product.getImageUri() != null && !product.getImageUri().isEmpty()) {
        Glide.with(holder.itemView.getContext())
            .load(Uri.parse(product.getImageUri()))
            .placeholder(R.drawable.no_image)
            .error(R.drawable.no_image)
            .into(holder.productImage);
    }
    
    // Click vào card để xem chi tiết
    holder.itemView.setOnClickListener(v -> {
        listener.onProductClick(product);
    });
    
    // Click nút thêm vào giỏ
    holder.btnAddToCart.setOnClickListener(v -> {
        listener.onAddToCart(product);
    });
}
```

---

## 6. CÁCH HOẠT ĐỘNG

### 6.1. Quy trình hiển thị

```
1. RecyclerView được setup với GridLayoutManager (2 cột)
   ↓
2. Adapter nhận danh sách sản phẩm
   ↓
3. Adapter tạo ViewHolder cho mỗi sản phẩm
   ↓
4. Inflate layout item_customer_product.xml (CardView)
   ↓
5. Bind data vào CardView (ảnh, tên, giá)
   ↓
6. GridLayoutManager sắp xếp 2 cột
   ↓
7. Hiển thị trên màn hình
```

### 6.2. Tại sao mỗi sản phẩm là 1 ô riêng biệt?

- **CardView**: Mỗi item layout là 1 CardView → Tạo ô riêng biệt
- **GridLayoutManager**: Sắp xếp 2 cột → Mỗi hàng 2 sản phẩm
- **Margin**: `layout_margin="8dp"` → Tạo khoảng cách giữa các card
- **Elevation**: `cardElevation="4dp"` → Tạo bóng, tách biệt các card

---

## 7. DEPENDENCIES

### 7.1. Trong `build.gradle.kts`

```kotlin
// RecyclerView (có sẵn trong AndroidX)
implementation(libs.appcompat)  // Bao gồm RecyclerView

// CardView
implementation("androidx.cardview:cardview:1.0.0")
```

### 7.2. Giải thích

- **RecyclerView**: Có sẵn trong AndroidX (không cần thêm dependency riêng)
- **GridLayoutManager**: Có sẵn trong RecyclerView
- **CardView**: Cần thêm dependency `androidx.cardview:cardview`

---

## 8. CÁC THUỘC TÍNH QUAN TRỌNG

### 8.1. GridLayoutManager

```java
GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
layoutManager.setOrientation(LinearLayoutManager.VERTICAL);  // Dọc
productRecyclerView.setLayoutManager(layoutManager);
```

**Các tham số:**
- **`spanCount`**: Số cột (2 = 2 cột)
- **`orientation`**: Hướng (VERTICAL = dọc)

### 8.2. CardView

```xml
<androidx.cardview.widget.CardView
    app:cardCornerRadius="12dp"      <!-- Bo góc -->
    app:cardElevation="4dp"          <!-- Đổ bóng -->
    app:cardBackgroundColor="#FFFFFF" <!-- Màu nền -->
    android:layout_margin="8dp">      <!-- Khoảng cách -->
```

---

## 9. SO SÁNH VỚI CÁC CÁCH KHÁC

### 9.1. GridLayoutManager vs LinearLayoutManager

#### GridLayoutManager (2 cột)
- ✅ **Dạng lưới**: 2 cột, nhiều hàng
- ✅ **Tiết kiệm không gian**: Hiển thị nhiều sản phẩm hơn
- ✅ **Phù hợp**: Danh sách sản phẩm

#### LinearLayoutManager (1 cột)
- ⚠️ **Dạng danh sách**: 1 cột, nhiều hàng
- ⚠️ **Tốn không gian**: Mỗi hàng 1 sản phẩm
- ⚠️ **Phù hợp**: Danh sách chi tiết

### 9.2. CardView vs LinearLayout

#### CardView
- ✅ **Có elevation**: Đổ bóng đẹp
- ✅ **Bo góc**: Corner radius
- ✅ **Material Design**: Tuân thủ Material Design

#### LinearLayout
- ⚠️ **Không có elevation**: Phải tự code
- ⚠️ **Không bo góc**: Phải dùng drawable
- ⚠️ **Thủ công hơn**: Nhiều code hơn

---

## 10. TÀI LIỆU THAM KHẢO

### 10.1. Official Documentation
- **RecyclerView**: https://developer.android.com/guide/topics/ui/layout/recyclerview
- **GridLayoutManager**: https://developer.android.com/reference/androidx/recyclerview/widget/GridLayoutManager
- **CardView**: https://developer.android.com/reference/androidx/cardview/widget/CardView

### 10.2. Material Design
- **Cards**: https://material.io/components/cards

---

## 11. KẾT LUẬN

**Sản phẩm hiển thị trong các ô nhỏ riêng biệt** nhờ:

1. ✅ **RecyclerView**: Hiển thị danh sách
2. ✅ **GridLayoutManager (2 cột)**: Sắp xếp dạng lưới
3. ✅ **CardView**: Tạo card/ô riêng biệt cho mỗi sản phẩm
4. ✅ **Margin**: Tạo khoảng cách giữa các card
5. ✅ **Elevation**: Tạo bóng, tách biệt các card

**Công nghệ sử dụng:**
- **RecyclerView** + **GridLayoutManager** (AndroidX)
- **CardView** (androidx.cardview:cardview)
- **Adapter Pattern** (CustomerProductAdapter)

**Kết quả**: Mỗi sản phẩm là 1 card riêng biệt, hiển thị 2 cột, có bóng và bo góc đẹp mắt!

---

## 12. HÌNH ẢNH MINH HỌA

```
┌─────────────────────────────────┐
│  ┌──────────┐  ┌──────────┐     │
│  │ [Ảnh]    │  │ [Ảnh]    │     │
│  │ Tên SP 1 │  │ Tên SP 2 │     │
│  │ ₫17,000  │  │ ₫25,000  │     │
│  │ [🛒]     │  │ [🛒]     │     │
│  └──────────┘  └──────────┘     │
│      ↑              ↑           │
│   CardView 1    CardView 2      │
│                                 │
│  ┌──────────┐  ┌──────────┐     │
│  │ [Ảnh]    │  │ [Ảnh]    │     │
│  │ Tên SP 3 │  │ Tên SP 4 │     │
│  │ ₫30,000  │  │ ₫15,000  │     │
│  │ [🛒]     │  │ [🛒]     │     │
│  └──────────┘  └──────────┘     │
│      ↑              ↑           │
│   CardView 3    CardView 4      │
└─────────────────────────────────┘
     ↑
GridLayoutManager (2 cột)
```

---

*Tài liệu này giải thích cách hiển thị sản phẩm trong các ô riêng biệt bằng RecyclerView + GridLayoutManager + CardView.*

