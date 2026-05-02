package com.example.cuoiki.HoSo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cuoiki.R;
import com.example.cuoiki.ManHinhChinh.MainActivity;
import com.example.cuoiki.DangNhap.LoginActivity;
import com.example.cuoiki.TimKiem.SearchActivity;
import com.example.cuoiki.ThongKe.StaticActivity;
import com.example.cuoiki.TienIch.SQLiteHelper;
import com.example.cuoiki.TienIch.FirebaseBackupHelper;
import com.example.cuoiki.DuLieu.User;

public class ProfileActivity extends AppCompatActivity {

    private EditText fullName, phoneNumber, role;
    private TextView username, email;
    private Button btnEditProfile, btnLogout;
    private ImageView imageView11; // ảnh đại diện
    private ImageView btnChangeAvatar; // Nút chọn ảnh

    private boolean isEditing = false;
    private SQLiteHelper dbHelper;
    private User currentUser;
    private Uri selectedAvatarUri = null;

    //  Mở File Manager để chọn ảnh đại diện
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedAvatarUri = result.getData().getData();
                    if (selectedAvatarUri != null) {
                        try {
                            //  Lưu quyền truy cập ảnh vĩnh viễn
                            getContentResolver().takePersistableUriPermission(
                                    selectedAvatarUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );

                            //  Hiển thị ảnh đã chọn
                            imageView11.setImageURI(selectedAvatarUri);

                            //  Lưu URI ảnh vào SharedPreferences để lần sau mở lại vẫn còn
                            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            prefs.edit().putString("avatar_uri", selectedAvatarUri.toString()).apply();

                        } catch (SecurityException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Không thể lưu quyền truy cập ảnh!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // 🔹 Ánh xạ view
        username = findViewById(R.id.userName);
        fullName = findViewById(R.id.fullName);
        email = findViewById(R.id.email);
        phoneNumber = findViewById(R.id.phoneNumber);
        role = findViewById(R.id.role);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        imageView11 = findViewById(R.id.imageView11); // ảnh đại diện
        btnChangeAvatar = findViewById(R.id.btnChangeAvatar); // nút chọn ảnh

        dbHelper = new SQLiteHelper(this);

        //  Lấy username người đang đăng nhập từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String loggedUser = prefs.getString("logged_user", null);

        if (loggedUser == null) {
            Toast.makeText(this, "Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileActivity.this, com.example.cuoiki.DangNhap.LoginActivity.class));
            finish();
            return;
        }

        //  Truy vấn thông tin người dùng từ SQLite
        currentUser = dbHelper.getUserByUsername(loggedUser);

        if (currentUser != null) {
            username.setText(currentUser.getUsername());
            fullName.setText(currentUser.getFull_name());
            email.setText(currentUser.getGmail());
            phoneNumber.setText(currentUser.getPhone_number());
            role.setText(currentUser.getRole());
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
        }

        //  Hiển thị ảnh đại diện đã lưu trong SharedPreferences (nếu có)
        String avatarUriStr = prefs.getString("avatar_uri", null);
        if (avatarUriStr != null) {
            try {
                selectedAvatarUri = Uri.parse(avatarUriStr);
                imageView11.setImageURI(selectedAvatarUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //  Ban đầu khóa không cho sửa
        setEditingEnabled(false);

        //  Ẩn nút thống kê cho nhân viên
        setupRoleBasedUI();

        //  Bottom Navigation
        findViewById(R.id.btm_home).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, com.example.cuoiki.ManHinhChinh.MainActivity.class));
            finish();
        });
        findViewById(R.id.btm_profile).setOnClickListener(v -> {
            // Đã ở màn hình Profile rồi, không cần làm gì
        });
        findViewById(R.id.btm_search).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, com.example.cuoiki.TimKiem.SearchActivity.class));
            finish();
        });
        findViewById(R.id.btm_static).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, com.example.cuoiki.ThongKe.StaticActivity.class));
            finish();
        });

        //  Khi bấm vào nút bút → mở File Manager để chọn ảnh mới
        btnChangeAvatar.setOnClickListener(v -> {
            openImagePicker();
        });

        //  Khi bấm vào ảnh → cũng mở File Manager để chọn ảnh mới
        imageView11.setOnClickListener(v -> {
            openImagePicker();
        });

        //  Khi bấm nút "Chỉnh sửa / Lưu lại"
        btnEditProfile.setOnClickListener(v -> {
            if (isEditing) {
                // Đang sửa → Lưu lại DB
                currentUser.setFull_name(fullName.getText().toString().trim());
                currentUser.setPhone_number(phoneNumber.getText().toString().trim());

                dbHelper.updateUser(currentUser);

                //  Cập nhật lại SharedPreferences để đồng bộ với MainActivity
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("full_name", currentUser.getFull_name());
                editor.putString("phone_number", currentUser.getPhone_number());
                if (selectedAvatarUri != null) {
                    editor.putString("avatar_uri", selectedAvatarUri.toString());
                }
                editor.apply();

                //  Tự động backup dữ liệu lên Firebase
                FirebaseBackupHelper backupHelper = new FirebaseBackupHelper(this);
                backupHelper.backupAllData(null); // Backup ngầm, không hiển thị thông báo

                //  Trả kết quả OK để MainActivity tự reload giao diện
                setResult(Activity.RESULT_OK);

                setEditingEnabled(false);
                isEditing = false;
                btnEditProfile.setText("Chỉnh sửa thông tin");
                Toast.makeText(this, "✅ Đã lưu thông tin cá nhân!", Toast.LENGTH_SHORT).show();
            } else {
                // Cho phép chỉnh sửa (trừ vai trò)
                setEditingEnabled(true);
                isEditing = true;
                btnEditProfile.setText("Lưu lại");
                Toast.makeText(this, "✏️ Bạn có thể chỉnh sửa thông tin.", Toast.LENGTH_SHORT).show();
            }
        });

        //  Đăng xuất
        btnLogout.setOnClickListener(v -> {
            //  Backup dữ liệu trước khi đăng xuất
            FirebaseBackupHelper backupHelper = new FirebaseBackupHelper(this);
            backupHelper.backupAllData(null);
            
            prefs.edit().clear().apply(); // Xóa thông tin đăng nhập
            Toast.makeText(this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
            Intent intentLogout = new Intent(ProfileActivity.this, com.example.cuoiki.DangNhap.LoginActivity.class);
            intentLogout.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentLogout);
            finish();
        });
    }

    //  Hàm bật/tắt trạng thái chỉnh sửa
    private void setEditingEnabled(boolean enabled) {
        fullName.setEnabled(enabled);
        phoneNumber.setEnabled(enabled);
        // Vai trò không cho sửa
        role.setEnabled(false);
    }

    // 🔹 Hàm mở Image Picker
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Chọn ảnh đại diện"));
    }

    //  Thiết lập giao diện theo quyền người dùng
    private void setupRoleBasedUI() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userRole = prefs.getString("user_role", "admin");
        
        // Ẩn nút thống kê cho nhân viên
        if (!"admin".equalsIgnoreCase(userRole)) {
            findViewById(R.id.btm_static).setVisibility(android.view.View.GONE);
        }
    }
}
