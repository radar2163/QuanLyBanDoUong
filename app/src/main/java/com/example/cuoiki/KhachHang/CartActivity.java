package com.example.cuoiki.KhachHang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.DuLieu.Product;
import com.example.cuoiki.TienIch.SQLiteHelper;
import com.example.cuoiki.KhachHang.CartAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView cartRecyclerView;
    private TextView tvTotalPrice, tvEmptyCart;
    private Button btnCheckout, btnContinueShopping;
    private android.widget.ImageView btnBack;
    private CartAdapter adapter;
    private SQLiteHelper dbHelper;
    private List<CartItem> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        loadCartItems();
    }

    private void initViews() {
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvEmptyCart = findViewById(R.id.tvEmptyCart);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnContinueShopping = findViewById(R.id.btnContinueShopping);
        btnBack = findViewById(R.id.btnBack);
        
        btnBack.setOnClickListener(v -> finish());

        dbHelper = new SQLiteHelper(this);
        cartItems = new ArrayList<>();

        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(this, cartItems, new CartAdapter.OnCartItemChangeListener() {
            @Override
            public void onQuantityChanged() {
                updateTotalPrice();
            }

            @Override
            public void onItemRemoved(int position) {
                cartItems.remove(position);
                adapter.notifyItemRemoved(position);
                updateTotalPrice();
                checkEmptyCart();
            }
        });
        cartRecyclerView.setAdapter(adapter);

        btnCheckout.setOnClickListener(v -> checkout());
        btnContinueShopping.setOnClickListener(v -> {
            finish();
        });
    }

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

    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        
        DecimalFormat currencyFormat = new DecimalFormat("#,###");
        tvTotalPrice.setText("Tổng tiền: " + currencyFormat.format(total) + " đ");
    }

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

    private void checkout() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String loggedUser = prefs.getString("logged_user", null);
        
        if (loggedUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thanh toán", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, com.example.cuoiki.DangNhap.LoginActivity.class);
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
        
        // Truyền danh sách sản phẩm (product IDs và quantities)
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

    private void clearCart() {
        SharedPreferences prefs = getSharedPreferences("cart_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        
        cartItems.clear();
        adapter.notifyDataSetChanged();
        updateTotalPrice();
        checkEmptyCart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartItems();
    }

    // Inner class để lưu thông tin sản phẩm trong giỏ hàng
    public static class CartItem {
        private Product product;
        private int quantity;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}

