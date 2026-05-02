package com.example.cuoiki.ThongBao;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.TienIch.SQLiteHelper;
import com.example.cuoiki.DuLieu.Notification;
import com.example.cuoiki.ThongBao.NotificationAdapter;

import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerViewNotifications;
    private NotificationAdapter adapter;
    private SQLiteHelper dbHelper;
    private TextView tvEmpty;
    private String currentUsername;
    private Button btnFilterAll, btnFilterUnread, btnFilterRead;
    private String currentFilter = "all"; // "all", "unread", "read"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterUnread = findViewById(R.id.btnFilterUnread);
        btnFilterRead = findViewById(R.id.btnFilterRead);

        dbHelper = new SQLiteHelper(this);

        // Lấy username hiện tại
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUsername = prefs.getString("logged_user", "admin");

        // Setup RecyclerView
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));

        // Setup adapter với click listener (khởi tạo với list rỗng trước)
        adapter = new NotificationAdapter(new java.util.ArrayList<>(), notification -> {
            // Đánh dấu đã đọc
            if (!notification.isRead()) {
                dbHelper.markNotificationAsRead(notification.getId());
                loadNotifications(); // Reload
            }
        });
        recyclerViewNotifications.setAdapter(adapter);

        // Setup filter buttons
        setupFilters();

        // Load notifications sau khi adapter đã được set
        loadNotifications();

        // Setup Bottom Navigation
        setupBottomNavigation();
    }

    private void setupFilters() {
        btnFilterAll.setOnClickListener(v -> {
            currentFilter = "all";
            updateFilterButtons();
            loadNotifications();
        });

        btnFilterUnread.setOnClickListener(v -> {
            currentFilter = "unread";
            updateFilterButtons();
            loadNotifications();
        });

        btnFilterRead.setOnClickListener(v -> {
            currentFilter = "read";
            updateFilterButtons();
            loadNotifications();
        });

        // Set initial state
        updateFilterButtons();
    }

    private void updateFilterButtons() {
        // Reset all buttons to default color
        int defaultColor = 0xFF37386e; // #37386e
        int selectedColor = 0xFF5E17EB; // #5E17EB

        switch (currentFilter) {
            case "all":
                btnFilterAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedColor));
                btnFilterUnread.setBackgroundTintList(android.content.res.ColorStateList.valueOf(defaultColor));
                btnFilterRead.setBackgroundTintList(android.content.res.ColorStateList.valueOf(defaultColor));
                break;
            case "unread":
                btnFilterAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf(defaultColor));
                btnFilterUnread.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedColor));
                btnFilterRead.setBackgroundTintList(android.content.res.ColorStateList.valueOf(defaultColor));
                break;
            case "read":
                btnFilterAll.setBackgroundTintList(android.content.res.ColorStateList.valueOf(defaultColor));
                btnFilterUnread.setBackgroundTintList(android.content.res.ColorStateList.valueOf(defaultColor));
                btnFilterRead.setBackgroundTintList(android.content.res.ColorStateList.valueOf(selectedColor));
                break;
        }
    }

    private void loadNotifications() {
        try {
            List<Notification> notifications;
            
            // Load notifications based on filter
            switch (currentFilter) {
                case "unread":
                    notifications = dbHelper.getNotificationsByReadStatus(currentUsername, false);
                    break;
                case "read":
                    notifications = dbHelper.getNotificationsByReadStatus(currentUsername, true);
                    break;
                default: // "all"
                    notifications = dbHelper.getNotifications(currentUsername);
                    break;
            }
            
            if (notifications == null || notifications.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                recyclerViewNotifications.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                recyclerViewNotifications.setVisibility(View.VISIBLE);
                if (adapter != null) {
                    adapter.updateList(notifications);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerViewNotifications.setVisibility(View.GONE);
        }
    }

    private void setupBottomNavigation() {
        //  Ẩn nút thống kê cho nhân viên
        setupRoleBasedUI();

        findViewById(R.id.btm_home).setOnClickListener(v -> {
            startActivity(new Intent(NotificationActivity.this, com.example.cuoiki.ManHinhChinh.MainActivity.class));
            finish();
        });
        findViewById(R.id.btm_profile).setOnClickListener(v -> {
            startActivity(new Intent(NotificationActivity.this, com.example.cuoiki.HoSo.ProfileActivity.class));
            finish();
        });
        findViewById(R.id.btm_search).setOnClickListener(v -> {
            startActivity(new Intent(NotificationActivity.this, com.example.cuoiki.TimKiem.SearchActivity.class));
            finish();
        });
        findViewById(R.id.btm_static).setOnClickListener(v -> {
            startActivity(new Intent(NotificationActivity.this, com.example.cuoiki.ThongKe.StaticActivity.class));
            finish();
        });
    }

    //  Thiết lập giao diện theo quyền người dùng
    private void setupRoleBasedUI() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userRole = prefs.getString("user_role", "admin");
        
        // Ẩn nút thống kê cho nhân viên
        if (!"admin".equalsIgnoreCase(userRole)) {
            findViewById(R.id.btm_static).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }
}

