package com.example.cuoiki.DangNhap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cuoiki.R;
import com.example.cuoiki.ManHinhChinh.MainActivity;
import com.example.cuoiki.DangNhap.CreateAccountActivity;
import com.example.cuoiki.TienIch.SQLiteHelper;
import com.example.cuoiki.DuLieu.User;

public class LoginActivity extends AppCompatActivity {
    private Button buttonLogin, buttonRegister;
    private EditText edtUser, edtPass;
    private ImageView ivTogglePassword;
    private CheckBox chkRemember;
    private TextView tvForgot;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        buttonLogin = findViewById(R.id.button);
        buttonRegister = findViewById(R.id.button2);
        edtUser = findViewById(R.id.editTextText);
        edtPass = findViewById(R.id.editTextText2);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        chkRemember = findViewById(R.id.chkRemember);
        tvForgot = findViewById(R.id.tvForgot);

        //Đảm bảo mật khẩu được ẩn mặc định - hiển thị dấu chấm ngay khi nhập
        // Không override inputType từ XML, chỉ đảm bảo TransformationMethod được set
        android.text.method.PasswordTransformationMethod passwordMethod = android.text.method.PasswordTransformationMethod.getInstance();
        edtPass.setTransformationMethod(passwordMethod);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

        //  Nạp lại thông tin nếu có
        String savedUser = prefs.getString("saved_user", "");
        String savedPass = prefs.getString("saved_pass", "");
        boolean remember = prefs.getBoolean("remember_login", false);

        if (remember) {
            edtUser.setText(savedUser);
            //Đảm bảo TransformationMethod được set trước khi set text
            edtPass.setTransformationMethod(passwordMethod);
            edtPass.setText(savedPass);
            chkRemember.setChecked(true);
        }

        // Toggle mật khẩu
        ivTogglePassword.setOnClickListener(v -> {
            int selection = edtPass.getSelectionStart(); // Lưu vị trí con trỏ
            if (isPasswordVisible) {
                // Ẩn mật khẩu - set TransformationMethod trước
                edtPass.setTransformationMethod(passwordMethod);
                edtPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_eye_off);
                isPasswordVisible = false;
            } else {
                // Hiện mật khẩu
                edtPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                edtPass.setTransformationMethod(null);
                ivTogglePassword.setImageResource(R.drawable.ic_eye);
                isPasswordVisible = true;
            }
            // Khôi phục vị trí con trỏ
            if (selection >= 0 && selection <= edtPass.length()) {
                edtPass.setSelection(selection);
            } else {
                edtPass.setSelection(edtPass.length());
            }
        });

        //  Đăng nhập
        buttonLogin.setOnClickListener(v -> {
            String username = edtUser.getText().toString().trim();
            String password = edtPass.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteHelper dbHelper = new SQLiteHelper(this);
            if (dbHelper.checkAccount(username, password)) {
                //  Lấy thông tin đầy đủ của user từ database
                User user = dbHelper.getUserByUsername(username);
                String role = user != null ? user.getRole() : dbHelper.getUserRole(username);
                String fullName = user != null ? user.getFull_name() : "User";
                String phoneNumber = user != null ? user.getPhone_number() : "";
                String email = user != null ? user.getGmail() : "";

                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("logged_user", username);
                editor.putString("user_role", role);
                editor.putString("full_name", fullName); //  Lưu họ tên
                editor.putString("phone_number", phoneNumber); //  Lưu số điện thoại
                editor.putString("user_email", email); //  Lưu email

                // Ghi nhớ nếu có tick
                if (chkRemember.isChecked()) {
                    editor.putString("saved_user", username);
                    editor.putString("saved_pass", password);
                    editor.putBoolean("remember_login", true);
                } else {
                    editor.remove("saved_user");
                    editor.remove("saved_pass");
                    editor.putBoolean("remember_login", false);
                }

                editor.apply();

                // Chuyển màn hình dựa theo quyền
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                
                // Kiểm tra role để chuyển đến màn hình phù hợp
                if (role != null && ("Admin".equalsIgnoreCase(role) || "Nhân viên".equalsIgnoreCase(role))) {
                    // Admin hoặc Nhân viên → chuyển đến MainActivity (giao diện quản lý)
                    Intent intent = new Intent(this, com.example.cuoiki.ManHinhChinh.MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    // Khách hàng (User hoặc bất kỳ role nào khác) → chuyển đến CustomerMainActivity (giao diện khách hàng)
                    Intent intent = new Intent(this, com.example.cuoiki.KhachHang.CustomerMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                finish();

                // Đăng nhập thành công
            } else {
                Toast.makeText(this, "Sai tên đăng nhập hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            }
        });

        //  Nút đăng ký
        buttonRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, com.example.cuoiki.DangNhap.CreateAccountActivity.class));
        });

        //  Quên mật khẩu
        tvForgot.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Quên mật khẩu");
            builder.setMessage("Nhập Gmail của bạn để đặt lại mật khẩu.");

            final EditText input = new EditText(this);
            input.setHint("Nhập Gmail");
            builder.setView(input);

            builder.setPositiveButton("Gửi", (dialog, which) -> {
                String email = input.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(this, "Vui lòng nhập Gmail", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Email khôi phục sẽ được gửi đến: " + email, Toast.LENGTH_LONG).show();
                }
            });
            builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }
}
