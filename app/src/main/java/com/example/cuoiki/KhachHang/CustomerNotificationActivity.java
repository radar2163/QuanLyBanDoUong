package com.example.cuoiki.KhachHang;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.TienIch.SQLiteHelper;
import com.example.cuoiki.DuLieu.Notification;
import com.example.cuoiki.ThongBao.NotificationAdapter;

import java.util.List;

public class CustomerNotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerViewNotifications;
    private NotificationAdapter adapter;
    private SQLiteHelper dbHelper;
    private TextView tvEmpty;
    private String currentUsername;
    private Button btnFilterAll, btnFilterUnread, btnFilterRead;
    private ImageView btnBack;
    private String currentFilter = "all"; // "all", "unread", "read"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_notification);

        initViews();
        setupRecyclerView();
        setupFilters();
        loadNotifications();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnFilterAll = findViewById(R.id.btnFilterAll);
        btnFilterUnread = findViewById(R.id.btnFilterUnread);
        btnFilterRead = findViewById(R.id.btnFilterRead);

        dbHelper = new SQLiteHelper(this);

        // Lấy username hiện tại
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUsername = prefs.getString("logged_user", null);

        if (currentUsername == null || currentUsername.isEmpty()) {
            // Nếu chưa đăng nhập, dùng "ALL" để xem thông báo công khai
            currentUsername = "ALL";
        }

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));

        // Setup adapter với click listener
        adapter = new NotificationAdapter(new java.util.ArrayList<>(), notification -> {
            // Đánh dấu đã đọc
            if (!notification.isRead()) {
                dbHelper.markNotificationAsRead(notification.getId());
                loadNotifications(); // Reload
            }
        });
        recyclerViewNotifications.setAdapter(adapter);
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
        int defaultColor = 0xFF9E9E9E; // Gray
        int selectedColor = 0xFF1976D2; // Blue

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

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }
}

