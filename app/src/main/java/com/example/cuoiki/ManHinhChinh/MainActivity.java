package com.example.cuoiki.ManHinhChinh;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.ThemSanPham.AddActivity;
import com.example.cuoiki.XemGia.BuyActivity;
import com.example.cuoiki.XuatKho.ExportnActivity;
import com.example.cuoiki.CapNhatKho.UpdateActivity;
import com.example.cuoiki.HoSo.ProfileActivity;
import com.example.cuoiki.TimKiem.SearchActivity;
import com.example.cuoiki.ThongKe.StaticActivity;
import com.example.cuoiki.QuanLyTaiKhoan.AccountManageActivity;
import com.example.cuoiki.QuanLySanPham.ManageProductActivity;
import com.example.cuoiki.ThongBao.NotificationActivity;
import com.example.cuoiki.QuanLyThongBao.NotificationManageActivity;
import com.example.cuoiki.TienIch.SQLiteHelper;
import com.example.cuoiki.TienIch.FirebaseBackupHelper;
import com.example.cuoiki.DuLieu.Product;
import com.example.cuoiki.DuLieu.User;
import com.example.cuoiki.ManHinhChinh.ProductAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView productListView;
    private SQLiteHelper dbHelper;
    private List<Product> productList;
    private ProductAdapter adapter;

    //  Các nút lọc danh mục
    private Button btnAll, btnBeer, btnWine, btnSoftDrink, btnWater, btnOther;

    //  Thông tin người dùng ở phần header
    private ImageView profileImage;
    private TextView helloText;
    private ConstraintLayout btnNotification;
    private TextView notificationBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Ánh xạ ---
        ConstraintLayout btnMore = findViewById(R.id.btnMore);
        btnMore.setOnClickListener(v -> showMoreOptions());

        ImageView addButton = findViewById(R.id.imageView4);
        ImageView exportbtn = findViewById(R.id.imageView9);
        ImageView updatebtn = findViewById(R.id.imageView6);
        ImageView buybtn = findViewById(R.id.imageView7);

        ImageView btmHome = findViewById(R.id.btm_home);
        ImageView btmProfile = findViewById(R.id.btm_profile);
        ImageView btmSearch = findViewById(R.id.btm_search);
        ImageView btmStatic = findViewById(R.id.btm_static);

        btnAll = findViewById(R.id.btnAll);
        btnBeer = findViewById(R.id.btnBeer);
        btnWine = findViewById(R.id.btnWine);
        btnSoftDrink = findViewById(R.id.btnSoftDrink);
        btnWater = findViewById(R.id.btnWater);
        btnOther = findViewById(R.id.btnOther);

        //  Ánh xạ cho phần header người dùng
        profileImage = findViewById(R.id.profileImage);  // ảnh ở khung Hello
        helloText = findViewById(R.id.helloText);       // dòng Hello ...
        btnNotification = findViewById(R.id.btnNotification);
        notificationBadge = findViewById(R.id.notificationBadge);

        // --- Khởi tạo DB & RecyclerView ---
        productListView = findViewById(R.id.productListView);
        dbHelper = new SQLiteHelper(this);
        productListView.setLayoutManager(new GridLayoutManager(this, 2));

        // Load toàn bộ sản phẩm ban đầu ---
        loadProducts();
        adapter = new ProductAdapter(productList);
        productListView.setAdapter(adapter);

        //  Nút lọc danh mục ---
        btnAll.setOnClickListener(v -> updateCategory("Tất cả"));
        btnBeer.setOnClickListener(v -> updateCategory("Bia"));
        btnWine.setOnClickListener(v -> updateCategory("Rượu"));
        btnSoftDrink.setOnClickListener(v -> updateCategory("Nước ngọt"));
        btnWater.setOnClickListener(v -> updateCategory("Nước suối"));
        btnOther.setOnClickListener(v -> updateCategory("Khác"));

        //  Kiểm tra quyền và ẩn/hiện chức năng theo role
        setupRoleBasedUI();

        // --- Các nút thao tác ---
        addButton.setOnClickListener(v -> startActivityForResult(new Intent(this, com.example.cuoiki.ThemSanPham.AddActivity.class), 1));
        exportbtn.setOnClickListener(v -> startActivityForResult(new Intent(this, com.example.cuoiki.XuatKho.ExportnActivity.class), 1));
        updatebtn.setOnClickListener(v -> startActivityForResult(new Intent(this, com.example.cuoiki.CapNhatKho.UpdateActivity.class), 1));
        buybtn.setOnClickListener(v -> startActivityForResult(new Intent(this, com.example.cuoiki.XemGia.BuyActivity.class), 1));

        // --- Thanh điều hướng dưới cùng ---
        btmHome.setOnClickListener(v -> {});
        btmProfile.setOnClickListener(v -> startActivityForResult(new Intent(this, com.example.cuoiki.HoSo.ProfileActivity.class), 2));
        btmSearch.setOnClickListener(v -> startActivity(new Intent(this, com.example.cuoiki.TimKiem.SearchActivity.class)));
        btmStatic.setOnClickListener(v -> startActivity(new Intent(this, com.example.cuoiki.ThongKe.StaticActivity.class)));
        
        //  Setup settings button (chỉ cho nhân viên)
        ImageView btmSettings = findViewById(R.id.btm_settings);
        btmSettings.setOnClickListener(v -> showSettingsDialog());

        //   Hiển thị thông tin người dùng hiện tại
        updateUserInfo();

        //  Setup notification button
        btnNotification.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, com.example.cuoiki.ThongBao.NotificationActivity.class));
        });
        updateNotificationBadge();
    }

    // Đọc SharedPreferences để hiển thị tên và ảnh người dùng
    private void updateUserInfo() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String fullName = prefs.getString("full_name", "Admin");
        String avatarUriStr = prefs.getString("avatar_uri", null);

        // 👤 Hiển thị "Hello <Họ tên>"
        helloText.setText("Hello " + fullName);

        //  Hiển thị ảnh nếu có, ngược lại dùng ảnh mặc định
        if (avatarUriStr != null) {
            try {
                Uri avatarUri = Uri.parse(avatarUriStr);
                profileImage.setImageURI(avatarUri);
            } catch (Exception e) {
                profileImage.setImageResource(R.drawable.profile);
            }
        } else {
            profileImage.setImageResource(R.drawable.profile);
        }
    }

    //  Load toàn bộ danh sách sản phẩm từ DB
    private void loadProducts() {
        productList = dbHelper.getAllProducts();
    }

    //  Cập nhật danh mục
    private void updateCategory(String category) {
        try {
            productList = dbHelper.getProductsByCategory(category);
            adapter.updateData(productList);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể tải danh mục: " + category, Toast.LENGTH_SHORT).show();
        }
    }

    //  Khi quay lại từ màn hình khác (Add / ManageProduct / Update / Profile)
    @Override
    protected void onResume() {
        super.onResume();
        // Reload dữ liệu thật từ DB mỗi lần quay lại
        loadProducts();
        adapter.updateData(productList);
        // Scroll lên đầu để hiển thị sản phẩm mới nhất
        productListView.smoothScrollToPosition(0);

        //  Cập nhật lại ảnh + tên khi quay lại từ ProfileActivity
        updateUserInfo();
        updateNotificationBadge();
    }

    //  Cập nhật badge thông báo
    private void updateNotificationBadge() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String username = prefs.getString("logged_user", "admin");
        int unreadCount = dbHelper.getUnreadNotificationCount(username);
        
        if (unreadCount > 0) {
            notificationBadge.setVisibility(View.VISIBLE);
            notificationBadge.setText(String.valueOf(unreadCount));
        } else {
            notificationBadge.setVisibility(View.GONE);
        }
    }

    //  Sau khi thêm sản phẩm (AddActivity)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //  Reload lại sản phẩm
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            loadProducts();
            adapter.updateData(productList);
            //  Scroll lên đầu để hiển thị sản phẩm mới
            productListView.smoothScrollToPosition(0);
        }

        //  Reload lại thông tin người dùng nếu chỉnh sửa Profile
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            updateUserInfo();
        }
    }

    //  Thiết lập giao diện theo quyền người dùng
    private void setupRoleBasedUI() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userRole = prefs.getString("user_role", "admin");
        
        // Ẩn các chức năng admin cho nhân viên
        if (!"admin".equalsIgnoreCase(userRole)) {
            // Ẩn nút "Thêm sản phẩm" (chỉ admin mới được thêm)
            findViewById(R.id.layoutAddButton).setVisibility(View.GONE);
            // Ẩn nút "More" (chứa các tùy chọn quản lý)
            findViewById(R.id.layoutMoreButton).setVisibility(View.GONE);
            // Ẩn nút "Thống kê" (chỉ admin mới được xem thống kê)
            findViewById(R.id.layoutStaticButton).setVisibility(View.GONE);
            // Hiển thị nút cài đặt cho nhân viên (thay thế nút thống kê)
            findViewById(R.id.layoutSettingsButton).setVisibility(View.VISIBLE);
        } else {
            // Ẩn nút cài đặt cho admin
            findViewById(R.id.layoutSettingsButton).setVisibility(View.GONE);
        }
    }

    //  Hiển thị dialog "Tùy chọn thêm"
    private void showMoreOptions() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_more_options);

        LinearLayout btnManageAccount = dialog.findViewById(R.id.btnManageAccount);
        LinearLayout btnManageProduct = dialog.findViewById(R.id.btnManageProduct);
        LinearLayout btnManageNotification = dialog.findViewById(R.id.btnManageNotification);
        LinearLayout btnManageOrder = dialog.findViewById(R.id.btnManageOrder);
        LinearLayout btnManageBanner = dialog.findViewById(R.id.btnManageBanner);
        Button btnClose = dialog.findViewById(R.id.btnClose);

        //  Kiểm tra quyền và ẩn các tùy chọn quản lý cho nhân viên
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userRole = prefs.getString("user_role", "admin");
        
        if (!"admin".equalsIgnoreCase(userRole)) {
            // Ẩn các tùy chọn quản lý cho nhân viên
            btnManageAccount.setVisibility(View.GONE);
            btnManageProduct.setVisibility(View.GONE);
            btnManageNotification.setVisibility(View.GONE);
            btnManageOrder.setVisibility(View.GONE);
            btnManageBanner.setVisibility(View.GONE);
        } else {
            // Hiển thị đầy đủ cho admin
            btnManageAccount.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this, com.example.cuoiki.QuanLyTaiKhoan.AccountManageActivity.class));
            });

            btnManageProduct.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this, com.example.cuoiki.QuanLySanPham.ManageProductActivity.class));
            });

            btnManageNotification.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this, com.example.cuoiki.QuanLyThongBao.NotificationManageActivity.class));
            });

            btnManageOrder.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this, com.example.cuoiki.QuanLyDonHang.OrderManageActivity.class));
            });

            btnManageBanner.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this, com.example.cuoiki.QuanLyBanner.BannerManageActivity.class));
            });
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setDimAmount(0.7f); // Làm tối background
        dialog.show();
    }

    // ️ Hiển thị dialog cài đặt (chỉ cho nhân viên)
    private void showSettingsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_settings);

        LinearLayout btnChangePassword = dialog.findViewById(R.id.btnChangePassword);
        LinearLayout btnLogout = dialog.findViewById(R.id.btnLogoutSettings);
        LinearLayout btnClose = dialog.findViewById(R.id.btnCloseSettings);

        btnChangePassword.setOnClickListener(v -> {
            dialog.dismiss();
            showChangePasswordDialog();
        });

        btnLogout.setOnClickListener(v -> {
            dialog.dismiss();
            performLogout();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.show();
    }

    //  Hiển thị dialog đổi mật khẩu
    private void showChangePasswordDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Đổi mật khẩu");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        android.widget.EditText edtOldPassword = dialogView.findViewById(R.id.edtOldPassword);
        android.widget.EditText edtNewPassword = dialogView.findViewById(R.id.edtNewPassword);
        android.widget.EditText edtConfirmPassword = dialogView.findViewById(R.id.edtConfirmPassword);

        builder.setView(dialogView);

        builder.setPositiveButton("Đổi mật khẩu", (dialog, which) -> {
            String oldPassword = edtOldPassword.getText().toString().trim();
            String newPassword = edtNewPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu mới không khớp!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 4) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 4 ký tự!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra mật khẩu cũ
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String username = prefs.getString("logged_user", null);
            
            if (username == null) {
                Toast.makeText(this, "Lỗi: Không tìm thấy thông tin đăng nhập!", Toast.LENGTH_SHORT).show();
                return;
            }

            SQLiteHelper dbHelper = new SQLiteHelper(this);
            if (!dbHelper.checkAccount(username, oldPassword)) {
                Toast.makeText(this, "Mật khẩu cũ không đúng!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật mật khẩu mới
            User user = dbHelper.getUserByUsername(username);
            if (user != null) {
                user.setPassword(newPassword);
                dbHelper.updateUser(user);
                
                // 💾 Backup dữ liệu lên Firebase
                FirebaseBackupHelper backupHelper = new FirebaseBackupHelper(this);
                backupHelper.backupAllData(null);
                
                Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lỗi: Không tìm thấy người dùng!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    //  Đăng xuất
    private void performLogout() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Đăng xuất");
        builder.setMessage("Bạn có chắc chắn muốn đăng xuất?");
        
        builder.setPositiveButton("Đăng xuất", (dialog, which) -> {
            //  Backup dữ liệu trước khi đăng xuất
            FirebaseBackupHelper backupHelper = new FirebaseBackupHelper(this);
            backupHelper.backupAllData(null);
            
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            prefs.edit().clear().apply();
            
            Toast.makeText(this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, com.example.cuoiki.DangNhap.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
