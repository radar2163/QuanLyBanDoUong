# HƯỚNG DẪN EXPORT DỮ LIỆU KÈM ẢNH

## VẤN ĐỀ

Khi nén code và gửi cho người khác, ảnh không hiển thị vì:
- App lưu **URI** (đường dẫn local) vào database
- URI chỉ tồn tại trên máy của bạn
- Khi chạy trên máy khác, không tìm thấy file ảnh

**Ví dụ URI:**
- `content://media/external/images/media/12345`
- `file:///storage/emulated/0/DCIM/Camera/IMG_2024.jpg`

## GIẢI PHÁP

### CÁCH 1: Export Database + Ảnh (Khuyến nghị)

#### Bước 1: Export Database
1. Mở Android Studio
2. Mở **Device File Explorer** (View → Tool Windows → Device File Explorer)
3. Điều hướng đến: `/data/data/com.example.cuoiki/databases/`
4. Copy file `database.db` ra máy tính

#### Bước 2: Export Ảnh
1. Trong Device File Explorer, tìm thư mục chứa ảnh
2. Hoặc dùng app để export ảnh (xem Cách 2)

#### Bước 3: Gửi kèm
- Gửi file `database.db`
- Gửi thư mục chứa ảnh
- Hướng dẫn người nhận copy vào đúng vị trí

---

### CÁCH 2: Sử dụng Firebase Backup (Đã có sẵn)

App đã có tính năng backup lên Firebase. Cách này tốt nhất:

1. **Backup dữ liệu lên Firebase:**
   - App tự động backup khi thêm/sửa sản phẩm
   - Hoặc vào màn hình quản lý → backup thủ công

2. **Người nhận restore từ Firebase:**
   - Mở app trên máy mới
   - Đăng nhập với tài khoản Firebase
   - App sẽ tự động restore dữ liệu (bao gồm cả ảnh nếu dùng Firebase Storage)

**Lưu ý:** Hiện tại app chỉ backup metadata, chưa backup ảnh lên Firebase Storage.

---

### CÁCH 3: Sử dụng Ảnh từ Drawable (Đơn giản nhất)

Thay vì dùng URI, dùng ảnh có sẵn trong project:

1. **Copy ảnh vào thư mục `res/drawable/`:**
   ```
   app/src/main/res/drawable/
   ├── product1.jpg
   ├── product2.jpg
   └── banner1.jpg
   ```

2. **Khi thêm sản phẩm:**
   - Không chọn ảnh từ gallery
   - Dùng `imageResId` thay vì `imageUri`
   - Code: `db.addProduct(..., R.drawable.product1, null, ...)`

**Ưu điểm:**
- ✅ Ảnh nằm trong project
- ✅ Khi nén code, ảnh đi kèm
- ✅ Không cần export riêng

**Nhược điểm:**
- ❌ Phải copy ảnh vào project trước
- ❌ Tăng kích thước APK

---

### CÁCH 4: Tạo Script Export (Nâng cao)

Tạo Activity để export tất cả ảnh kèm database:

```java
// Export database + ảnh vào thư mục Download
File exportDir = new File(Environment.getExternalStoragePublicDirectory(
    Environment.DIRECTORY_DOWNLOADS), "AppExport");
exportDir.mkdirs();

// Copy database
File dbFile = new File(getDatabasePath("database.db"));
File destDb = new File(exportDir, "database.db");
copyFile(dbFile, destDb);

// Export ảnh
ImageExportHelper.exportAllImages(context, dbHelper, exportDir.getAbsolutePath());
```

---

## KHUYẾN NGHỊ

**Cho đồ án/demo:**
→ Dùng **Cách 3** (ảnh trong drawable) - Đơn giản, đảm bảo ảnh luôn có

**Cho production:**
→ Dùng **Firebase Storage** - Lưu ảnh lên cloud, tự động sync

**Cho chia sẻ tạm thời:**
→ Dùng **Cách 1** (export database + ảnh) - Giữ nguyên dữ liệu

---

## HƯỚNG DẪN NGƯỜI NHẬN

Khi nhận code từ bạn:

1. **Nếu có file database.db:**
   - Copy vào: `/data/data/com.example.cuoiki/databases/`
   - Dùng Android Studio Device File Explorer
   - Hoặc dùng ADB: `adb push database.db /data/data/com.example.cuoiki/databases/`

2. **Nếu có thư mục ảnh:**
   - Copy ảnh vào đúng vị trí theo URI trong database
   - Hoặc dùng app restore từ Firebase

3. **Nếu không có gì:**
   - Chạy app và thêm ảnh lại
   - Hoặc dùng ảnh mặc định (no_image)

---

## LƯU Ý QUAN TRỌNG

⚠️ **URI không portable:**
- URI `content://` hoặc `file://` chỉ hoạt động trên máy tạo ra nó
- Khi chuyển máy, URI không còn hợp lệ

✅ **Giải pháp tốt:**
- Lưu ảnh vào internal storage của app
- Hoặc dùng Firebase Storage
- Hoặc dùng ảnh trong drawable

---

## CODE MẪU: Copy ảnh vào app storage

```java
// Khi chọn ảnh, copy vào app storage thay vì lưu URI
Uri selectedUri = ...; // Từ image picker
String savedPath = ImageExportHelper.copyImageToAppStorage(context, selectedUri);

// Lưu đường dẫn vào database
product.setImageUri(savedPath); // Thay vì selectedUri.toString()
```

Sau đó khi load ảnh:
```java
if (product.getImageUri() != null) {
    File imageFile = new File(product.getImageUri());
    if (imageFile.exists()) {
        Glide.with(context).load(imageFile).into(imageView);
    }
}
```

