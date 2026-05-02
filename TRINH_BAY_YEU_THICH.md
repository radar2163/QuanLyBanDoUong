# TRÌNH BÀY MÀN HÌNH YÊU THÍCH
## FavoriteActivity - Ứng dụng Cuối Kỳ

---

## 1. TỔNG QUAN MÀN HÌNH

### 1.1. Mục đích
Màn hình hiển thị danh sách sản phẩm yêu thích của khách hàng, cho phép:
- Xem tất cả sản phẩm đã đánh dấu yêu thích
- Thêm sản phẩm yêu thích vào giỏ hàng
- Xem chi tiết sản phẩm
- Tự động cập nhật khi có thay đổi yêu thích

### 1.2. Công nghệ sử dụng
- **Language**: Java
- **Platform**: Android (minSdk 24, targetSdk 34)
- **UI Framework**: AndroidX (Material Design)
- **Database**: SQLite (SQLiteHelper)
- **Storage**: SharedPreferences (lưu trữ yêu thích)

---

## 2. CẤU TRÚC LAYOUT

### 2.1. Layout chính: `activity_favorite.xml`

```
<ConstraintLayout>  // Container chính
  <ImageView id="btnBack" />  // Nút quay lại
  <TextView id="tvTitle" />  // Tiêu đề "Yêu thích"
  <RecyclerView id="favoriteRecyclerView" />  // Danh sách sản phẩm
  <TextView id="tvEmptyFavorite" />  // Thông báo danh sách trống
</ConstraintLayout>
```

### 2.2. Item Layout
- Sử dụng layout của `CustomerProductAdapter` (GridLayout 2 cột)
- Mỗi item hiển thị: ảnh, tên, giá, nút yêu thích, nút thêm giỏ hàng

---

## 3. CÁC THÀNH PHẦN CHÍNH

### 3.1. RecyclerView - Danh Sách Yêu Thích
- **Component**: `RecyclerView` với `GridLayoutManager` (2 cột)
- **Adapter**: `CustomerProductAdapter` (tái sử dụng từ màn hình chính)
- **Chức năng**: 
  - Hiển thị danh sách sản phẩm yêu thích
  - Cho phép click để xem chi tiết
  - Cho phép thêm vào giỏ hàng
  - Cho phép bỏ yêu thích

### 3.2. Empty State
- **Component**: `TextView`
- **Chức năng**: Hiển thị thông báo khi danh sách yêu thích trống

---

## 4. CODE IMPLEMENTATION CHI TIẾT

### 4.1. FavoriteActivity.java

#### A. Khởi tạo (onCreate)
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_favorite);

    initViews();
    loadFavoriteProducts();
}
```

#### B. Load Sản Phẩm Yêu Thích
```java
private void loadFavoriteProducts() {
    favoriteList.clear();
    SharedPreferences prefs = getSharedPreferences("favorite_prefs", MODE_PRIVATE);
    
    // Lấy tất cả sản phẩm từ database
    List<Product> allProducts = dbHelper.getAllProducts();
    
    for (Product product : allProducts) {
        boolean isFavorite = prefs.getBoolean("favorite_" + product.getId(), false);
        if (isFavorite) {
            favoriteList.add(product);
        }
    }
    
    adapter.updateData(favoriteList);
    
    // Hiển thị empty state
    if (favoriteList.isEmpty()) {
        tvEmptyFavorite.setVisibility(View.VISIBLE);
        favoriteRecyclerView.setVisibility(View.GONE);
    } else {
        tvEmptyFavorite.setVisibility(View.GONE);
        favoriteRecyclerView.setVisibility(View.VISIBLE);
    }
}
```

#### C. Setup Adapter
```java
private void initViews() {
    favoriteRecyclerView = findViewById(R.id.favoriteRecyclerView);
    tvEmptyFavorite = findViewById(R.id.tvEmptyFavorite);
    
    dbHelper = new SQLiteHelper(this);
    favoriteList = new ArrayList<>();
    
    favoriteRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    
    adapter = new CustomerProductAdapter(this, favoriteList, new CustomerProductAdapter.OnProductClickListener() {
        @Override
        public void onProductClick(Product product) {
            // Mở màn hình chi tiết sản phẩm
            Intent intent = new Intent(FavoriteActivity.this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        }

        @Override
        public void onAddToCart(Product product) {
            addToCart(product);
        }
    });
    
    // Lắng nghe thay đổi yêu thích để tự động refresh
    adapter.setOnFavoriteChangeListener(() -> {
        loadFavoriteProducts(); // Reload khi có thay đổi yêu thích
    });
    
    favoriteRecyclerView.setAdapter(adapter);
}
```

#### D. Thêm Vào Giỏ Hàng
```java
private void addToCart(Product product) {
    if (product.getQuantity() <= 0) {
        Toast.makeText(this, "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT).show();
        return;
    }

    SharedPreferences prefs = getSharedPreferences("cart_prefs", MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    
    String cartKey = "cart_item_" + product.getId();
    int currentQuantity = prefs.getInt(cartKey, 0);
    editor.putInt(cartKey, currentQuantity + 1);
    editor.apply();
    
    Toast.makeText(this, "Đã thêm " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
}
```

#### E. Lifecycle - onResume
```java
@Override
protected void onResume() {
    super.onResume();
    // Reload danh sách yêu thích khi quay lại
    // (có thể đã bỏ yêu thích ở màn hình khác)
    loadFavoriteProducts();
}
```

---

## 5. CÁC TÍNH NĂNG CHÍNH

### 5.1. Hiển Thị Danh Sách
- ✅ Load sản phẩm yêu thích từ SharedPreferences
- ✅ Hiển thị dạng Grid 2 cột
- ✅ Tự động cập nhật khi có thay đổi

### 5.2. Tương Tác
- ✅ Click sản phẩm: Xem chi tiết
- ✅ Click nút giỏ hàng: Thêm vào giỏ
- ✅ Click nút yêu thích: Bỏ yêu thích (tự động refresh)

### 5.3. Empty State
- ✅ Hiển thị thông báo khi danh sách trống
- ✅ Ẩn RecyclerView khi không có sản phẩm

### 5.4. Auto Refresh
- ✅ Tự động reload khi quay lại màn hình (onResume)
- ✅ Tự động reload khi bỏ yêu thích (callback)

---

## 6. CUSTOMER PRODUCT ADAPTER

### 6.1. Tái Sử Dụng Adapter
- Sử dụng `CustomerProductAdapter` từ màn hình chính
- Adapter tự động xử lý:
  - Hiển thị icon yêu thích (filled/unfilled)
  - Click yêu thích để toggle
  - Click sản phẩm để xem chi tiết
  - Click nút giỏ hàng để thêm vào giỏ

### 6.2. Favorite Change Listener
```java
public interface OnFavoriteChangeListener {
    void onFavoriteChanged();
}

// Trong adapter, khi toggle favorite:
if (onFavoriteChangeListener != null) {
    onFavoriteChangeListener.onFavoriteChanged();
}
```

---

## 7. LIFECYCLE VÀ PERFORMANCE

### 7.1. Lifecycle Methods
```java
@Override
protected void onResume() {
    super.onResume();
    loadFavoriteProducts();  // Reload khi quay lại
}
```

### 7.2. Performance Optimization
- ✅ RecyclerView với ViewHolder pattern
- ✅ Tái sử dụng adapter từ màn hình chính
- ✅ Efficient SharedPreferences operations
- ✅ Lazy loading với RecyclerView

---

## 8. KẾT LUẬN

Màn hình yêu thích được xây dựng với:
- **UI/UX tốt**: Grid layout, clear actions
- **Performance tốt**: RecyclerView, tái sử dụng adapter
- **Tính năng đầy đủ**: Xem, thêm giỏ, bỏ yêu thích
- **Code clean**: Tái sử dụng code, callback pattern

**Công nghệ nổi bật:**
1. RecyclerView với GridLayoutManager
2. SharedPreferences cho persistent storage
3. Callback interface cho communication
4. Tái sử dụng adapter

---

## 9. HÌNH ẢNH MINH HỌA (Có thể thêm vào Word)

```
┌─────────────────────────────────┐
│  [← Back]    Yêu thích          │
├─────────────────────────────────┤
│  ┌──────────┐  ┌──────────┐     │
│  │ [Ảnh]    │  │ [Ảnh]    │     │
│  │ Tên SP 1 │  │ Tên SP 2 │     │
│  │ 17,000 đ │  │ 25,000 đ │     │
│  │ [❤️] [🛒]│  │ [❤️] [🛒]│     │
│  └──────────┘  └──────────┘     │
│  ┌──────────┐  ┌──────────┐     │
│  │ [Ảnh]    │  │ [Ảnh]    │     │
│  │ Tên SP 3 │  │ Tên SP 4 │     │
│  │ ...      │  │ ...      │     │
│  └──────────┘  └──────────┘     │
└─────────────────────────────────┘
```

---

*Tài liệu này có thể copy vào Word và chỉnh sửa theo nhu cầu trình bày.*

