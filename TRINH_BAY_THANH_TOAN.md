# TRÌNH BÀY MÀN HÌNH THANH TOÁN
## CheckoutActivity - Ứng dụng Cuối Kỳ

---

## 1. TỔNG QUAN MÀN HÌNH

### 1.1. Mục đích
Màn hình thanh toán đơn hàng, cho phép khách hàng:
- Xem thông tin đơn hàng (sản phẩm, số lượng, tổng tiền)
- Xem thông tin khách hàng (tên, SĐT, địa chỉ)
- Chọn phương thức thanh toán (COD hoặc VNPay QR)
- Nhập địa chỉ giao hàng
- Xác nhận đặt hàng

### 1.2. Công nghệ sử dụng
- **Language**: Java
- **Platform**: Android (minSdk 24, targetSdk 34)
- **UI Framework**: AndroidX (Material Design)
- **Database**: SQLite (SQLiteHelper)
- **Payment**: VNPay Integration (VNPayHelper)
- **Email**: EmailHelper (gửi email xác nhận)

---

## 2. CẤU TRÚC LAYOUT

### 2.1. Layout chính: `activity_checkout.xml`

```
<ScrollView>  // Cho phép scroll dọc
  <LinearLayout>  // Container chính
    <ImageView id="btnBack" />  // Nút quay lại
    
    // Thông tin khách hàng
    <TextView id="tvFullName" />  // Tên khách hàng
    <TextView id="tvPhoneNumber" />  // Số điện thoại
    <TextView id="tvOrderDate" />  // Ngày đặt hàng
    <EditText id="edtAddress" />  // Địa chỉ giao hàng
    
    // Danh sách sản phẩm
    <RecyclerView id="recyclerOrderItems" />  // Danh sách sản phẩm trong đơn
    
    // Phương thức thanh toán
    <CardView id="cardCOD" />  // Card COD
      <ImageView id="ivCODSelected" />  // Icon selected
    <CardView id="cardQR" />  // Card QR
      <ImageView id="ivQRSelected" />  // Icon selected
    
    // Tổng tiền
    <TextView id="tvTotalPrice" />  // Tổng tiền
    
    // Nút xác nhận
    <Button id="btnConfirmOrder" />  // Xác nhận đặt hàng
  </LinearLayout>
</ScrollView>
```

### 2.2. Item Layout: `item_order_item.xml`

```
<CardView>  // Card sản phẩm
  <ImageView id="productImage" />  // Ảnh sản phẩm
  <TextView id="productName" />  // Tên sản phẩm
  <TextView id="productQuantity" />  // Số lượng
  <TextView id="productPrice" />  // Giá
  <TextView id="productSubtotal" />  // Tổng tiền sản phẩm
</CardView>
```

---

## 3. CÁC THÀNH PHẦN CHÍNH

### 3.1. Thông Tin Khách Hàng
- **Tên**: Load từ SharedPreferences (`full_name` hoặc `logged_user`)
- **Số điện thoại**: Load từ SharedPreferences (`phone_number`)
- **Ngày đặt**: Hiển thị ngày giờ hiện tại (dd/MM/yyyy HH:mm)
- **Địa chỉ**: EditText cho phép nhập địa chỉ giao hàng

### 3.2. Danh Sách Sản Phẩm
- **Component**: `RecyclerView` với `LinearLayoutManager`
- **Adapter**: `OrderItemAdapter` (custom adapter)
- **Dữ liệu**: Load từ Intent hoặc SharedPreferences

### 3.3. Phương Thức Thanh Toán
- **COD (Cash on Delivery)**: Thanh toán khi nhận hàng
- **QR (VNPay)**: Thanh toán qua VNPay QR code
- **UI**: CardView với icon selected để hiển thị lựa chọn

### 3.4. Tổng Tiền
- **Component**: `TextView`
- **Format**: "Tổng tiền: X,XXX đ"

---

## 4. CODE IMPLEMENTATION CHI TIẾT

### 4.1. CheckoutActivity.java

#### A. Khởi tạo (onCreate)
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_checkout);

    // Lấy tổng tiền từ Intent
    totalPrice = getIntent().getDoubleExtra("total_price", 0);
    
    dbHelper = new SQLiteHelper(this);
    orderItems = new ArrayList<>();

    initViews();
    loadUserInfo();
    loadOrderItems();
    setupPaymentMethodSelection();
    setupConfirmButton();
}
```

#### B. Load Thông Tin Khách Hàng
```java
private void loadUserInfo() {
    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
    String fullName = prefs.getString("full_name", null);
    
    if (fullName != null && !fullName.isEmpty()) {
        tvFullName.setText(fullName);
    } else {
        String loggedUser = prefs.getString("logged_user", "Khách hàng");
        tvFullName.setText(loggedUser);
    }

    // Hiển thị số điện thoại
    String phoneNumber = prefs.getString("phone_number", null);
    if (phoneNumber != null && !phoneNumber.isEmpty()) {
        tvPhoneNumber.setText(phoneNumber);
    } else {
        tvPhoneNumber.setText("Chưa cập nhật");
    }

    // Hiển thị ngày đặt hàng
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    String currentDate = dateFormat.format(new Date());
    tvOrderDate.setText(currentDate);
}
```

#### C. Load Danh Sách Sản Phẩm
```java
private void loadOrderItems() {
    ArrayList<Integer> productIds = getIntent().getIntegerArrayListExtra("product_ids");
    ArrayList<Integer> quantities = getIntent().getIntegerArrayListExtra("quantities");
    
    if (productIds == null || quantities == null) {
        // Load từ SharedPreferences nếu không có từ Intent
        loadOrderItemsFromSharedPreferences();
        return;
    }
    
    if (productIds.size() == quantities.size() && productIds.size() > 0) {
        orderItems.clear();
        for (int i = 0; i < productIds.size(); i++) {
            Product product = dbHelper.getProductById(productIds.get(i));
            if (product != null) {
                orderItems.add(new OrderItem(product, quantities.get(i)));
            }
        }
        orderAdapter.notifyDataSetChanged();
    }
}
```

#### D. Chọn Phương Thức Thanh Toán
```java
private void setupPaymentMethodSelection() {
    cardCOD.setOnClickListener(v -> {
        selectedPaymentMethod = "COD";
        updatePaymentMethodUI();
    });

    cardQR.setOnClickListener(v -> {
        selectedPaymentMethod = "QR";
        updatePaymentMethodUI();
    });

    // Mặc định chọn COD
    updatePaymentMethodUI();
}

private void updatePaymentMethodUI() {
    if ("COD".equals(selectedPaymentMethod)) {
        ivCODSelected.setVisibility(View.VISIBLE);
        ivQRSelected.setVisibility(View.GONE);
    } else {
        ivQRSelected.setVisibility(View.VISIBLE);
        ivCODSelected.setVisibility(View.GONE);
    }
}
```

#### E. Xử Lý Đặt Hàng
```java
private void processOrder(String address) {
    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
    String fullName = tvFullName.getText().toString();
    String phoneNumber = tvPhoneNumber.getText().toString();
    String orderDate = tvOrderDate.getText().toString();
    String username = prefs.getString("logged_user", null);

    // Tạo đơn hàng
    Order order = new Order();
    order.setCustomerName(fullName);
    order.setPhoneNumber(phoneNumber);
    order.setOrderDate(orderDate);
    order.setAddress(address);
    order.setTotalPrice(totalPrice);
    order.setStatus("Đang chờ xử lý");
    order.setUsername(username);
    
    // Set payment method
    if ("QR".equals(selectedPaymentMethod)) {
        order.setPaymentMethod("Thanh toán qua VNPay");
    } else {
        order.setPaymentMethod("Thanh toán khi nhận hàng");
    }

    // Lấy danh sách sản phẩm
    List<Order.OrderItem> orderItems = new ArrayList<>();
    for (CheckoutActivity.OrderItem item : this.orderItems) {
        Product product = item.getProduct();
        Order.OrderItem orderItem = new Order.OrderItem(
            product.getId(),
            product.getName(),
            item.getQuantity(),
            product.getPrice()
        );
        orderItems.add(orderItem);
    }
    order.setItems(orderItems);

    // Insert vào database
    long orderId = dbHelper.insertOrder(order);
    
    if (orderId > 0) {
        if ("QR".equals(selectedPaymentMethod)) {
            // Xử lý thanh toán VNPay
            processVNPayPayment(orderId);
        } else {
            // COD - Gửi email và chuyển màn hình
            EmailHelper.sendOrderConfirmationEmail(this, order);
            clearCart();
            Intent intent = new Intent(this, OrderSuccessActivity.class);
            intent.putExtra("order_id", orderId);
            startActivity(intent);
            finish();
        }
    } else {
        Toast.makeText(this, "Lỗi: Không thể tạo đơn hàng. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
    }
}
```

#### F. Xử Lý Thanh Toán VNPay
```java
private void processVNPayPayment(long orderId) {
    try {
        String paymentUrl = VNPayHelper.createPaymentUrl(
            (int) orderId,
            totalPrice,
            "Thanh toan don hang #" + orderId
        );
        
        if (paymentUrl == null || paymentUrl.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không thể tạo URL thanh toán.", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Chuyển đến màn hình thanh toán VNPay
        Intent intent = new Intent(this, VNPayPaymentActivity.class);
        intent.putExtra("payment_url", paymentUrl);
        intent.putExtra("order_id", String.valueOf(orderId));
        intent.putExtra("order_id_long", orderId);
        
        startActivityForResult(intent, 1001);
    } catch (Exception e) {
        Toast.makeText(this, "Lỗi không thể xử lý thanh toán: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    
    if (requestCode == 1001) {
        if (resultCode == RESULT_OK && data != null) {
            // Thanh toán thành công
            String paymentStatus = data.getStringExtra("payment_status");
            long orderId = data.getLongExtra("order_id_long", -1);
            
            if ("success".equals(paymentStatus) && orderId > 0) {
                // Cập nhật đơn hàng
                Order order = dbHelper.getOrderById((int) orderId);
                if (order != null) {
                    order.setPaymentMethod("Thanh toán qua VNPay");
                    order.setStatus("Đã thanh toán");
                    dbHelper.updateOrder(order);
                    
                    // Gửi email xác nhận
                    EmailHelper.sendOrderConfirmationEmail(this, order);
                }
                
                // Xóa giỏ hàng
                clearCart();
                
                // Chuyển đến màn hình thành công
                Intent intent = new Intent(this, OrderSuccessActivity.class);
                intent.putExtra("order_id", orderId);
                startActivity(intent);
                finish();
            }
        }
    }
}
```

---

## 5. ORDER ITEM ADAPTER

### 5.1. OrderItemAdapter.java

```java
private class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {
    private List<OrderItem> items;
    
    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItem item = items.get(position);
        Product product = item.getProduct();
        
        holder.productName.setText(product.getName());
        holder.productQuantity.setText("Số lượng: " + item.getQuantity());
        holder.productPrice.setText(currencyFormat.format(product.getPrice()) + " đ");
        
        double subtotal = product.getPrice() * item.getQuantity();
        holder.productSubtotal.setText(currencyFormat.format(subtotal) + " đ");
        
        // Load ảnh với Glide
        if (product.getImageUri() != null && !product.getImageUri().isEmpty()) {
            Glide.with(CheckoutActivity.this)
                .load(Uri.parse(product.getImageUri()))
                .placeholder(R.drawable.no_image)
                .error(R.drawable.no_image)
                .into(holder.productImage);
        }
    }
}
```

---

## 6. CÁC TÍNH NĂNG CHÍNH

### 6.1. Hiển Thị Thông Tin
- ✅ Load thông tin khách hàng từ SharedPreferences
- ✅ Hiển thị danh sách sản phẩm với RecyclerView
- ✅ Tính và hiển thị tổng tiền

### 6.2. Phương Thức Thanh Toán
- ✅ Chọn COD hoặc VNPay QR
- ✅ UI feedback khi chọn phương thức
- ✅ Xử lý khác nhau cho từng phương thức

### 6.3. Đặt Hàng
- ✅ Validation địa chỉ giao hàng
- ✅ Tạo đơn hàng trong database
- ✅ Gửi email xác nhận (COD)
- ✅ Xử lý thanh toán VNPay (QR)
- ✅ Xóa giỏ hàng sau khi đặt thành công

### 6.4. VNPay Integration
- ✅ Tạo payment URL
- ✅ Mở WebView thanh toán
- ✅ Xử lý callback từ VNPay
- ✅ Cập nhật trạng thái đơn hàng

---

## 7. LIFECYCLE VÀ PERFORMANCE

### 7.1. Lifecycle Methods
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // Xử lý kết quả từ VNPayPaymentActivity
}
```

### 7.2. Performance Optimization
- ✅ RecyclerView với ViewHolder pattern
- ✅ Efficient database operations
- ✅ Background email sending
- ✅ Error handling và validation

---

## 8. KẾT LUẬN

Màn hình thanh toán được xây dựng với:
- **UI/UX tốt**: Clear layout, payment selection
- **Tính năng đầy đủ**: COD, VNPay, email confirmation
- **Code clean**: Separation of concerns, error handling
- **Integration**: VNPay, EmailHelper

**Công nghệ nổi bật:**
1. VNPay payment gateway integration
2. EmailHelper cho email confirmation
3. Activity result handling
4. SharedPreferences và SQLite

---

## 9. HÌNH ẢNH MINH HỌA (Có thể thêm vào Word)

```
┌─────────────────────────────────┐
│  [← Back]    Thanh toán         │
├─────────────────────────────────┤
│  Thông tin khách hàng:           │
│  Tên: Nguyễn Văn A               │
│  SĐT: 0901234567                 │
│  Ngày đặt: 12/12/2025 17:30      │
│  Địa chỉ: [Nhập địa chỉ...]      │
├─────────────────────────────────┤
│  Sản phẩm:                       │
│  ┌───────────────────────────┐  │
│  │ [Ảnh]  Tên SP x2          │  │
│  │        34,000 đ           │  │
│  └───────────────────────────┘  │
├─────────────────────────────────┤
│  Phương thức thanh toán:         │
│  ┌──────────┐  ┌──────────┐     │
│  │ COD [✓]  │  │ QR [ ]   │     │
│  └──────────┘  └──────────┘     │
├─────────────────────────────────┤
│  Tổng tiền: 34,000 đ             │
│  [Xác nhận đặt hàng]             │
└─────────────────────────────────┘
```

---

*Tài liệu này có thể copy vào Word và chỉnh sửa theo nhu cầu trình bày.*

