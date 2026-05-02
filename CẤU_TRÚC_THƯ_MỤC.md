# Cấu trúc thư mục mới cho dự án Android

## Cấu trúc đã được tạo:

```
com/example/cuoiki/
├── activities/
│   ├── auth/              # Xác thực
│   │   ├── LoginActivity.java
│   │   └── CreateAccountActivity.java
│   ├── account/           # Quản lý tài khoản
│   │   ├── AccountManageActivity.java
│   │   └── ProfileActivity.java
│   ├── product/           # Quản lý sản phẩm
│   │   ├── ManageProductActivity.java
│   │   ├── AddActivity.java
│   │   ├── UpdateActivity.java
│   │   ├── BuyActivity.java
│   │   └── ExportnActivity.java
│   ├── transaction/       # Giao dịch
│   │   └── StaticActivity.java
│   ├── notification/      # Thông báo
│   │   ├── NotificationActivity.java
│   │   └── NotificationManageActivity.java
│   └── common/           # Màn hình chung
│       ├── MainActivity.java
│       └── SearchActivity.java
├── adapters/
│   ├── account/
│   │   └── AccountAdapter.java
│   ├── product/
│   │   ├── ProductAdapter.java
│   │   └── ManageProductAdapter.java
│   ├── transaction/
│   │   └── TransactionsAdapter.java
│   └── notification/
│       ├── NotificationAdapter.java
│       ├── NotificationHistoryAdapter.java
│       └── UserSelectAdapter.java
├── models/
│   ├── User.java
│   ├── Product.java
│   ├── Transaction.java
│   └── Notification.java
└── helpers/
    ├── SQLiteHelper.java
    └── FirebaseBackupHelper.java
```

## Cách cập nhật package và import:

### 1. Package declaration:
Thay đổi từ:
```java
package com.example.cuoiki;
```

Thành:
- Activities: `package com.example.cuoiki.activities.[thư mục];`
- Adapters: `package com.example.cuoiki.adapters.[thư mục];`
- Models: `package com.example.cuoiki.models;`
- Helpers: `package com.example.cuoiki.helpers;`

### 2. Import statements:
Cập nhật tất cả các import để trỏ đến package mới:
```java
// Models
import com.example.cuoiki.models.User;
import com.example.cuoiki.models.Product;
import com.example.cuoiki.models.Transaction;
import com.example.cuoiki.models.Notification;

// Helpers
import com.example.cuoiki.helpers.SQLiteHelper;
import com.example.cuoiki.helpers.FirebaseBackupHelper;

// Activities
import com.example.cuoiki.activities.auth.LoginActivity;
import com.example.cuoiki.activities.common.MainActivity;
// ... và các activities khác

// Adapters
import com.example.cuoiki.adapters.account.AccountAdapter;
import com.example.cuoiki.adapters.product.ProductAdapter;
// ... và các adapters khác
```

## Lưu ý:
- Cần cập nhật AndroidManifest.xml với package mới cho các Activities
- Cần cập nhật tất cả các Intent để trỏ đến class mới
- Kiểm tra lại tất cả các import trong các file

