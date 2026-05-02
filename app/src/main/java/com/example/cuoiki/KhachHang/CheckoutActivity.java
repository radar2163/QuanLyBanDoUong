package com.example.cuoiki.KhachHang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cuoiki.R;
import com.example.cuoiki.DuLieu.Product;
import com.example.cuoiki.DuLieu.Order;
import com.example.cuoiki.TienIch.SQLiteHelper;
import com.example.cuoiki.TienIch.EmailHelper;
import com.example.cuoiki.TienIch.VNPayHelper;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private TextView tvFullName, tvOrderDate, tvPhoneNumber, tvTotalPrice;
    private EditText edtAddress;
    private CardView cardCOD, cardQR;
    private ImageView ivCODSelected, ivQRSelected, btnBack;
    private Button btnConfirmOrder;
    private RecyclerView recyclerOrderItems;
    
    private String selectedPaymentMethod = "COD"; // Mặc định là COD
    private double totalPrice = 0;
    private SQLiteHelper dbHelper;
    private List<OrderItem> orderItems;
    private OrderItemAdapter orderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_checkout);

            // Lấy tổng tiền từ Intent
            totalPrice = getIntent().getDoubleExtra("total_price", 0);
            
            dbHelper = new SQLiteHelper(this);
            orderItems = new ArrayList<>();

            initViews();
            loadUserInfo();
            loadOrderItems(); // Load sau khi initViews để đảm bảo adapter đã được khởi tạo
            setupPaymentMethodSelection();
            setupConfirmButton();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        try {
            tvFullName = findViewById(R.id.tvFullName);
            tvOrderDate = findViewById(R.id.tvOrderDate);
            tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
            tvTotalPrice = findViewById(R.id.tvTotalPrice);
            edtAddress = findViewById(R.id.edtAddress);
            cardCOD = findViewById(R.id.cardCOD);
            cardQR = findViewById(R.id.cardQR);
            ivCODSelected = findViewById(R.id.ivCODSelected);
            ivQRSelected = findViewById(R.id.ivQRSelected);
            btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
            btnBack = findViewById(R.id.btnBack);
            recyclerOrderItems = findViewById(R.id.recyclerOrderItems);

            if (btnBack != null) {
                btnBack.setOnClickListener(v -> finish());
            }

            // Hiển thị tổng tiền
            if (tvTotalPrice != null) {
                DecimalFormat currencyFormat = new DecimalFormat("#,###");
                tvTotalPrice.setText("Tổng tiền: " + currencyFormat.format(totalPrice) + " đ");
            }
            
            // Setup RecyclerView cho đơn hàng
            if (recyclerOrderItems != null) {
                recyclerOrderItems.setLayoutManager(new LinearLayoutManager(this));
                orderAdapter = new OrderItemAdapter(orderItems);
                recyclerOrderItems.setAdapter(orderAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khởi tạo giao diện: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void loadOrderItems() {
        try {
            ArrayList<Integer> productIds = getIntent().getIntegerArrayListExtra("product_ids");
            ArrayList<Integer> quantities = getIntent().getIntegerArrayListExtra("quantities");
            
            if (productIds == null || quantities == null) {
                // Nếu không có dữ liệu từ Intent, thử load từ SharedPreferences
                loadOrderItemsFromSharedPreferences();
                return;
            }
            
            if (productIds.size() == quantities.size() && productIds.size() > 0) {
                orderItems.clear();
                for (int i = 0; i < productIds.size(); i++) {
                    Product product = dbHelper.getProductById(productIds.get(i));
                    if (product != null) {
                        orderItems.add(new OrderItem(product, quantities.get(i)));
                    } else {
                        // Log để debug
                        android.util.Log.e("CheckoutActivity", "Không tìm thấy sản phẩm với ID: " + productIds.get(i));
                    }
                }
                if (orderAdapter != null) {
                    orderAdapter.notifyDataSetChanged();
                }
            } else {
                // Nếu không có dữ liệu từ Intent, thử load từ SharedPreferences
                loadOrderItemsFromSharedPreferences();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Nếu có lỗi, thử load từ SharedPreferences
            loadOrderItemsFromSharedPreferences();
        }
    }
    
    private void loadOrderItemsFromSharedPreferences() {
        try {
            SharedPreferences prefs = getSharedPreferences("cart_prefs", MODE_PRIVATE);
            List<Product> allProducts = dbHelper.getAllProducts();
            
            orderItems.clear();
            for (Product product : allProducts) {
                String cartKey = "cart_item_" + product.getId();
                int quantity = prefs.getInt(cartKey, 0);
                
                if (quantity > 0) {
                    orderItems.add(new OrderItem(product, quantity));
                }
            }
            
            // Đảm bảo adapter được cập nhật
            if (orderAdapter == null && recyclerOrderItems != null) {
                orderAdapter = new OrderItemAdapter(orderItems);
                recyclerOrderItems.setAdapter(orderAdapter);
            } else if (orderAdapter != null) {
                orderAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi load đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

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

        // Hiển thị ngày tháng năm và giờ phút hiện tại
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        tvOrderDate.setText(currentDate);
    }

    private void setupPaymentMethodSelection() {
        try {
            if (cardCOD != null) {
                cardCOD.setOnClickListener(v -> {
                    selectedPaymentMethod = "COD";
                    updatePaymentMethodUI();
                });
            }

            if (cardQR != null) {
                cardQR.setOnClickListener(v -> {
                    selectedPaymentMethod = "QR";
                    updatePaymentMethodUI();
                });
            }

            // Mặc định chọn COD
            updatePaymentMethodUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePaymentMethodUI() {
        try {
            if (ivCODSelected == null || ivQRSelected == null) {
                return; // Views chưa được khởi tạo
            }

            if ("COD".equals(selectedPaymentMethod)) {
                // COD được chọn - chỉ thay đổi icon
                ivCODSelected.setVisibility(View.VISIBLE);
                ivQRSelected.setVisibility(View.GONE);
            } else {
                // QR được chọn
                ivQRSelected.setVisibility(View.VISIBLE);
                ivCODSelected.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupConfirmButton() {
        if (btnConfirmOrder != null) {
            btnConfirmOrder.setOnClickListener(v -> {
                try {
                    String address = "";
                    if (edtAddress != null) {
                        address = edtAddress.getText().toString().trim();
                    }
                    
                    if (address.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Xử lý đặt hàng
                    processOrder(address);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

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
        
        // Set payment method dựa trên phương thức thanh toán đã chọn
        if ("QR".equals(selectedPaymentMethod)) {
            order.setPaymentMethod("Thanh toán qua VNPay");
        } else {
            order.setPaymentMethod("Thanh toán khi nhận hàng");
        }

        // Lấy danh sách sản phẩm trong đơn hàng
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

        // Lưu đơn hàng vào database
        long orderId = dbHelper.insertOrder(order);
        
        if (orderId <= 0) {
            android.util.Log.e("CheckoutActivity", "Failed to insert order - orderId: " + orderId);
            Toast.makeText(this, "Lỗi: Không thể tạo đơn hàng. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            return;
        }
        
        order.setId((int) orderId);
        android.util.Log.d("CheckoutActivity", "Order created successfully - OrderId: " + orderId);

        // Xử lý theo phương thức thanh toán
        if ("QR".equals(selectedPaymentMethod)) {
            // Thanh toán qua VNPay
            // Payment method đã được set ở trên, không cần update lại
            processVNPayPayment(order, orderId);
        } else {
            // Thanh toán COD
            // Payment method đã được set ở trên, không cần update lại
            
            // Gửi email xác nhận đơn hàng
            EmailHelper.sendOrderConfirmationEmail(this, order);

            // Xóa giỏ hàng sau khi đặt hàng thành công
            clearCart();

            // Chuyển đến màn hình xác nhận thành công
            Intent intent = new Intent(this, OrderSuccessActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
    
    /**
     * Xử lý thanh toán VNPay
     */
    private void processVNPayPayment(Order order, long orderId) {
        try {
            android.util.Log.d("CheckoutActivity", "Starting VNPay payment - OrderId: " + orderId + ", Amount: " + totalPrice);
            
            // Tạo payment URL
            String orderInfo = "Thanh toan don hang #" + orderId;
            String ipAddress = VNPayHelper.getIpAddress();
            long amount = (long) totalPrice;
            
            android.util.Log.d("CheckoutActivity", "Creating payment URL - OrderInfo: " + orderInfo + ", IP: " + ipAddress + ", Amount: " + amount);
            
            String paymentUrl = VNPayHelper.createPaymentUrl(
                String.valueOf(orderId),
                amount,
                orderInfo,
                ipAddress
            );
            
            if (paymentUrl == null || paymentUrl.isEmpty()) {
                android.util.Log.e("CheckoutActivity", "Payment URL is null or empty");
                Toast.makeText(this, "Lỗi: Không thể tạo URL thanh toán. Vui lòng kiểm tra cấu hình VNPay.", Toast.LENGTH_LONG).show();
                return;
            }
            
            android.util.Log.d("CheckoutActivity", "Payment URL created successfully, opening VNPayPaymentActivity");
            
            // Chuyển đến màn hình thanh toán VNPay
            Intent intent = new Intent(this, VNPayPaymentActivity.class);
            intent.putExtra("payment_url", paymentUrl);
            intent.putExtra("order_id", String.valueOf(orderId));
            intent.putExtra("order_id_long", orderId);
            
            startActivityForResult(intent, 1001);
            
            android.util.Log.d("CheckoutActivity", "VNPayPaymentActivity started");
            
        } catch (Exception e) {
            android.util.Log.e("CheckoutActivity", "Error in processVNPayPayment: " + e.getMessage(), e);
            e.printStackTrace();
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
                
                android.util.Log.d("CheckoutActivity", "VNPay payment result - Status: " + paymentStatus + ", OrderId: " + orderId);
                
                if ("success".equals(paymentStatus) && orderId > 0) {
                    // Cập nhật đơn hàng
                    Order order = dbHelper.getOrderById((int) orderId);
                    if (order != null) {
                        order.setPaymentMethod("Thanh toán qua VNPay");
                        order.setStatus("Đã thanh toán");
                        dbHelper.updateOrder(order);
                        
                        // Gửi email xác nhận đơn hàng
                        EmailHelper.sendOrderConfirmationEmail(this, order);
                    }
                    
                    // Xóa giỏ hàng
                    clearCart();
                    
                    // Chuyển đến màn hình xác nhận thành công (giống COD)
                    Intent intent = new Intent(this, OrderSuccessActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    android.util.Log.e("CheckoutActivity", "Invalid payment result - Status: " + paymentStatus + ", OrderId: " + orderId);
                    // Nếu payment_status không phải "success" nhưng có orderId, vẫn có thể là thành công
                    // (trường hợp hash không khớp nhưng thanh toán thực sự thành công)
                    if (orderId > 0) {
                        android.util.Log.w("CheckoutActivity", "Payment status not 'success' but has orderId, checking order...");
                        Order order = dbHelper.getOrderById((int) orderId);
                        if (order != null) {
                            // Kiểm tra xem đơn hàng đã được thanh toán chưa
                            if (!"Đã thanh toán".equals(order.getStatus())) {
                                // Cập nhật đơn hàng và chuyển màn hình
                                order.setPaymentMethod("Thanh toán qua VNPay");
                                order.setStatus("Đã thanh toán");
                                dbHelper.updateOrder(order);
                                
                                EmailHelper.sendOrderConfirmationEmail(this, order);
                                clearCart();
                                
                                Intent intent = new Intent(this, OrderSuccessActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                                return;
                            }
                        }
                    }
                    Toast.makeText(this, "Lỗi: Không thể xử lý kết quả thanh toán", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Thanh toán thất bại hoặc bị hủy
                String errorMessage = "Thanh toán bị hủy";
                long orderId = -1;
                if (data != null) {
                    errorMessage = data.getStringExtra("error_message");
                    if (errorMessage == null || errorMessage.isEmpty()) {
                        errorMessage = "Thanh toán bị hủy";
                    }
                    orderId = data.getLongExtra("order_id_long", -1);
                }
                android.util.Log.d("CheckoutActivity", "VNPay payment cancelled or failed: " + errorMessage + ", OrderId: " + orderId);
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void clearCart() {
        SharedPreferences prefs = getSharedPreferences("cart_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
    
    // Inner class để lưu thông tin sản phẩm trong đơn hàng
    public static class OrderItem {
        private Product product;
        private int quantity;

        public OrderItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }
    }
    
    // Adapter cho danh sách sản phẩm trong đơn hàng
    private class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {
        private List<OrderItem> items;
        private final DecimalFormat currencyFormat = new DecimalFormat("#,###");

        public OrderItemAdapter(List<OrderItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_checkout, parent, false);
            return new OrderItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
            OrderItem item = items.get(position);
            Product product = item.getProduct();
            
            holder.productName.setText(product.getName());
            holder.productUnit.setText(product.getInit());
            holder.productPrice.setText(currencyFormat.format(product.getPrice()) + " đ");
            holder.productQuantity.setText("x" + item.getQuantity());
            
            // Load ảnh sản phẩm
            if (product.getImageUri() != null && !product.getImageUri().isEmpty()) {
                try {
                    Glide.with(holder.itemView.getContext())
                            .load(Uri.parse(product.getImageUri()))
                            .placeholder(R.drawable.no_image)
                            .error(R.drawable.no_image)
                            .into(holder.productImage);
                } catch (Exception e) {
                    holder.productImage.setImageResource(R.drawable.no_image);
                }
            } else if (product.getImageResId() != 0) {
                holder.productImage.setImageResource(product.getImageResId());
            } else {
                holder.productImage.setImageResource(R.drawable.no_image);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class OrderItemViewHolder extends RecyclerView.ViewHolder {
            ImageView productImage;
            TextView productName, productUnit, productPrice, productQuantity;

            public OrderItemViewHolder(@NonNull View itemView) {
                super(itemView);
                productImage = itemView.findViewById(R.id.productImage);
                productName = itemView.findViewById(R.id.productName);
                productUnit = itemView.findViewById(R.id.productUnit);
                productPrice = itemView.findViewById(R.id.productPriceText);
                productQuantity = itemView.findViewById(R.id.productQuantity);
            }
        }
    }
}
