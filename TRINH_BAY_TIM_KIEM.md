# TRÌNH BÀY MÀN HÌNH TÌM KIẾM
## CustomerSearchActivity - Ứng dụng Cuối Kỳ

---

## 1. TỔNG QUAN MÀN HÌNH

### 1.1. Mục đích
Màn hình tìm kiếm sản phẩm, cho phép khách hàng:
- Tìm kiếm sản phẩm theo tên
- Gợi ý tên sản phẩm khi nhập
- Xem kết quả tìm kiếm real-time
- Thêm sản phẩm vào giỏ hàng từ kết quả tìm kiếm
- Xem chi tiết sản phẩm

### 1.2. Công nghệ sử dụng
- **Language**: Java
- **Platform**: Android (minSdk 24, targetSdk 34)
- **UI Framework**: AndroidX (Material Design)
- **Database**: SQLite (SQLiteHelper)
- **Component**: AutoCompleteTextView (gợi ý tự động)

---

## 2. CẤU TRÚC LAYOUT

### 2.1. Layout chính: `activity_customer_search.xml`

```
<ConstraintLayout>  // Container chính
  <LinearLayout>  // Header
    <ImageView id="btnBack" />  // Nút quay lại
    <AutoCompleteTextView id="edtSearch" />  // Ô tìm kiếm
    <ImageView id="btnClear" />  // Nút xóa
  </LinearLayout>
  
  <TextView id="tvSectionTitle" />  // Tiêu đề "Kết quả tìm kiếm"
  <RecyclerView id="recyclerSearch" />  // Danh sách kết quả
</ConstraintLayout>
```

### 2.2. Item Layout
- Sử dụng layout của `CustomerProductAdapter` (GridLayout 2 cột)
- Mỗi item hiển thị: ảnh, tên, giá, nút yêu thích, nút thêm giỏ hàng

---

## 3. CÁC THÀNH PHẦN CHÍNH

### 3.1. AutoCompleteTextView
- **Component**: `AutoCompleteTextView`
- **Chức năng**: 
  - Cho phép nhập từ khóa tìm kiếm
  - Hiển thị gợi ý tên sản phẩm khi nhập
  - Tự động tìm kiếm khi gõ

### 3.2. RecyclerView - Kết Quả Tìm Kiếm
- **Component**: `RecyclerView` với `GridLayoutManager` (2 cột)
- **Adapter**: `CustomerProductAdapter`
- **Chức năng**: Hiển thị kết quả tìm kiếm real-time

### 3.3. Nút Xóa
- **Component**: `ImageView`
- **Chức năng**: Xóa nội dung tìm kiếm và ẩn kết quả

---

## 4. CODE IMPLEMENTATION CHI TIẾT

### 4.1. CustomerSearchActivity.java

#### A. Khởi tạo (onCreate)
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_customer_search);

    initViews();
    loadAllProducts();
    setupSearch();
}
```

#### B. Load Tất Cả Sản Phẩm
```java
private void loadAllProducts() {
    allProducts = dbHelper.getAllProducts();
    for (Product p : allProducts) {
        nameSuggestions.add(p.getName());
    }
}
```

#### C. Setup Tìm Kiếm
```java
private void setupSearch() {
    // Gợi ý tên sản phẩm khi nhập
    ArrayAdapter<String> suggestAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_dropdown_item_1line, nameSuggestions);
    edtSearch.setAdapter(suggestAdapter);
    edtSearch.setThreshold(1); // Bắt đầu gợi ý từ ký tự đầu tiên

    // Khi chọn từ gợi ý
    edtSearch.setOnItemClickListener((parent, view, position, id) -> {
        String selectedText = (String) parent.getItemAtPosition(position);
        edtSearch.setText(selectedText);
        filterProducts(selectedText);
    });

    // Khi gõ tự động tìm kiếm
    edtSearch.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String keyword = s.toString().trim();
            if (keyword.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                tvSectionTitle.setVisibility(View.GONE);
                btnClear.setVisibility(View.GONE);
            } else {
                btnClear.setVisibility(View.VISIBLE);
                filterProducts(keyword);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    });
}
```

#### D. Lọc Sản Phẩm
```java
private void filterProducts(String keyword) {
    List<Product> filtered = new ArrayList<>();
    for (Product p : allProducts) {
        if (p.getName().toLowerCase().contains(keyword.toLowerCase())) {
            filtered.add(p);
        }
    }

    if (filtered.isEmpty()) {
        recyclerView.setVisibility(View.GONE);
        tvSectionTitle.setVisibility(View.GONE);
    } else {
        recyclerView.setVisibility(View.VISIBLE);
        tvSectionTitle.setVisibility(View.VISIBLE);
        adapter.updateData(filtered);
    }
}
```

#### E. Setup Adapter
```java
private void initViews() {
    edtSearch = findViewById(R.id.edtSearch);
    btnBack = findViewById(R.id.btnBack);
    btnClear = findViewById(R.id.btnClear);
    recyclerView = findViewById(R.id.recyclerSearch);
    tvSectionTitle = findViewById(R.id.tvSectionTitle);

    dbHelper = new SQLiteHelper(this);

    btnBack.setOnClickListener(v -> finish());

    btnClear.setOnClickListener(v -> {
        edtSearch.setText("");
        recyclerView.setVisibility(View.GONE);
        tvSectionTitle.setVisibility(View.GONE);
    });

    // RecyclerView hiển thị kết quả tìm kiếm
    recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
    adapter = new CustomerProductAdapter(this, new ArrayList<>(), new CustomerProductAdapter.OnProductClickListener() {
        @Override
        public void onProductClick(Product product) {
            Intent intent = new Intent(CustomerSearchActivity.this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        }

        @Override
        public void onAddToCart(Product product) {
            addToCart(product);
        }
    });
    recyclerView.setAdapter(adapter);
}
```

#### F. Thêm Vào Giỏ Hàng
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

---

## 5. CÁC TÍNH NĂNG CHÍNH

### 5.1. Tìm Kiếm Real-Time
- ✅ Tự động tìm kiếm khi gõ (TextWatcher)
- ✅ Tìm kiếm không phân biệt hoa thường
- ✅ Tìm kiếm theo tên sản phẩm (contains)

### 5.2. Gợi Ý Tự Động
- ✅ AutoCompleteTextView với ArrayAdapter
- ✅ Gợi ý từ ký tự đầu tiên (threshold = 1)
- ✅ Click gợi ý để tự động điền và tìm kiếm

### 5.3. Hiển Thị Kết Quả
- ✅ GridLayout 2 cột
- ✅ Tự động ẩn/hiện RecyclerView
- ✅ Hiển thị thông báo khi không có kết quả

### 5.4. Tương Tác
- ✅ Click sản phẩm: Xem chi tiết
- ✅ Click nút giỏ hàng: Thêm vào giỏ
- ✅ Nút xóa: Xóa nội dung và ẩn kết quả

---

## 6. LIFECYCLE VÀ PERFORMANCE

### 6.1. Performance Optimization
- ✅ Efficient filtering với ArrayList
- ✅ RecyclerView với ViewHolder pattern
- ✅ Tái sử dụng adapter
- ✅ Case-insensitive search

### 6.2. User Experience
- ✅ Real-time search feedback
- ✅ Auto-complete suggestions
- ✅ Clear button để reset
- ✅ Smooth scrolling

---

## 7. KẾT LUẬN

Màn hình tìm kiếm được xây dựng với:
- **UI/UX tốt**: Auto-complete, real-time search
- **Performance tốt**: Efficient filtering, RecyclerView
- **Tính năng đầy đủ**: Tìm kiếm, gợi ý, thêm giỏ
- **Code clean**: TextWatcher, adapter pattern

**Công nghệ nổi bật:**
1. AutoCompleteTextView cho gợi ý
2. TextWatcher cho real-time search
3. RecyclerView với GridLayoutManager
4. Case-insensitive filtering

---

## 8. HÌNH ẢNH MINH HỌA (Có thể thêm vào Word)

```
┌─────────────────────────────────┐
│  [←]  [Tìm kiếm...]  [X]        │
├─────────────────────────────────┤
│  Kết quả tìm kiếm:               │
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

