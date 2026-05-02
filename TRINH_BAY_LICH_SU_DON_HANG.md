# TRÌNH BÀY MÀN HÌNH LỊCH SỬ ĐƠN HÀNG
## OrderHistoryActivity - Ứng dụng Cuối Kỳ

---

## 1. TỔNG QUAN MÀN HÌNH

### 1.1. Mục đích
Màn hình hiển thị lịch sử đơn hàng của khách hàng, cho phép:
- Xem tất cả đơn hàng đã đặt
- Xem thông tin chi tiết từng đơn hàng (sản phẩm, số lượng, tổng tiền)
- Xem trạng thái đơn hàng với màu sắc phân biệt
- Tự động cập nhật khi có đơn hàng mới

### 1.2. Công nghệ sử dụng
- **Language**: Java
- **Platform**: Android (minSdk 24, targetSdk 34)
- **UI Framework**: AndroidX (Material Design)
- **Database**: SQLite (SQLiteHelper)
- **Storage**: SharedPreferences (lưu thông tin user)

---

## 2. CẤU TRÚC LAYOUT

### 2.1. Layout chính: `activity_order_history.xml`

```
<ConstraintLayout>  // Container chính
  <ImageView id="btnBack" />  // Nút quay lại
  <TextView id="tvTitle" />  // Tiêu đề "Lịch sử đơn hàng"
  <RecyclerView id="recyclerOrders" />  // Danh sách đơn hàng
  <TextView id="tvEmpty" />  // Thông báo danh sách trống
</ConstraintLayout>
```

### 2.2. Item Layout: `item_order_history.xml`

```
<CardView>  // Card đơn hàng
  <TextView id="tvOrderId" />  // Mã đơn hàng (#011)
  <TextView id="tvStatus" />  // Trạng thái (với màu sắc)
  <TextView id="tvOrderDate" />  // Ngày đặt hàng
  <TextView id="tvProducts" />  // Danh sách sản phẩm
  <TextView id="tvTotalPrice" />  // Tổng tiền
</CardView>
```

---

## 3. CÁC THÀNH PHẦN CHÍNH

### 3.1. RecyclerView - Danh Sách Đơn Hàng
- **Component**: `RecyclerView` với `LinearLayoutManager`
- **Adapter**: `OrderAdapter` (inner class)
- **Chức năng**: 
  - Hiển thị danh sách đơn hàng theo thứ tự mới nhất
  - Mỗi item hiển thị đầy đủ thông tin đơn hàng

### 3.2. Trạng Thái Đơn Hàng (Màu Sắc)
- **Đang chờ xử lý**: Text màu cam (#FF9800), background cam nhạt (#FFF3E0)
- **Đang xử lý**: Text màu xanh dương (#2196F3), background xanh dương nhạt (#E3F2FD)
- **Thành công / Đã thanh toán**: Text màu xanh lá (#4CAF50), background xanh lá nhạt (#E8F5E9)
- **Thất bại**: Text màu đỏ (#F44336), background đỏ nhạt (#FFEBEE)

### 3.3. Empty State
- **Component**: `TextView`
- **Chức năng**: Hiển thị thông báo khi chưa có đơn hàng nào

---

## 4. CODE IMPLEMENTATION CHI TIẾT

### 4.1. OrderHistoryActivity.java

#### A. Khởi tạo (onCreate)
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_order_history);

    initViews();
    loadOrders();
}
```

#### B. Load Đơn Hàng
```java
private void loadOrders() {
    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
    String username = prefs.getString("logged_user", null);

    if (username != null) {
        orderList.clear();
        orderList.addAll(dbHelper.getOrdersByUsername(username));
        adapter.notifyDataSetChanged();
    }

    // Hiển thị empty state
    if (orderList.isEmpty()) {
        tvEmpty.setVisibility(View.VISIBLE);
        recyclerOrders.setVisibility(View.GONE);
    } else {
        tvEmpty.setVisibility(View.GONE);
        recyclerOrders.setVisibility(View.VISIBLE);
    }
}
```

#### C. Lifecycle - onResume
```java
@Override
protected void onResume() {
    super.onResume();
    loadOrders();  // Reload khi quay lại (có thể có đơn hàng mới)
}
```

---

## 5. ORDER ADAPTER

### 5.1. OrderAdapter (Inner Class)

#### A. ViewHolder
```java
class OrderViewHolder extends RecyclerView.ViewHolder {
    TextView tvOrderId, tvOrderDate, tvProducts, tvTotalPrice, tvStatus;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);
        tvOrderId = itemView.findViewById(R.id.tvOrderId);
        tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
        tvProducts = itemView.findViewById(R.id.tvProducts);
        tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
        tvStatus = itemView.findViewById(R.id.tvStatus);
    }
}
```

#### B. Bind Data
```java
@Override
public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
    Order order = orders.get(position);
    
    holder.tvOrderId.setText("Đơn hàng #" + String.format("%03d", order.getId()));
    holder.tvOrderDate.setText("Ngày đặt: " + order.getOrderDate());
    holder.tvTotalPrice.setText(currencyFormat.format(order.getTotalPrice()) + " đ");
    
    // Hiển thị danh sách sản phẩm
    StringBuilder productsText = new StringBuilder();
    if (order.getItems() != null) {
        for (int i = 0; i < order.getItems().size(); i++) {
            Order.OrderItem item = order.getItems().get(i);
            productsText.append(item.getProductName());
            productsText.append(" x").append(item.getQuantity());
            if (i < order.getItems().size() - 1) {
                productsText.append(", ");
            }
        }
    }
    holder.tvProducts.setText(productsText.toString());
    
    // Hiển thị trạng thái với màu sắc
    String status = order.getStatus();
    holder.tvStatus.setText(status);
    updateStatusColor(holder.tvStatus, status);
}
```

#### C. Cập Nhật Màu Sắc Trạng Thái
```java
private void updateStatusColor(TextView tvStatus, String status) {
    tvStatus.setText(status);
    if ("Đang chờ xử lý".equals(status)) {
        tvStatus.setTextColor(0xFFFF9800); // Cam
        tvStatus.setBackgroundColor(0xFFFFF3E0); // Cam nhạt
    } else if ("Đang xử lý".equals(status)) {
        tvStatus.setTextColor(0xFF2196F3); // Xanh dương
        tvStatus.setBackgroundColor(0xFFE3F2FD); // Xanh dương nhạt
    } else if ("Thành công".equals(status) || "Đã thanh toán".equals(status)) {
        tvStatus.setTextColor(0xFF4CAF50); // Xanh lá
        tvStatus.setBackgroundColor(0xFFE8F5E9); // Xanh lá nhạt
    } else if ("Thất bại".equals(status)) {
        tvStatus.setTextColor(0xFFF44336); // Đỏ
        tvStatus.setBackgroundColor(0xFFFFEBEE); // Đỏ nhạt
    } else {
        // Màu mặc định cho các trạng thái khác
        tvStatus.setTextColor(0xFF757575); // Xám
        tvStatus.setBackgroundColor(0xFFF5F5F5); // Xám nhạt
    }
}
```

---

## 6. CÁC TÍNH NĂNG CHÍNH

### 6.1. Hiển Thị Danh Sách
- ✅ Load đơn hàng từ database theo username
- ✅ Hiển thị danh sách với RecyclerView
- ✅ Format mã đơn hàng (#001, #002...)
- ✅ Format tổng tiền với dấu phẩy

### 6.2. Thông Tin Đơn Hàng
- ✅ Mã đơn hàng
- ✅ Ngày đặt hàng
- ✅ Danh sách sản phẩm (tên x số lượng)
- ✅ Tổng tiền
- ✅ Trạng thái với màu sắc

### 6.3. Màu Sắc Trạng Thái
- ✅ Phân biệt rõ ràng các trạng thái
- ✅ Đồng bộ với màn hình quản lý đơn hàng của admin
- ✅ Visual feedback tốt

### 6.4. Auto Refresh
- ✅ Tự động reload khi quay lại màn hình (onResume)
- ✅ Cập nhật khi có đơn hàng mới

---

## 7. LIFECYCLE VÀ PERFORMANCE

### 7.1. Lifecycle Methods
```java
@Override
protected void onResume() {
    super.onResume();
    loadOrders();  // Reload khi quay lại
}
```

### 7.2. Performance Optimization
- ✅ RecyclerView với ViewHolder pattern
- ✅ Efficient database query (getOrdersByUsername)
- ✅ Format currency một lần
- ✅ StringBuilder cho string concatenation

---

## 8. KẾT LUẬN

Màn hình lịch sử đơn hàng được xây dựng với:
- **UI/UX tốt**: Card layout, màu sắc phân biệt trạng thái
- **Performance tốt**: RecyclerView, efficient queries
- **Tính năng đầy đủ**: Xem đầy đủ thông tin đơn hàng
- **Code clean**: Inner adapter, color utility method

**Công nghệ nổi bật:**
1. RecyclerView với LinearLayoutManager
2. Color coding cho trạng thái
3. SQLite query filtering
4. SharedPreferences cho user info

---

## 9. HÌNH ẢNH MINH HỌA (Có thể thêm vào Word)

```
┌─────────────────────────────────┐
│  [← Back]    Lịch sử đơn hàng   │
├─────────────────────────────────┤
│  ┌───────────────────────────┐  │
│  │ Đơn hàng #011            │  │
│  │ [Đang chờ xử lý] (cam)   │  │
│  │ Ngày đặt: 12/12/2025     │  │
│  │ Sản phẩm: Tiger bac x1   │  │
│  │ Tổng tiền: 17,000 đ      │  │
│  └───────────────────────────┘  │
│  ┌───────────────────────────┐  │
│  │ Đơn hàng #010            │  │
│  │ [Đã thanh toán] (xanh lá)│  │
│  │ Ngày đặt: 11/12/2025     │  │
│  │ Sản phẩm: ...            │  │
│  │ Tổng tiền: 34,000 đ      │  │
│  └───────────────────────────┘  │
└─────────────────────────────────┘
```

---

*Tài liệu này có thể copy vào Word và chỉnh sửa theo nhu cầu trình bày.*

