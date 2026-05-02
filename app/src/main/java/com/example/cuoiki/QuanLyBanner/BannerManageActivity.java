package com.example.cuoiki.QuanLyBanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cuoiki.R;
import com.example.cuoiki.TienIch.SQLiteHelper;
import com.example.cuoiki.DuLieu.Banner;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class BannerManageActivity extends AppCompatActivity {

    private RecyclerView recyclerBanners;
    private TextView tvEmptyBanner;
    private Button btnAddBanner, btnSettings;
    private BannerManageAdapter adapter;
    private List<Banner> bannerList = new ArrayList<>();
    private SQLiteHelper dbHelper;

    // Chọn ảnh
    private Uri selectedImageUri = null;
    private ImageView currentPreviewImage;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (currentPreviewImage != null && selectedImageUri != null) {
                        Glide.with(this)
                                .load(selectedImageUri)
                                .into(currentPreviewImage);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner_manage);

        recyclerBanners = findViewById(R.id.recyclerBanners);
        tvEmptyBanner = findViewById(R.id.tvEmptyBanner);
        btnAddBanner = findViewById(R.id.btnAddBanner);
        btnSettings = findViewById(R.id.btnSettings);
        dbHelper = new SQLiteHelper(this);

        loadBanners();
        checkEmptyBanner();

        adapter = new BannerManageAdapter(this, bannerList, new BannerManageAdapter.OnBannerActionListener() {
            @Override
            public void onEdit(Banner banner) {
                openEditDialog(banner);
            }

            @Override
            public void onDelete(Banner banner) {
                new AlertDialog.Builder(BannerManageActivity.this)
                        .setTitle("Xóa banner")
                        .setMessage("Bạn có chắc chắn muốn xóa banner này?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            dbHelper.deleteBanner(banner.getId());
                            loadBanners();
                            adapter.updateList(bannerList);
                            checkEmptyBanner();
                            
                            Toast.makeText(BannerManageActivity.this, "Đã xóa banner", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onToggleActive(Banner banner, boolean isActive) {
                banner.setActive(isActive);
                dbHelper.updateBanner(banner);
            }
        });

        recyclerBanners.setLayoutManager(new LinearLayoutManager(this));
        recyclerBanners.setAdapter(adapter);
        
        // Kiểm tra và hiển thị thông báo nếu danh sách trống
        checkEmptyBanner();

        btnAddBanner.setOnClickListener(v -> openAddDialog());
        btnSettings.setOnClickListener(v -> openSettingsDialog());
    }

    private void loadBanners() {
        bannerList = dbHelper.getAllBanners();
        if (bannerList == null) {
            bannerList = new ArrayList<>();
        }
    }
    
    private void checkEmptyBanner() {
        if (bannerList == null || bannerList.isEmpty()) {
            recyclerBanners.setVisibility(View.GONE);
            tvEmptyBanner.setVisibility(View.VISIBLE);
        } else {
            recyclerBanners.setVisibility(View.VISIBLE);
            tvEmptyBanner.setVisibility(View.GONE);
        }
    }

    private void openAddDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_edit_banner);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ImageView imgPreview = dialog.findViewById(R.id.imgPreview);
        currentPreviewImage = imgPreview;
        Button btnChooseImage = dialog.findViewById(R.id.btnChooseImage);
        EditText edtOrder = dialog.findViewById(R.id.edtOrder);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        // Chọn ảnh
        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            imagePickerLauncher.launch(Intent.createChooser(intent, "Chọn ảnh banner"));
        });

        // Lưu banner mới
        btnSave.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(this, "Vui lòng chọn ảnh banner", Toast.LENGTH_SHORT).show();
                return;
            }

            int order = 0;
            try {
                order = Integer.parseInt(edtOrder.getText().toString());
            } catch (NumberFormatException e) {
                order = bannerList.size() + 1;
            }

            // Lưu banner với URI local
            long id = dbHelper.addBanner(selectedImageUri.toString(), 0, order, true);
            if (id > 0) {
                loadBanners();
                adapter.updateList(bannerList);
                checkEmptyBanner();
                
                Toast.makeText(BannerManageActivity.this, "Đã thêm banner", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(BannerManageActivity.this, "Lỗi khi thêm banner", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void openEditDialog(Banner banner) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_add_edit_banner);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ImageView imgPreview = dialog.findViewById(R.id.imgPreview);
        currentPreviewImage = imgPreview;
        Button btnChooseImage = dialog.findViewById(R.id.btnChooseImage);
        EditText edtOrder = dialog.findViewById(R.id.edtOrder);
        Button btnSave = dialog.findViewById(R.id.btnSave);

        // Hiển thị dữ liệu cũ
        if (banner.getImageUri() != null && !banner.getImageUri().isEmpty()) {
            Glide.with(this)
                    .load(Uri.parse(banner.getImageUri()))
                    .into(imgPreview);
            selectedImageUri = Uri.parse(banner.getImageUri());
        } else if (banner.getImageResId() != 0) {
            imgPreview.setImageResource(banner.getImageResId());
        }

        edtOrder.setText(String.valueOf(banner.getDisplayOrder()));

        // Chọn ảnh mới
        btnChooseImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            imagePickerLauncher.launch(Intent.createChooser(intent, "Chọn ảnh banner"));
        });

        // Lưu thay đổi
        btnSave.setOnClickListener(v -> {
            int order = 0;
            try {
                order = Integer.parseInt(edtOrder.getText().toString());
            } catch (NumberFormatException e) {
                order = banner.getDisplayOrder();
            }

            banner.setDisplayOrder(order);

            // Cập nhật banner
            if (selectedImageUri != null) {
                banner.setImageUri(selectedImageUri.toString());
            }
            
            if (dbHelper.updateBanner(banner)) {
                loadBanners();
                adapter.updateList(bannerList);
                
                Toast.makeText(this, "Đã cập nhật banner", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Lỗi khi cập nhật banner", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void openSettingsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_banner_settings);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        EditText edtAutoScrollInterval = dialog.findViewById(R.id.edtAutoScrollInterval);
        Button btnSave = dialog.findViewById(R.id.btnSave);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        // Load thời gian hiện tại từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("banner_prefs", MODE_PRIVATE);
        int currentInterval = prefs.getInt("auto_scroll_interval", 6000); // Mặc định 6 giây
        edtAutoScrollInterval.setText(String.valueOf(currentInterval / 1000)); // Hiển thị bằng giây

        btnSave.setOnClickListener(v -> {
            try {
                int seconds = Integer.parseInt(edtAutoScrollInterval.getText().toString());
                if (seconds < 1) {
                    Toast.makeText(this, "Thời gian phải lớn hơn 0 giây", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (seconds > 60) {
                    Toast.makeText(this, "Thời gian không được vượt quá 60 giây", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Lưu vào SharedPreferences (lưu bằng milliseconds)
                int milliseconds = seconds * 1000;
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("auto_scroll_interval", milliseconds);
                editor.apply();

                Toast.makeText(this, "Đã lưu cài đặt. Thời gian tự động chuyển: " + seconds + " giây", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}

