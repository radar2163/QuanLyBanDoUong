# HƯỚNG DẪN CẤU HÌNH FIREBASE STORAGE

## BƯỚC 1: BẬT FIREBASE STORAGE

1. Vào [Firebase Console](https://console.firebase.google.com/)
2. Chọn project của bạn
3. Vào **Storage** (bên trái menu)
4. Click **Get started**
5. Chọn **Start in test mode** (hoặc production mode)
6. Chọn location (chọn gần Việt Nam nhất, ví dụ: `asia-southeast1`)

## BƯỚC 2: CẤU HÌNH SECURITY RULES

Vào **Rules** tab trong Firebase Storage, cập nhật rules:

### Cho test/demo (cho phép đọc công khai):
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Cho phép đọc công khai
    match /images/{allPaths=**} {
      allow read: if true;  // Ai cũng đọc được
      allow write: if request.auth != null;  // Chỉ user đã đăng nhập mới upload
    }
  }
}
```

### Cho production (bảo mật hơn):
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /images/{allPaths=**} {
      // Chỉ user đã đăng nhập mới đọc/ghi
      allow read, write: if request.auth != null;
    }
  }
}
```

**Lưu ý:** Nếu dùng test mode, Firebase sẽ tự động cho phép đọc/ghi trong 30 ngày đầu.

## BƯỚC 3: KIỂM TRA DEPENDENCY

Đảm bảo trong `build.gradle.kts` đã có:
```kotlin
implementation("com.google.firebase:firebase-storage")
```

## BƯỚC 4: TEST UPLOAD

1. Mở app
2. Thêm sản phẩm mới với ảnh
3. Kiểm tra Firebase Console → Storage → xem có file upload không

## CẤU TRÚC THƯ MỤC TRONG FIREBASE STORAGE

```
Firebase Storage
└── images/
    ├── products/
    │   ├── product_1_1234567890.jpg
    │   ├── product_2_1234567891.jpg
    │   └── ...
    └── banners/
        ├── banner_1_1234567892.jpg
        └── ...
```

## XỬ LÝ LỖI

### Lỗi: "Permission denied"
→ Kiểm tra Security Rules, đảm bảo cho phép đọc/ghi

### Lỗi: "Network error"
→ Kiểm tra internet connection

### Lỗi: "File too large"
→ Firebase Storage miễn phí giới hạn 5GB, mỗi file tối đa 32MB

## CHI PHÍ

Firebase Storage có gói miễn phí:
- **5 GB storage** miễn phí
- **1 GB download/ngày** miễn phí
- Đủ cho demo/đồ án

## SAU KHI CẤU HÌNH

✅ App sẽ tự động upload ảnh lên Firebase Storage
✅ URL được lưu vào database
✅ Ảnh hiển thị trên mọi máy (không cần export)
✅ Tự động sync giữa các thiết bị

