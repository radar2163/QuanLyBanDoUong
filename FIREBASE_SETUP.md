# Hướng dẫn thiết lập Firebase Backup

## Bước 1: Tạo Firebase Project

1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" hoặc chọn project có sẵn
3. Điền tên project và làm theo hướng dẫn

## Bước 2: Thêm Android App vào Firebase

1. Trong Firebase Console, click biểu tượng Android
2. Nhập Package name: `com.example.cuoiki` (giống với applicationId trong build.gradle.kts)
3. Tải file `google-services.json`
4. Copy file `google-services.json` vào thư mục `app/` (cùng cấp với build.gradle.kts)

## Bước 3: Cấu hình Firebase Realtime Database

1. Trong Firebase Console, vào "Realtime Database"
2. Click "Create Database"
3. Chọn location (ví dụ: asia-southeast1)
4. Chọn "Start in test mode" (hoặc cấu hình rules phù hợp)

### Rules cho Realtime Database (nếu cần):

```json
{
  "rules": {
    "backups": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

## Bước 4: Cấu hình Firebase Authentication (Tùy chọn)

Nếu muốn backup theo từng user:
1. Vào "Authentication" trong Firebase Console
2. Enable "Anonymous" hoặc "Email/Password" authentication
3. Code sẽ tự động sử dụng user ID để lưu backup riêng cho mỗi user

## Bước 5: Build và chạy app

1. Sync Gradle files
2. Build project
3. Chạy app và vào ProfileActivity
4. Click "Backup lên Firebase" để backup dữ liệu
5. Click "Restore từ Firebase" để restore dữ liệu

## Lưu ý:

- Dữ liệu được backup bao gồm: Products, Accounts, Transactions
- Backup được lưu theo user ID (nếu đã đăng nhập Firebase) hoặc "anonymous"
- Đảm bảo có kết nối Internet khi backup/restore
- Dữ liệu restore sẽ được thêm vào database hiện tại (không xóa dữ liệu cũ)

