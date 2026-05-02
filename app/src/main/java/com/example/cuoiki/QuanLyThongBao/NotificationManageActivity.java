package com.example.cuoiki.QuanLyThongBao;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.TienIch.SQLiteHelper;
import com.example.cuoiki.TienIch.FirebaseBackupHelper;
import com.example.cuoiki.DuLieu.User;
import com.example.cuoiki.DuLieu.Notification;
import com.example.cuoiki.QuanLyThongBao.UserSelectAdapter;
import com.example.cuoiki.QuanLyThongBao.NotificationHistoryAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationManageActivity extends AppCompatActivity {

    private EditText edtTitle, edtMessage;
    private RadioButton radioAll, radioAllEmployees, radioAllCustomers, radioSelect;
    private LinearLayout optionAll, optionAllEmployees, optionAllCustomers, optionSelect;
    private RecyclerView recyclerViewUsers;
    private Button btnSend, btnHistory;
    private SQLiteHelper dbHelper;
    private UserSelectAdapter userAdapter;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_manage);

        edtTitle = findViewById(R.id.edtTitle);
        edtMessage = findViewById(R.id.edtMessage);
        radioAll = findViewById(R.id.radioAll);
        radioAllEmployees = findViewById(R.id.radioAllEmployees);
        radioAllCustomers = findViewById(R.id.radioAllCustomers);
        radioSelect = findViewById(R.id.radioSelect);
        optionAll = findViewById(R.id.optionAll);
        optionAllEmployees = findViewById(R.id.optionAllEmployees);
        optionAllCustomers = findViewById(R.id.optionAllCustomers);
        optionSelect = findViewById(R.id.optionSelect);
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        btnSend = findViewById(R.id.btnSend);
        btnHistory = findViewById(R.id.btnHistory);

        dbHelper = new SQLiteHelper(this);

        // Lấy username hiện tại
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUsername = prefs.getString("logged_user", "admin");

        // Setup RecyclerView cho danh sách user (tất cả user)
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
        List<User> userList = dbHelper.getAllUsers();
        userAdapter = new UserSelectAdapter(userList);
        recyclerViewUsers.setAdapter(userAdapter);

        // Setup radio button listeners
        optionAll.setOnClickListener(v -> {
            radioAll.setChecked(true);
            radioAllEmployees.setChecked(false);
            radioAllCustomers.setChecked(false);
            radioSelect.setChecked(false);
            recyclerViewUsers.setVisibility(View.GONE);
        });

        optionAllEmployees.setOnClickListener(v -> {
            radioAll.setChecked(false);
            radioAllEmployees.setChecked(true);
            radioAllCustomers.setChecked(false);
            radioSelect.setChecked(false);
            recyclerViewUsers.setVisibility(View.GONE);
        });

        optionAllCustomers.setOnClickListener(v -> {
            radioAll.setChecked(false);
            radioAllEmployees.setChecked(false);
            radioAllCustomers.setChecked(true);
            radioSelect.setChecked(false);
            recyclerViewUsers.setVisibility(View.GONE);
        });

        optionSelect.setOnClickListener(v -> {
            radioAll.setChecked(false);
            radioAllEmployees.setChecked(false);
            radioAllCustomers.setChecked(false);
            radioSelect.setChecked(true);
            recyclerViewUsers.setVisibility(View.VISIBLE);
        });

        radioAll.setOnClickListener(v -> {
            radioAllEmployees.setChecked(false);
            radioAllCustomers.setChecked(false);
            radioSelect.setChecked(false);
            recyclerViewUsers.setVisibility(View.GONE);
        });

        radioAllEmployees.setOnClickListener(v -> {
            radioAll.setChecked(false);
            radioAllCustomers.setChecked(false);
            radioSelect.setChecked(false);
            recyclerViewUsers.setVisibility(View.GONE);
        });

        radioAllCustomers.setOnClickListener(v -> {
            radioAll.setChecked(false);
            radioAllEmployees.setChecked(false);
            radioSelect.setChecked(false);
            recyclerViewUsers.setVisibility(View.GONE);
        });

        radioSelect.setOnClickListener(v -> {
            radioAll.setChecked(false);
            radioAllEmployees.setChecked(false);
            radioAllCustomers.setChecked(false);
            recyclerViewUsers.setVisibility(View.VISIBLE);
        });

        // Send button
        btnSend.setOnClickListener(v -> sendNotification());

        // History button
        btnHistory.setOnClickListener(v -> showHistoryDialog());
    }

    private void showHistoryDialog() {
        // Lấy danh sách thông báo đã gửi
        List<Notification> sentNotifications = dbHelper.getSentNotifications(currentUsername);

        if (sentNotifications.isEmpty()) {
            Toast.makeText(this, "Chưa có thông báo nào được gửi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo dialog để hiển thị lịch sử
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_notification_history);
        dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        RecyclerView recyclerViewHistory = dialog.findViewById(R.id.recyclerViewHistory);
        android.widget.Button btnClose = dialog.findViewById(R.id.btnClose);

        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        NotificationHistoryAdapter historyAdapter = new NotificationHistoryAdapter(sentNotifications, notification -> {
            // Thu hồi thông báo
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("Thu hồi thông báo");
            builder.setMessage("Bạn có chắc chắn muốn thu hồi thông báo này?");
            builder.setPositiveButton("Thu hồi", (d, which) -> {
                dbHelper.deleteNotification(notification.getId());
                Toast.makeText(this, "Đã thu hồi thông báo", Toast.LENGTH_SHORT).show();
                // Backup to Firebase
                FirebaseBackupHelper backupHelper = new FirebaseBackupHelper(this);
                backupHelper.backupAllData(null);
                // Reload history
                showHistoryDialog();
                dialog.dismiss();
            });
            builder.setNegativeButton("Hủy", (d, which) -> d.dismiss());
            builder.show();
        });
        recyclerViewHistory.setAdapter(historyAdapter);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void sendNotification() {
        String title = edtTitle.getText().toString().trim();
        String message = edtMessage.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Vui lòng nhập nội dung", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String createdDate = sdf.format(new Date());

        boolean success = false;

        if (radioAll.isChecked()) {
            // Gửi đến tất cả (nhân viên và khách hàng)
            success = dbHelper.addNotification(title, message, currentUsername, "ALL", createdDate);
        } else if (radioAllEmployees.isChecked()) {
            // Gửi đến tất cả nhân viên
            List<User> allUsers = dbHelper.getAllUsers();
            for (User user : allUsers) {
                String role = user.getRole();
                if (role != null && (role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("employee"))) {
                    dbHelper.addNotification(title, message, currentUsername, user.getUsername(), createdDate);
                }
            }
            success = true;
        } else if (radioAllCustomers.isChecked()) {
            // Gửi đến tất cả khách hàng
            List<User> allUsers = dbHelper.getAllUsers();
            for (User user : allUsers) {
                String role = user.getRole();
                if (role != null && role.equalsIgnoreCase("customer")) {
                    dbHelper.addNotification(title, message, currentUsername, user.getUsername(), createdDate);
                }
            }
            success = true;
        } else {
            // Gửi đến các user đã chọn
            List<String> selectedUsernames = userAdapter.getSelectedUsernames();
            if (selectedUsernames.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất một người nhận", Toast.LENGTH_SHORT).show();
                return;
            }

            for (String username : selectedUsernames) {
                dbHelper.addNotification(title, message, currentUsername, username, createdDate);
            }
            success = true;
        }

        if (success) {
            Toast.makeText(this, "Gửi thông báo thành công", Toast.LENGTH_SHORT).show();
            // Backup to Firebase
            FirebaseBackupHelper backupHelper = new FirebaseBackupHelper(this);
            backupHelper.backupAllData(null);
            finish();
        } else {
            Toast.makeText(this, "Gửi thông báo thất bại", Toast.LENGTH_SHORT).show();
        }
    }
}

