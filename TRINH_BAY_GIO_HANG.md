# TRÌNH BÀY MÀN HÌNH GIỎ HÀNG
## CartActivity - Ứng dụng Cuối Kỳ

---

## 1. TỔNG QUAN MÀN HÌNH

### 1.1. Mục đích
Màn hình quản lý giỏ hàng của khách hàng, cho phép:
- Xem danh sách sản phẩm đã thêm vào giỏ hàng
- Thay đổi số lượng sản phẩm (tăng/giảm)
- Xóa sản phẩm khỏi giỏ hàng
- Xem tổng tiền đơn hàng
- Thanh toán đơn hàng
- Tiếp tục mua sắm

### 1.2. Công nghệ sử dụng
- **Language**: Java
- **Platform**: Android (minSdk 24, targetSdk 34)
- **UI Framework**: AndroidX (Material Design)
- **Database**: SQLite (SQLiteHelper)
- **Storage**: SharedPreferences (lưu trữ giỏ hàng)

---

## 2. CẤU TRÚC LAYOUT

### 2.1. Layout chính: `activity_cart.xml`

```
<ConstraintLayout>  // Container chính
  <ImageView id="btnBack" />  // Nút quay lại
  <TextView id="tvTitle" />  // Tiêu đề "Giỏ hàng"
  <RecyclerView id="cartRecyclerView" />  // Danh sách sản phẩm
  <TextView id="tvEmptyCart" />  // Thông báo giỏ hàng trống
  <TextView id="tvTotalPrice" />  // Tổng tiền
  <Button id="btnCheckout" />  // Nút thanh toán
  <Button id="btnContinueShopping" />  // Nút tiếp tục mua sắm
</ConstraintLayout>
```

### 2.2. Item Layout: `item_cart.xml`

```
<CardView>  // Card chứa thông tin sản phẩm
  <ImageView id="productImage" />  // Ảnh sản phẩm
  <TextView id="productName" />  // Tên sản phẩm
  <TextView id="productPrice" />  // Giá sản phẩm
  <LinearLayout>  // Container số lượng
    <Button id="btnDecrease" />  // Nút giảm
    <TextView id="tvQuantity" />  // Số lượng
    <Button id="btnIncrease" />  // Nút tăng
  </LinearLayout>
  <TextView id="tvSubtotal" />  // Tổng tiền sản phẩm
  <ImageView id="btnRemove" />  // Nút xóa
</CardView>
```

---

## 3. CÁC THÀNH PHẦN CHÍNH

### 3.1. RecyclerView - Danh Sách Sản Phẩm
- **Component**: `RecyclerView` với `LinearLayoutManager`
- **Adapter**: `CartAdapter` (custom adapter)
- **Chức năng**: 
  - Hiển thị danh sách sản phẩm trong giỏ hàng
  - Cho phép thay đổi số lượng
  - Cho phép xóa sản phẩm

### 3.2. CartItem (Inner Class)
- **Mục đích**: Lưu trữ thông tin sản phẩm và số lượng trong giỏ hàng
- **Cấu trúc**:
  ```java
  public static class CartItem {
      private Product product;
      private int quantity;
  }
  ```

### 3.3. Tổng Tiền
- **Component**: `TextView`
- **Chức năng**: Hiển thị tổng tiền của tất cả sản phẩm trong giỏ hàng
- **Format**: "Tổng tiền: X,XXX đ"

### 3.4. Nút Hành Động
- **Thanh toán**: Chuyển đến `CheckoutActivity` (yêu cầu đăng nhập)
- **Tiếp tục mua sắm**: Quay lại màn hình trước

---

## 4. CODE IMPLEMENTATION CHI TIẾT

### 4.1. CartActivity.java

#### A. Khởi tạo (onCreate)
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_cart);

    initViews();
    loadCartItems();
}
```

#### B. Load Giỏ Hàng
```java
private void loadCartItems() {
    cartItems.clear();
    SharedPreferences prefs = getSharedPreferences("cart_prefs", MODE_PRIVATE);
    
    // Lấy tất cả sản phẩm từ database
    List<Product> allProducts = dbHelper.getAllProducts();
    
    for (Product product : allProducts) {
        String cartKey = "cart_item_" + product.getId();
        int quantity = prefs.getInt(cartKey, 0);
        
        if (quantity > 0) {
            cartItems.add(new CartItem(product, quantity));
        }
    }
    
    adapter.notifyDataSetChanged();
    updateTotalPrice();
    checkEmptyCart();
}
```

#### C. Cập Nhật Tổng Tiền
```java
private void updateTotalPrice() {
    double total = 0;
    for (CartItem item : cartItems) {
        total += item.getProduct().getPrice() * item.getQuantity();
    }
    
    DecimalFormat currencyFormat = new DecimalFormat("#,###");
    tvTotalPrice.setText("Tổng tiền: " + currencyFormat.format(total) + " đ");
}
```

#### D. Kiểm Tra Giỏ Hàng Trống
```java
private void checkEmptyCart() {
    if (cartItems.isEmpty()) {
        tvEmptyCart.setVisibility(View.VISIBLE);
        cartRecyclerView.setVisibility(View.GONE);
        btnCheckout.setEnabled(false);
    } else {
        tvEmptyCart.setVisibility(View.GONE);
        cartRecyclerView.setVisibility(View.VISIBLE);
        btnCheckout.setEnabled(true);
    }
}
```

#### E. Thanh Toán
```java
private void checkout() {
    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
    String loggedUser = prefs.getString("logged_user", null);
    
    if (loggedUser == null) {
        Toast.makeText(this, "Vui lòng đăng nhập để thanh toán", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        return;
    }

    // Tính tổng tiền
    double total = 0;
    for (CartItem item : cartItems) {
        total += item.getProduct().getPrice() * item.getQuantity();
    }

    // Chuyển đến màn hình thanh toán
    Intent intent = new Intent(this, CheckoutActivity.class);
    intent.putExtra("total_price", total);
    
    // Truyền danh sách sản phẩm
    ArrayList<Integer> productIds = new ArrayList<>();
    ArrayList<Integer> quantities = new ArrayList<>();
    for (CartItem item : cartItems) {
        productIds.add(item.getProduct().getId());
        quantities.add(item.getQuantity());
    }
    intent.putIntegerArrayListExtra("product_ids", productIds);
    intent.putIntegerArrayListExtra("quantities", quantities);
    
    startActivity(intent);
}
```

---

## 5. CART ADAPTER

### 5.1. CartAdapter.java

#### A. Interface Callback
```java
public interface OnCartItemChangeListener {
    void onQuantityChanged();  // Khi số lượng thay đổi
    void onItemRemoved(int position);  // Khi xóa sản phẩm
}
```

#### B. ViewHolder
```java
class CartViewHolder extends RecyclerView.ViewHolder {
    ImageView productImage, btnRemove;
    TextView productName, productPrice, tvQuantity, tvSubtotal;
    Button btnDecrease, btnIncrease;
    
    // Bind data và setup click listeners
}
```

#### C. Tăng/Giảm Số Lượng
```java
btnIncrease.setOnClickListener(v -> {
    int currentQty = cartItem.getQuantity();
    if (currentQty < cartItem.getProduct().getQuantity()) {
        cartItem.setQuantity(currentQty + 1);
        updateCartInSharedPreferences(cartItem);
        listener.onQuantityChanged();
    } else {
        Toast.makeText(context, "Số lượng vượt quá tồn kho", Toast.LENGTH_SHORT).show();
    }
});

btnDecrease.setOnClickListener(v -> {
    int currentQty = cartItem.getQuantity();
    if (currentQty > 1) {
        cartItem.setQuantity(currentQty - 1);
        updateCartInSharedPreferences(cartItem);
        listener.onQuantityChanged();
    } else {
        // Xóa sản phẩm nếu số lượng = 0
        listener.onItemRemoved(position);
    }
});
```

#### D. Xóa Sản Phẩm
```java
btnRemove.setOnClickListener(v -> {
    // Xóa khỏi SharedPreferences
    SharedPreferences prefs = context.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    editor.remove("cart_item_" + cartItem.getProduct().getId());
    editor.apply();
    
    listener.onItemRemoved(position);
});
```

---

## 6. CÁC TÍNH NĂNG CHÍNH

### 6.1. Quản Lý Giỏ Hàng
- ✅ Load giỏ hàng từ SharedPreferences
- ✅ Hiển thị danh sách sản phẩm với RecyclerView
- ✅ Tự động cập nhật tổng tiền khi số lượng thay đổi
- ✅ Kiểm tra giỏ hàng trống và hiển thị thông báo

### 6.2. Thay Đổi Số Lượng
- ✅ Tăng số lượng (kiểm tra tồn kho)
- ✅ Giảm số lượng (tối thiểu = 1, nếu = 0 thì xóa)
- ✅ Cập nhật real-time vào SharedPreferences
- ✅ Cập nhật tổng tiền tự động

### 6.3. Xóa Sản Phẩm
- ✅ Xóa sản phẩm khỏi giỏ hàng
- ✅ Cập nhật SharedPreferences
- ✅ Refresh RecyclerView

### 6.4. Thanh Toán
- ✅ Kiểm tra đăng nhập
- ✅ Tính tổng tiền
- ✅ Truyền dữ liệu đến CheckoutActivity
- ✅ Chuyển màn hình

---

## 7. LIFECYCLE VÀ PERFORMANCE

### 7.1. Lifecycle Methods
```java
@Override
protected void onResume() {
    super.onResume();
    loadCartItems();  // Reload khi quay lại (có thể đã thay đổi ở màn hình khác)
}
```

### 7.2. Performance Optimization
- ✅ RecyclerView với ViewHolder pattern
- ✅ Efficient SharedPreferences operations
- ✅ Lazy loading với RecyclerView
- ✅ Update chỉ các item thay đổi (notifyItemChanged)

---

## 8. KẾT LUẬN

Màn hình giỏ hàng được xây dựng với:
- **UI/UX tốt**: Card layout, clear actions
- **Performance tốt**: RecyclerView, efficient updates
- **Tính năng đầy đủ**: Thêm, sửa, xóa, thanh toán
- **Code clean**: Adapter pattern, callback interface

**Công nghệ nổi bật:**
1. RecyclerView với custom adapter
2. SharedPreferences cho persistent storage
3. Callback interface cho communication
4. Intent để truyền dữ liệu

---

## 9. HÌNH ẢNH MINH HỌA (Có thể thêm vào Word)

```
┌─────────────────────────────────┐
│  [← Back]    Giỏ hàng           │
├─────────────────────────────────┤
│  ┌───────────────────────────┐ │
│  │ [Ảnh]  Tên SP             │ │
│  │        17,000 đ           │ │
│  │        [-] 2 [+]          │ │
│  │        Tổng: 34,000 đ     │ │
│  │        [X]                │ │
│  └───────────────────────────┘ │
│  ┌───────────────────────────┐ │
│  │ [Ảnh]  Tên SP 2           │ │
│  │        ...                │ │
│  └───────────────────────────┘ │
├─────────────────────────────────┤
│  Tổng tiền: 51,000 đ           │
│  [Thanh toán]                   │
│  [Tiếp tục mua sắm]            │
└─────────────────────────────────┘
```

---

*Tài liệu này có thể copy vào Word và chỉnh sửa theo nhu cầu trình bày.*

