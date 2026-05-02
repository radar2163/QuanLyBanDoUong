# Hướng dẫn cấu trúc thư mục mới - Mỗi màn hình một thư mục

## Cấu trúc mới:

```
com/example/cuoiki/
├── MainActivity/
│   ├── MainActivity.java
│   └── ProductAdapter.java (dùng chung với SearchActivity)
├── LoginActivity/
│   ├── LoginActivity.java
│   └── CreateAccountActivity.java
├── AccountManage/
│   ├── AccountManageActivity.java
│   └── AccountAdapter.java
├── ProfileActivity/
│   └── ProfileActivity.java
├── ManageProduct/
│   ├── ManageProductActivity.java
│   └── ManageProductAdapter.java
├── AddActivity/
│   └── AddActivity.java
├── UpdateActivity/
│   └── UpdateActivity.java
├── BuyActivity/
│   └── BuyActivity.java
├── ExportnActivity/
│   └── ExportnActivity.java
├── SearchActivity/
│   └── SearchActivity.java (import ProductAdapter từ MainActivity)
├── StaticActivity/
│   ├── StaticActivity.java
│   └── TransactionsAdapter.java
├── NotificationActivity/
│   ├── NotificationActivity.java
│   └── NotificationAdapter.java
├── NotificationManage/
│   ├── NotificationManageActivity.java
│   ├── NotificationHistoryAdapter.java
│   └── UserSelectAdapter.java
├── models/ (dùng chung)
│   ├── User.java
│   ├── Product.java
│   ├── Transaction.java
│   └── Notification.java
└── helpers/ (dùng chung)
    ├── SQLiteHelper.java
    └── FirebaseBackupHelper.java
```

## Cách cập nhật:

### 1. Package declaration:
- MainActivity: `package com.example.cuoiki.MainActivity;`
- LoginActivity: `package com.example.cuoiki.LoginActivity;`
- AccountManage: `package com.example.cuoiki.AccountManage;`
- ... tương tự cho các màn hình khác

### 2. Import statements:
```java
// Models (dùng chung)
import com.example.cuoiki.User;
import com.example.cuoiki.Product;
import com.example.cuoiki.Transaction;
import com.example.cuoiki.Notification;

// Helpers (dùng chung)
import com.example.cuoiki.helpers.SQLiteHelper;
import com.example.cuoiki.helpers.FirebaseBackupHelper;

// Activities từ các thư mục khác
import com.example.cuoiki.MainActivity.MainActivity;
import com.example.cuoiki.LoginActivity.LoginActivity;
import com.example.cuoiki.AccountManage.AccountManageActivity;
// ... và các activities khác

// Adapters từ các thư mục tương ứng
import com.example.cuoiki.MainActivity.ProductAdapter;
import com.example.cuoiki.AccountManage.AccountAdapter;
// ... và các adapters khác
```

## Đã hoàn thành:
✅ Tạo thư mục cho tất cả các màn hình
✅ Di chuyển MainActivity và ProductAdapter vào thư mục MainActivity
✅ Cập nhật package và import cho MainActivity

## Cần hoàn thành:
- Di chuyển các Activities và Adapters còn lại vào thư mục tương ứng
- Cập nhật package và import cho tất cả các file
- Cập nhật AndroidManifest.xml với package mới

