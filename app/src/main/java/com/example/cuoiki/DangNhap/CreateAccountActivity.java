package com.example.cuoiki.DangNhap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cuoiki.R;
import com.example.cuoiki.ManHinhChinh.MainActivity;
import com.example.cuoiki.TienIch.SQLiteHelper;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText, gmailEditText, phoneEditText, fullNameEditText;
    private Button create, close;
    private SQLiteHelper dbHelper; // SQLiteHelper class để quản lý database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Ánh xạ các view
        usernameEditText = findViewById(R.id.name);
        passwordEditText = findViewById(R.id.pass);
        gmailEditText = findViewById(R.id.gmail);
        phoneEditText = findViewById(R.id.phone_number);
        fullNameEditText = findViewById(R.id.full_name);
        create = findViewById(R.id.create_account_button);
        close = findViewById(R.id.close_button);

        dbHelper = new SQLiteHelper(this); // Khởi tạo đối tượng SQLiteHelper

        // Lắng nghe sự kiện click nút tạo tài khoản
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String gmail = gmailEditText.getText().toString().trim();
                String phone = phoneEditText.getText().toString().trim();
                String fullName = fullNameEditText.getText().toString().trim();

                // Kiểm tra dữ liệu nhập vào
                if (username.isEmpty()) {
                    usernameEditText.setError("Tên đăng nhập không được để trống");
                    usernameEditText.requestFocus();
                    return;
                }

                if (password.isEmpty()) {
                    passwordEditText.setError("Mật khẩu không được để trống");
                    passwordEditText.requestFocus();
                    return;
                }

                if (gmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(gmail).matches()) {
                    gmailEditText.setError("Email không hợp lệ");
                    gmailEditText.requestFocus();
                    return;
                }

                if (phone.isEmpty() || !phone.matches("\\d{10,11}")) {
                    phoneEditText.setError("Số điện thoại không hợp lệ");
                    phoneEditText.requestFocus();
                    return;
                }

                if (fullName.isEmpty()) {
                    fullNameEditText.setError("Họ tên không được để trống");
                    fullNameEditText.requestFocus();
                    return;
                }

                // Thêm tài khoản vào database với role mặc định là "customer" (khách hàng)
                boolean isInserted = dbHelper.insertAccount(username, password, gmail, phone, fullName, "customer");

                if (isInserted) {
                    Toast.makeText(CreateAccountActivity.this, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();

                    //Lưu thông tin người dùng mới vào SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("logged_user", username);
                    editor.putString("user_role", "customer"); // Mặc định là khách hàng
                    editor.putString("full_name", fullName);
                    editor.putString("phone_number", phone);
                    editor.putString("avatar_uri", null); // ảnh mặc định
                    editor.apply();

                    // Mở thẳng CustomerMainActivity vì tài khoản mới là khách hàng
                    Intent intent = new Intent(CreateAccountActivity.this, com.example.cuoiki.KhachHang.CustomerMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CreateAccountActivity.this, "Không thể tạo tài khoản, hãy thử lại sau!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Xử lý nút thoát
        close.setOnClickListener(v -> finish());
    }
}
