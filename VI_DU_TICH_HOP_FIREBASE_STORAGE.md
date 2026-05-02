# VÍ DỤ TÍCH HỢP FIREBASE STORAGE

## CÁCH SỬ DỤNG FIREBASE STORAGE TRONG APP

### 1. KHI THÊM SẢN PHẨM (AddActivity.java)

**Code hiện tại:**
```java
// Lưu URI local
String imageUriString = selectedImageUri.toString();
db.addProduct(name, quantity, unit, price, 0, imageUriString, category);
```

**Code với Firebase Storage:**
```java
// Upload ảnh lên Firebase Storage trước
FirebaseStorageHelper.uploadProductImage(
    this, 
    selectedImageUri, 
    -1,  // productId tạm thời (sẽ cập nhật sau)
    new FirebaseStorageHelper.ImageUploadCallback() {
        @Override
        public void onSuccess(String imageUrl) {
            // Upload thành công, lưu URL vào database
            long productId = db.addProduct(name, quantity, unit, price, 0, imageUrl, category);
            
            // Có thể cập nhật lại tên file với productId thực tế (optional)
            Toast.makeText(AddActivity.this, "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
            
            // Backup lên Firebase
            FirebaseBackupHelper backupHelper = new FirebaseBackupHelper(AddActivity.this);
            backupHelper.backupAllData(null);
            
            finish();
        }
        
        @Override
        public void onFailure(String error) {
            Toast.makeText(AddActivity.this, "Lỗi upload ảnh: " + error, Toast.LENGTH_SHORT).show();
        }
    }
);
```

---

### 2. KHI SỬA SẢN PHẨM (ManageProductActivity.java)

**Code hiện tại:**
```java
if (selectedImageUri != null)
    product.setImageUri(selectedImageUri.toString());
dbHelper.updateProduct(product);
```

**Code với Firebase Storage:**
```java
if (selectedImageUri != null) {
    // Upload ảnh mới lên Firebase Storage
    FirebaseStorageHelper.uploadProductImage(
        this,
        selectedImageUri,
        product.getId(),
        new FirebaseStorageHelper.ImageUploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                // Xóa ảnh cũ nếu có
                if (product.getImageUri() != null && 
                    FirebaseStorageHelper.isFirebaseStorageUrl(product.getImageUri())) {
                    FirebaseStorageHelper.deleteImage(product.getImageUri(), null);
                }
                
                // Cập nhật URL mới
                product.setImageUri(imageUrl);
                dbHelper.updateProduct(product);
                
                Toast.makeText(ManageProductActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onFailure(String error) {
                Toast.makeText(ManageProductActivity.this, "Lỗi upload ảnh: " + error, Toast.LENGTH_SHORT).show();
            }
        }
    );
} else {
    // Không đổi ảnh, cập nhật bình thường
    dbHelper.updateProduct(product);
}
```

---

### 3. KHI XÓA SẢN PHẨM

**Code với Firebase Storage:**
```java
// Xóa ảnh từ Firebase Storage trước
if (product.getImageUri() != null && 
    FirebaseStorageHelper.isFirebaseStorageUrl(product.getImageUri())) {
    FirebaseStorageHelper.deleteImage(product.getImageUri(), 
        new FirebaseStorageHelper.OnDeleteCallback() {
            @Override
            public void onSuccess() {
                // Xóa ảnh thành công, xóa sản phẩm
                dbHelper.deleteProduct(product.getId());
            }
            
            @Override
            public void onFailure(String error) {
                // Vẫn xóa sản phẩm (ảnh có thể đã bị xóa trước đó)
                dbHelper.deleteProduct(product.getId());
            }
        }
    );
} else {
    // Không phải ảnh Firebase, xóa trực tiếp
    dbHelper.deleteProduct(product.getId());
}
```

---

### 4. KHI HIỂN THỊ ẢNH (Adapter)

**Code hiện tại (đã hỗ trợ URL):**
```java
if (product.getImageUri() != null && !product.getImageUri().isEmpty()) {
    Uri imageUri = Uri.parse(product.getImageUri());
    Glide.with(context)
        .load(imageUri)  // Hỗ trợ cả URI local và URL Firebase
        .placeholder(R.drawable.no_image)
        .error(R.drawable.no_image)
        .into(imageView);
}
```

**Code này đã hoạt động với Firebase Storage!** 
- Nếu `imageUri` là URL (bắt đầu bằng `http`), Glide tự động download
- Nếu là URI local, Glide load từ local

---

## QUY TRÌNH HOÀN CHỈNH

### Thêm sản phẩm mới:
```
1. User chọn ảnh
   ↓
2. Upload ảnh lên Firebase Storage
   ↓
3. Nhận URL công khai
   ↓
4. Lưu URL vào database
   ↓
5. Hiển thị sản phẩm (Glide load từ URL)
```

### Khi chia sẻ code:
```
1. Người nhận mở app
   ↓
2. App đọc URL từ database
   ↓
3. Glide tự động download ảnh từ Firebase Storage
   ↓
4. Hiển thị ảnh (tự động cache)
```

---

## LỢI ÍCH

✅ **Không cần export ảnh**: URL trong database là đủ
✅ **Tự động sync**: Mọi người thấy cùng ảnh
✅ **Hoạt động mọi nơi**: URL công khai, truy cập từ mọi máy
✅ **Tự động cache**: Glide cache ảnh, không cần tải lại

---

## LƯU Ý

⚠️ **Cần internet**: Khi load ảnh lần đầu (sau đó Glide cache)
⚠️ **Cần Firebase project**: Phải setup Firebase Storage trong Firebase Console
⚠️ **Security Rules**: Cấu hình quyền truy cập trong Firebase Console

---

## CẤU HÌNH FIREBASE STORAGE

1. Vào Firebase Console → Storage
2. Bật Firebase Storage
3. Cấu hình Security Rules (cho phép đọc công khai):

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /images/{allPaths=**} {
      allow read: if true;  // Cho phép đọc công khai
      allow write: if request.auth != null;  // Chỉ user đã đăng nhập mới upload
    }
  }
}
```

---

## KẾT LUẬN

Firebase Storage giải quyết hoàn toàn vấn đề ảnh không hiển thị khi chia sẻ code:
- ✅ Upload ảnh lên cloud
- ✅ Lưu URL vào database
- ✅ Glide tự động load từ URL
- ✅ Hoạt động trên mọi máy

