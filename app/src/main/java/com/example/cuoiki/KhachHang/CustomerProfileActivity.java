package com.example.cuoiki.KhachHang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.cuoiki.R;
import com.example.cuoiki.DangNhap.LoginActivity;
import android.content.Intent;

public class CustomerProfileActivity extends AppCompatActivity {

    private TextView tvUserName;
    private ImageView btnClose;
    private CardView cardOrderHistory, cardNotification, cardMyAccount, cardLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        initViews();
        loadUserInfo();
        setupClickListeners();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        btnClose = findViewById(R.id.btnClose);
        cardOrderHistory = findViewById(R.id.cardOrderHistory);
        cardNotification = findViewById(R.id.cardNotification);
        cardMyAccount = findViewById(R.id.cardMyAccount);
        cardLogout = findViewById(R.id.cardLogout);
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String fullName = prefs.getString("full_name", null);
        
        if (fullName != null && !fullName.isEmpty()) {
            tvUserName.setText(fullName);
        } else {
            String loggedUser = prefs.getString("logged_user", "Khách hàng");
            tvUserName.setText(loggedUser);
        }
    }

    private void setupClickListeners() {
        // Nút đóng
        btnClose.setOnClickListener(v -> finish());

        // Lịch sử đặt hàng
        cardOrderHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        // Thông báo
        cardNotification.setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomerNotificationActivity.class);
            startActivity(intent);
        });

        // Tài khoản của tôi
        cardMyAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyAccountActivity.class);
            startActivity(intent);
        });

        // Đăng xuất
        cardLogout.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            
            // Xóa thông tin đăng nhập
            editor.remove("logged_user");
            editor.remove("user_role");
            editor.remove("full_name");
            editor.remove("phone_number");
            editor.apply();
            
            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            
            // Quay về màn hình khách hàng (viewer mode)
            Intent intent = new Intent(this, CustomerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}

