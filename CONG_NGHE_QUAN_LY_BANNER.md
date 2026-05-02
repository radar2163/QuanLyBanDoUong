# CÔNG NGHỆ QUẢN LÝ BANNER
## BannerManageActivity - Ứng dụng Cuối Kỳ

---

## 1. TỔNG QUAN

### 1.1. Mục đích
Màn hình quản lý banner cho phép admin:
- Xem danh sách tất cả banner
- Thêm banner mới (chọn ảnh từ thiết bị)
- Sửa banner (thay đổi ảnh, thứ tự)
- Xóa banner
- Bật/tắt banner (active/inactive)
- Cài đặt thời gian auto-scroll

### 1.2. Công nghệ sử dụng
- **Language**: Java
- **Platform**: Android (minSdk 24, targetSdk 34)
- **UI Framework**: AndroidX (Material Design)
- **Database**: SQLite (SQLiteHelper)
- **Image Loading**: Glide Library
- **Image Picker**: ActivityResultLauncher (Modern API)
- **Dialog**: Custom Dialog
- **Storage**: SharedPreferences (cài đặt auto-scroll)

---

## 2. CÁC COMPONENT CHÍNH

### 2.1. RecyclerView
- **Chức năng**: Hiển thị danh sách banner
- **Layout Manager**: LinearLayoutManager (danh sách dọc)
- **Adapter**: BannerManageAdapter (custom adapter)

### 2.2. ActivityResultLauncher
- **Chức năng**: Chọn ảnh từ thiết bị (Modern API thay cho startActivityForResult)
- **API**: AndroidX Activity Result API
- **Intent**: ACTION_OPEN_DOCUMENT (chọn file ảnh)

### 2.3. Dialog
- **Chức năng**: Hiển thị form thêm/sửa banner
- **Type**: Custom Dialog với layout riêng
- **Features**: Preview ảnh, chọn ảnh, nhập thứ tự

### 2.4. Glide
- **Chức năng**: Load và hiển thị ảnh banner
- **Features**: Placeholder, error handling, caching

---

## 3. CODE IMPLEMENTATION

### 3.1. ActivityResultLauncher - Chọn Ảnh

```java
// Khai báo ActivityResultLauncher
private final ActivityResultLauncher<Intent> imagePickerLauncher =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                selectedImageUri = result.getData().getData();
                if (currentPreviewImage != null && selectedImageUri != null) {
                    // Load ảnh vào ImageView preview bằng Glide
                    Glide.with(this)
                            .load(selectedImageUri)
                            .into(currentPreviewImage);
                }
            }
        });
```

**Giải thích:**
- **`registerForActivityResult`**: Đăng ký launcher (Modern API)
- **`ActivityResultContracts.StartActivityForResult`**: Contract cho startActivityForResult
- **`result.getData().getData()`**: Lấy URI của ảnh đã chọn
- **Glide**: Load ảnh vào ImageView preview

### 3.2. Mở Image Picker

```java
btnChooseImage.setOnClickListener(v -> {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    intent.setType("image/*");
    imagePickerLauncher.launch(Intent.createChooser(intent, "Chọn ảnh banner"));
});
```

**Giải thích:**
- **`Intent.ACTION_OPEN_DOCUMENT`**: Mở file picker (Modern API, có persistent permission)
- **`Intent.CATEGORY_OPENABLE`**: Chỉ hiển thị file có thể mở được
- **`intent.setType("image/*")`**: Chỉ hiển thị file ảnh
- **`imagePickerLauncher.launch()`**: Khởi chạy image picker

### 3.3. Custom Dialog - Thêm/Sửa Banner

```java
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
            Toast.makeText(this, "Đã thêm banner", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }
    });

    dialog.show();
}
```

**Giải thích:**
- **`Dialog`**: Tạo dialog tùy chỉnh
- **`setContentView()`**: Set layout cho dialog
- **`getWindow().setLayout()`**: Set kích thước dialog
- **Preview ảnh**: Hiển thị ảnh đã chọn trước khi lưu
- **Validation**: Kiểm tra đã chọn ảnh chưa
- **Lưu vào database**: `dbHelper.addBanner()`

### 3.4. RecyclerView Setup

```java
recyclerBanners.setLayoutManager(new LinearLayoutManager(this));
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
                    Toast.makeText(this, "Đã xóa banner", Toast.LENGTH_SHORT).show();
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
recyclerBanners.setAdapter(adapter);
```

---

## 4. BANNER MANAGE ADAPTER

### 4.1. BannerManageAdapter.java

```java
public class BannerManageAdapter extends RecyclerView.Adapter<BannerManageAdapter.BannerViewHolder> {
    
    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Banner banner = bannerList.get(position);

        // Hiển thị ảnh banner bằng Glide
        if (banner.getImageUri() != null && !banner.getImageUri().isEmpty()) {
            Uri imageUri = Uri.parse(banner.getImageUri());
            Glide.with(context)
                    .load(imageUri)
                    .placeholder(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .into(holder.imgBanner);
        } else if (banner.getImageResId() != 0) {
            holder.imgBanner.setImageResource(banner.getImageResId());
        }

        holder.tvOrder.setText("Thứ tự: " + banner.getDisplayOrder());
        holder.switchActive.setChecked(banner.isActive());

        // Click sửa
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(banner));

        // Click xóa
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(banner));

        // Toggle active
        holder.switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            listener.onToggleActive(banner, isChecked);
        });
    }
}
```

---

## 5. CÀI ĐẶT AUTO-SCROLL

### 5.1. SharedPreferences

```java
private void openSettingsDialog() {
    Dialog dialog = new Dialog(this);
    dialog.setContentView(R.layout.dialog_banner_settings);

    EditText edtAutoScrollInterval = dialog.findViewById(R.id.edtAutoScrollInterval);
    Button btnSave = dialog.findViewById(R.id.btnSave);

    // Load thời gian hiện tại từ SharedPreferences
    SharedPreferences prefs = getSharedPreferences("banner_prefs", MODE_PRIVATE);
    int currentInterval = prefs.getInt("auto_scroll_interval", 6000); // Mặc định 6 giây
    edtAutoScrollInterval.setText(String.valueOf(currentInterval / 1000)); // Hiển thị bằng giây

    btnSave.setOnClickListener(v -> {
        try {
            int seconds = Integer.parseInt(edtAutoScrollInterval.getText().toString());
            if (seconds < 1 || seconds > 60) {
                Toast.makeText(this, "Thời gian phải từ 1-60 giây", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lưu vào SharedPreferences (lưu bằng milliseconds)
            int milliseconds = seconds * 1000;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("auto_scroll_interval", milliseconds);
            editor.apply();

            Toast.makeText(this, "Đã lưu cài đặt", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
        }
    });

    dialog.show();
}
```

**Giải thích:**
- **SharedPreferences**: Lưu cài đặt auto-scroll interval
- **Key**: `"auto_scroll_interval"` (milliseconds)
- **Default**: 6000ms (6 giây)
- **Validation**: 1-60 giây

---

## 6. CÁC TÍNH NĂNG CHÍNH

### 6.1. Quản Lý Banner
- ✅ Xem danh sách banner với RecyclerView
- ✅ Thêm banner mới (chọn ảnh, nhập thứ tự)
- ✅ Sửa banner (thay đổi ảnh, thứ tự)
- ✅ Xóa banner (có xác nhận)
- ✅ Bật/tắt banner (Switch)

### 6.2. Chọn Ảnh
- ✅ ActivityResultLauncher (Modern API)
- ✅ ACTION_OPEN_DOCUMENT (persistent permission)
- ✅ Preview ảnh trước khi lưu
- ✅ Glide load ảnh

### 6.3. Cài Đặt
- ✅ Cài đặt thời gian auto-scroll
- ✅ Lưu vào SharedPreferences
- ✅ Validation input

---

## 7. DEPENDENCIES

### 7.1. Trong `build.gradle.kts`

```kotlin
// Glide cho image loading
implementation("com.github.bumptech.glide:glide:4.15.1")
annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

// AndroidX Activity (cho ActivityResultLauncher)
implementation(libs.activity)  // Bao gồm ActivityResultLauncher
```

---

## 8. SO SÁNH VỚI CÁC CÁCH KHÁC

### 8.1. ActivityResultLauncher vs startActivityForResult

#### ActivityResultLauncher (Modern API)
- ✅ **Type-safe**: Compile-time checking
- ✅ **Lifecycle-aware**: Tự động cleanup
- ✅ **Khuyến nghị**: Google khuyến nghị dùng

#### startActivityForResult (Deprecated)
- ⚠️ **Deprecated**: Từ Android 11+
- ⚠️ **Không type-safe**: Runtime errors
- ⚠️ **Manual cleanup**: Phải tự xử lý

### 8.2. ACTION_OPEN_DOCUMENT vs ACTION_PICK

#### ACTION_OPEN_DOCUMENT
- ✅ **Persistent permission**: Có quyền truy cập lâu dài
- ✅ **Modern API**: Khuyến nghị dùng
- ✅ **Better UX**: File picker tốt hơn

#### ACTION_PICK
- ⚠️ **Temporary permission**: Quyền tạm thời
- ⚠️ **Older API**: API cũ hơn

---

## 9. TÀI LIỆU THAM KHẢO

### 9.1. Official Documentation
- **ActivityResultLauncher**: https://developer.android.com/training/basics/intents/result
- **ACTION_OPEN_DOCUMENT**: https://developer.android.com/reference/android/content/Intent#ACTION_OPEN_DOCUMENT
- **Glide**: https://github.com/bumptech/glide

---

## 10. KẾT LUẬN

**Quản lý Banner** sử dụng các công nghệ:
- ✅ **RecyclerView**: Hiển thị danh sách
- ✅ **ActivityResultLauncher**: Chọn ảnh (Modern API)
- ✅ **Glide**: Load và hiển thị ảnh
- ✅ **Dialog**: Form thêm/sửa
- ✅ **SharedPreferences**: Lưu cài đặt
- ✅ **SQLite**: Lưu trữ banner

**Công nghệ nổi bật:**
1. ActivityResultLauncher (Modern API thay cho startActivityForResult)
2. ACTION_OPEN_DOCUMENT (persistent permission)
3. Glide (image loading và caching)
4. Custom Dialog (form input)

---

*Tài liệu này giải thích công nghệ sử dụng trong quản lý banner.*

