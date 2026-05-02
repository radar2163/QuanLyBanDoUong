# HƯỚNG DẪN TEST FIREBASE STORAGE

## CÁCH TEST ĐƠN GIẢN NHẤT

### Cách 1: Test trên cùng máy (Khuyến nghị)

1. **Thêm sản phẩm/banner với ảnh:**
   - Mở app → Thêm sản phẩm → Chọn ảnh
   - Đợi upload xong (sẽ có thông báo "Thêm sản phẩm thành công")

2. **Kiểm tra Firebase Console:**
   - Vào [Firebase Console](https://console.firebase.google.com/)
   - Chọn project → Storage
   - Xem có file ảnh trong thư mục `images/products/` hoặc `images/banners/` không
   - ✅ Nếu có file → Upload thành công!

3. **Kiểm tra trong app:**
   - Xem danh sách sản phẩm/banner
   - Ảnh có hiển thị không?
   - ✅ Nếu có → Hoạt động tốt!

**Không cần xóa app!** Chỉ cần kiểm tra Firebase Console và xem ảnh trong app.

---

### Cách 2: Test trên máy khác (Test thực tế)

1. **Trên máy bạn:**
   - Thêm sản phẩm với ảnh
   - Ảnh upload lên Firebase Storage
   - URL được lưu vào database

2. **Export database:**
   - Copy file `database.db` từ máy bạn
   - Gửi cho người khác

3. **Trên máy người khác:**
   - Copy `database.db` vào app
   - Mở app
   - ✅ Ảnh sẽ tự động download từ Firebase Storage và hiển thị!

**Đây là cách test thực tế nhất!**

---

### Cách 3: Test bằng cách xóa data app (Không cần xóa app)

1. **Thêm sản phẩm với ảnh:**
   - Upload lên Firebase Storage
   - URL lưu vào database

2. **Xóa data app (không xóa app):**
   - Settings → Apps → [Tên app] → Storage → Clear Data
   - Hoặc: Settings → Apps → [Tên app] → Uninstall (xóa app)
   - Cài lại app

3. **Restore từ Firebase:**
   - Mở app → Đăng nhập
   - App sẽ restore dữ liệu từ Firebase
   - ✅ Ảnh sẽ tự động download và hiển thị!

---

## CÁCH TEST NHANH NHẤT (Khuyến nghị)

### Bước 1: Thêm sản phẩm với ảnh
```
1. Mở app
2. Thêm sản phẩm mới
3. Chọn ảnh
4. Đợi upload (sẽ có thông báo)
```

### Bước 2: Kiểm tra Firebase Console
```
1. Vào Firebase Console → Storage
2. Xem có file trong images/products/ không
3. Click vào file → Copy URL
4. Paste URL vào browser → Xem ảnh có hiển thị không
```

### Bước 3: Kiểm tra trong app
```
1. Xem danh sách sản phẩm
2. Ảnh có hiển thị không?
3. Click vào sản phẩm → Xem chi tiết
4. Ảnh có hiển thị không?
```

**Nếu cả 3 bước đều OK → Firebase Storage hoạt động tốt!**

---

## KIỂM TRA URL TRONG DATABASE

### Cách xem URL đã lưu:

1. **Dùng Android Studio:**
   - Device File Explorer
   - Vào `/data/data/com.example.cuoiki/databases/`
   - Copy `database.db` ra máy tính
   - Mở bằng SQLite Browser
   - Xem bảng `product` hoặc `banners`
   - Cột `imageUri` sẽ có URL Firebase (bắt đầu bằng `https://firebasestorage...`)

2. **Hoặc log trong code:**
   ```java
   Log.d("Test", "Image URL: " + product.getImageUri());
   ```

---

## TEST TRƯỜNG HỢP LỖI

### Test khi không có internet:

1. Tắt WiFi/Data
2. Thêm sản phẩm với ảnh
3. ✅ App sẽ fallback: Lưu URI local (không upload)
4. Bật internet lại
5. Sửa sản phẩm → Chọn ảnh mới
6. ✅ App sẽ upload lên Firebase Storage

---

## SO SÁNH TRƯỚC VÀ SAU

### Trước (URI local):
```
Database: content://media/external/images/media/12345
Máy khác: ❌ Không tìm thấy file
```

### Sau (Firebase Storage):
```
Database: https://firebasestorage.googleapis.com/.../product1.jpg
Máy khác: ✅ Tự động download và hiển thị
```

---

## KẾT LUẬN

**Không cần xóa app để test!**

Chỉ cần:
1. ✅ Thêm sản phẩm với ảnh
2. ✅ Kiểm tra Firebase Console
3. ✅ Xem ảnh trong app
4. ✅ (Tùy chọn) Test trên máy khác

Nếu muốn test restore:
- Xóa data app (Settings → Clear Data)
- Hoặc uninstall → cài lại
- Restore từ Firebase
- Xem ảnh có hiển thị không



