package com.example.cuoiki.KhachHang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cuoiki.R;

public class OrderSuccessActivity extends AppCompatActivity {

    private Button btnContinueShopping;
    private Button btnViewOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnContinueShopping = findViewById(R.id.btnContinueShopping);
        btnViewOrders = findViewById(R.id.btnViewOrders);
    }

    private void setupClickListeners() {
        // Nút xem đơn hàng
        btnViewOrders.setOnClickListener(v -> {
            // Chuyển đến màn hình xem đơn hàng
            Intent intent = new Intent(this, OrderHistoryActivity.class);
            startActivity(intent);
            finish();
        });
        
        // Nút tiếp tục mua sắm
        btnContinueShopping.setOnClickListener(v -> {
            // Quay về màn hình chính
            Intent intent = new Intent(this, CustomerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}

