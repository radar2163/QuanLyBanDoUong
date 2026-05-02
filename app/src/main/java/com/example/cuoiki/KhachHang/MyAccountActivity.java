package com.example.cuoiki.KhachHang;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cuoiki.DuLieu.User;
import com.example.cuoiki.R;
import com.example.cuoiki.TienIch.SQLiteHelper;

public class MyAccountActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etFullName, etPhoneNumber, etEmail, etUsername, etOldPassword, etPassword;
    private Button btnSave;
    private SQLiteHelper dbHelper;
    private String currentUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        initViews();
        loadUserInfo();
        setupClickListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etFullName = findViewById(R.id.etFullName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etOldPassword = findViewById(R.id.etOldPassword);
        etPassword = findViewById(R.id.etPassword);
        btnSave = findViewById(R.id.btnSave);
        
        dbHelper = new SQLiteHelper(this);
    }

    private void loadUserInfo() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUsername = prefs.getString("logged_user", null);
        
        if (currentUsername == null || currentUsername.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Lấy thông tin từ database
        User user = dbHelper.getUserByUsername(currentUsername);
        
        if (user != null) {
            etFullName.setText(user.getFull_name());
            etPhoneNumber.setText(user.getPhone_number());
            etEmail.setText(user.getGmail());
            etUsername.setText(user.getUsername());
            // Không hiển thị mật khẩu cũ
        } else {
            // Fallback: lấy từ SharedPreferences
            String fullName = prefs.getString("full_name", "");
            String phoneNumber = prefs.getString("phone_number", "");
            
            etFullName.setText(fullName);
            etPhoneNumber.setText(phoneNumber);
            etUsername.setText(currentUsername);
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> saveUserInfo());
    }

    private void saveUserInfo() {
        String fullName = etFullName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String oldPassword = etOldPassword.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (fullName.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ và tên");
            etFullName.requestFocus();
            return;
        }

        if (phoneNumber.isEmpty()) {
            etPhoneNumber.setError("Vui lòng nhập số điện thoại");
            etPhoneNumber.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return;
        }

        try {
            // Lấy thông tin user hiện tại
            User currentUser = dbHelper.getUserByUsername(currentUsername);
            
            if (currentUser == null) {
                Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra nếu muốn đổi mật khẩu
            if (!password.isEmpty()) {
                // Phải nhập mật khẩu cũ
                if (oldPassword.isEmpty()) {
                    etOldPassword.setError("Vui lòng nhập mật khẩu cũ");
                    etOldPassword.requestFocus();
                    return;
                }

                // Kiểm tra mật khẩu cũ có đúng không
                if (!oldPassword.equals(currentUser.getPassword())) {
                    etOldPassword.setError("Mật khẩu cũ không đúng");
                    etOldPassword.requestFocus();
                    return;
                }

                // Kiểm tra độ dài mật khẩu mới
                if (password.length() < 6) {
                    etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                    etPassword.requestFocus();
                    return;
                }

                // Cập nhật mật khẩu mới
                currentUser.setPassword(password);
            }

            // Cập nhật thông tin
            currentUser.setFull_name(fullName);
            currentUser.setPhone_number(phoneNumber);
            currentUser.setGmail(email);

            // Lưu vào database
            dbHelper.updateUser(currentUser);

            // Cập nhật SharedPreferences
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("full_name", fullName);
            editor.putString("phone_number", phoneNumber);
            editor.apply();

            Toast.makeText(this, "Đã cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
            
            // Xóa mật khẩu khỏi EditText nếu đã lưu
            if (!password.isEmpty()) {
                etOldPassword.setText("");
                etPassword.setText("");
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
