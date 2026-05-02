# CÔNG NGHỆ QUẢN LÝ THÔNG BÁO
## NotificationManageActivity - Ứng dụng Cuối Kỳ

---

## 1. TỔNG QUAN

### 1.1. Mục đích
Màn hình quản lý thông báo cho phép admin:
- Tạo thông báo mới (tiêu đề, nội dung)
- Chọn người nhận (tất cả, nhân viên, khách hàng, hoặc chọn từng người)
- Gửi thông báo đến nhiều người cùng lúc
- Xem lịch sử thông báo đã gửi
- Thu hồi thông báo

### 1.2. Công nghệ sử dụng
- **Language**: Java
- **Platform**: Android (minSdk 24, targetSdk 34)
- **UI Framework**: AndroidX (Material Design)
- **Database**: SQLite (SQLiteHelper)
- **UI Components**: RadioButton, RecyclerView, Dialog
- **Storage**: SharedPreferences (lưu thông tin user)

---

## 2. CÁC COMPONENT CHÍNH

### 2.1. RadioButton Group
- **Chức năng**: Chọn loại người nhận
- **Options**: 
  - Tất cả (All)
  - Tất cả nhân viên (All Employees)
  - Tất cả khách hàng (All Customers)
  - Chọn từng người (Select)

### 2.2. RecyclerView
- **Chức năng**: Hiển thị danh sách user để chọn
- **Layout Manager**: LinearLayoutManager
- **Adapter**: UserSelectAdapter (với checkbox)

### 2.3. Dialog
- **Chức năng**: Hiển thị lịch sử thông báo đã gửi
- **Type**: Full-screen Dialog
- **Adapter**: NotificationHistoryAdapter

### 2.4. SQLite Database
- **Chức năng**: Lưu trữ thông báo
- **Table**: notifications
- **Operations**: Insert, Select, Delete

---

## 3. CODE IMPLEMENTATION

### 3.1. Setup RadioButton Group

```java
// Setup radio button listeners
optionAll.setOnClickListener(v -> {
    radioAll.setChecked(true);
    radioAllEmployees.setChecked(false);
    radioAllCustomers.setChecked(false);
    radioSelect.setChecked(false);
    recyclerViewUsers.setVisibility(View.GONE);
});

optionAllEmployees.setOnClickListener(v -> {
    radioAll.setChecked(false);
    radioAllEmployees.setChecked(true);
    radioAllCustomers.setChecked(false);
    radioSelect.setChecked(false);
    recyclerViewUsers.setVisibility(View.GONE);
});

optionAllCustomers.setOnClickListener(v -> {
    radioAll.setChecked(false);
    radioAllEmployees.setChecked(false);
    radioAllCustomers.setChecked(true);
    radioSelect.setChecked(false);
    recyclerViewUsers.setVisibility(View.GONE);
});

optionSelect.setOnClickListener(v -> {
    radioAll.setChecked(false);
    radioAllEmployees.setChecked(false);
    radioAllCustomers.setChecked(false);
    radioSelect.setChecked(true);
    recyclerViewUsers.setVisibility(View.VISIBLE); // Hiển thị danh sách user
});
```

**Giải thích:**
- **RadioButton Group**: Chỉ cho phép chọn 1 option
- **Visibility**: Ẩn/hiện RecyclerView khi chọn "Chọn từng người"
- **Mutual Exclusion**: Tự động bỏ chọn các option khác

### 3.2. Setup RecyclerView - Danh Sách User

```java
// Setup RecyclerView cho danh sách user (tất cả user)
recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));
List<User> userList = dbHelper.getAllUsers();
userAdapter = new UserSelectAdapter(userList);
recyclerViewUsers.setAdapter(userAdapter);
```

**Giải thích:**
- **LinearLayoutManager**: Danh sách dọc
- **getAllUsers()**: Lấy tất cả user từ database
- **UserSelectAdapter**: Adapter với checkbox để chọn user

### 3.3. Gửi Thông Báo

```java
private void sendNotification() {
    String title = edtTitle.getText().toString().trim();
    String message = edtMessage.getText().toString().trim();

    // Validation
    if (TextUtils.isEmpty(title)) {
        Toast.makeText(this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
        return;
    }

    if (TextUtils.isEmpty(message)) {
        Toast.makeText(this, "Vui lòng nhập nội dung", Toast.LENGTH_SHORT).show();
        return;
    }

    // Get current date
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    String createdDate = sdf.format(new Date());

    boolean success = false;

    if (radioAll.isChecked()) {
        // Gửi đến tất cả (nhân viên và khách hàng)
        success = dbHelper.addNotification(title, message, currentUsername, "ALL", createdDate);
    } else if (radioAllEmployees.isChecked()) {
        // Gửi đến tất cả nhân viên
        List<User> allUsers = dbHelper.getAllUsers();
        for (User user : allUsers) {
            String role = user.getRole();
            if (role != null && (role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("employee"))) {
                dbHelper.addNotification(title, message, currentUsername, user.getUsername(), createdDate);
            }
        }
        success = true;
    } else if (radioAllCustomers.isChecked()) {
        // Gửi đến tất cả khách hàng
        List<User> allUsers = dbHelper.getAllUsers();
        for (User user : allUsers) {
            String role = user.getRole();
            if (role != null && role.equalsIgnoreCase("customer")) {
                dbHelper.addNotification(title, message, currentUsername, user.getUsername(), createdDate);
            }
        }
        success = true;
    } else {
        // Gửi đến các user đã chọn
        List<String> selectedUsernames = userAdapter.getSelectedUsernames();
        if (selectedUsernames.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một người nhận", Toast.LENGTH_SHORT).show();
            return;
        }

        for (String username : selectedUsernames) {
            dbHelper.addNotification(title, message, currentUsername, username, createdDate);
        }
        success = true;
    }

    if (success) {
        Toast.makeText(this, "Gửi thông báo thành công", Toast.LENGTH_SHORT).show();
        finish();
    } else {
        Toast.makeText(this, "Gửi thông báo thất bại", Toast.LENGTH_SHORT).show();
    }
}
```

**Giải thích:**
- **Validation**: Kiểm tra tiêu đề và nội dung không rỗng
- **Date Format**: Tạo ngày giờ hiện tại (yyyy-MM-dd HH:mm:ss)
- **4 trường hợp gửi**:
  1. Tất cả: Gửi với recipient = "ALL"
  2. Tất cả nhân viên: Lọc user theo role (admin/employee)
  3. Tất cả khách hàng: Lọc user theo role (customer)
  4. Chọn từng người: Lấy danh sách user đã chọn từ adapter
- **Database**: Lưu từng thông báo vào database

### 3.4. Hiển Thị Lịch Sử Thông Báo

```java
private void showHistoryDialog() {
    // Lấy danh sách thông báo đã gửi
    List<Notification> sentNotifications = dbHelper.getSentNotifications(currentUsername);

    if (sentNotifications.isEmpty()) {
        Toast.makeText(this, "Chưa có thông báo nào được gửi", Toast.LENGTH_SHORT).show();
        return;
    }

    // Tạo dialog để hiển thị lịch sử
    Dialog dialog = new Dialog(this);
    dialog.setContentView(R.layout.dialog_notification_history);
    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

    RecyclerView recyclerViewHistory = dialog.findViewById(R.id.recyclerViewHistory);
    Button btnClose = dialog.findViewById(R.id.btnClose);

    recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
    NotificationHistoryAdapter historyAdapter = new NotificationHistoryAdapter(sentNotifications, notification -> {
        // Thu hồi thông báo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thu hồi thông báo");
        builder.setMessage("Bạn có chắc chắn muốn thu hồi thông báo này?");
        builder.setPositiveButton("Thu hồi", (d, which) -> {
            dbHelper.deleteNotification(notification.getId());
            Toast.makeText(this, "Đã thu hồi thông báo", Toast.LENGTH_SHORT).show();
            showHistoryDialog(); // Reload
            dialog.dismiss();
        });
        builder.setNegativeButton("Hủy", (d, which) -> d.dismiss());
        builder.show();
    });
    recyclerViewHistory.setAdapter(historyAdapter);

    btnClose.setOnClickListener(v -> dialog.dismiss());
    dialog.show();
}
```

**Giải thích:**
- **Full-screen Dialog**: Dialog chiếm toàn màn hình
- **getSentNotifications()**: Lấy thông báo đã gửi bởi user hiện tại
- **NotificationHistoryAdapter**: Adapter hiển thị lịch sử
- **Thu hồi**: Xóa thông báo khỏi database

---

## 4. USER SELECT ADAPTER

### 4.1. UserSelectAdapter.java

```java
public class UserSelectAdapter extends RecyclerView.Adapter<UserSelectAdapter.UserViewHolder> {
    
    private List<User> userList;
    private Set<String> selectedUsernames = new HashSet<>();

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        
        holder.tvUsername.setText(user.getUsername());
        holder.tvRole.setText(user.getRole());
        
        // Checkbox state
        holder.checkbox.setChecked(selectedUsernames.contains(user.getUsername()));
        
        // Toggle selection
        holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedUsernames.add(user.getUsername());
            } else {
                selectedUsernames.remove(user.getUsername());
            }
        });
    }
    
    public List<String> getSelectedUsernames() {
        return new ArrayList<>(selectedUsernames);
    }
}
```

**Giải thích:**
- **HashSet**: Lưu danh sách username đã chọn (tránh duplicate)
- **Checkbox**: Cho phép chọn/bỏ chọn user
- **getSelectedUsernames()**: Trả về danh sách user đã chọn

---

## 5. DATABASE OPERATIONS

### 5.1. Thêm Thông Báo

```java
// SQLiteHelper.addNotification()
public boolean addNotification(String title, String message, String sender, String recipient, String createdDate) {
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put("title", title);
    values.put("message", message);
    values.put("sender_username", sender);
    values.put("recipient_username", recipient);
    values.put("created_date", createdDate);
    values.put("is_read", 0); // 0 = chưa đọc, 1 = đã đọc
    
    long result = db.insert("notifications", null, values);
    return result != -1;
}
```

### 5.2. Lấy Thông Báo Đã Gửi

```java
// SQLiteHelper.getSentNotifications()
public List<Notification> getSentNotifications(String senderUsername) {
    List<Notification> notifications = new ArrayList<>();
    SQLiteDatabase db = this.getReadableDatabase();
    
    String query = "SELECT * FROM notifications WHERE sender_username = ? ORDER BY created_date DESC";
    Cursor cursor = db.rawQuery(query, new String[]{senderUsername});
    
    while (cursor.moveToNext()) {
        Notification notification = new Notification();
        notification.setId(cursor.getInt(0));
        notification.setTitle(cursor.getString(1));
        notification.setMessage(cursor.getString(2));
        notification.setSenderUsername(cursor.getString(3));
        notification.setRecipientUsername(cursor.getString(4));
        notification.setCreatedDate(cursor.getString(5));
        notification.setRead(cursor.getInt(6) == 1);
        notifications.add(notification);
    }
    
    cursor.close();
    return notifications;
}
```

---

## 6. CÁC TÍNH NĂNG CHÍNH

### 6.1. Tạo Thông Báo
- ✅ Nhập tiêu đề và nội dung
- ✅ Validation input
- ✅ Format ngày giờ

### 6.2. Chọn Người Nhận
- ✅ RadioButton group (4 options)
- ✅ Hiển thị/ẩn danh sách user
- ✅ Checkbox để chọn từng user
- ✅ Lọc user theo role

### 6.3. Gửi Thông Báo
- ✅ Gửi đến tất cả
- ✅ Gửi đến nhân viên
- ✅ Gửi đến khách hàng
- ✅ Gửi đến user đã chọn
- ✅ Lưu vào database

### 6.4. Lịch Sử
- ✅ Xem thông báo đã gửi
- ✅ Thu hồi thông báo
- ✅ Full-screen dialog

---

## 7. DEPENDENCIES

### 7.1. Trong `build.gradle.kts`

```kotlin
// RecyclerView (có sẵn trong AndroidX)
implementation(libs.appcompat)  // Bao gồm RecyclerView

// SQLite (có sẵn trong Android)
// Không cần thêm dependency
```

---

## 8. SO SÁNH VỚI CÁC CÁCH KHÁC

### 8.1. RadioButton Group vs Spinner

#### RadioButton Group
- ✅ **Visual**: Dễ nhìn, rõ ràng
- ✅ **Multiple options**: Hiển thị tất cả options
- ✅ **User-friendly**: Click trực tiếp

#### Spinner
- ⚠️ **Hidden options**: Phải mở mới thấy
- ⚠️ **Less visual**: Không rõ ràng bằng

### 8.2. Checkbox vs Multi-select Spinner

#### Checkbox trong RecyclerView
- ✅ **Visual**: Thấy tất cả user
- ✅ **Easy selection**: Click trực tiếp
- ✅ **Scrollable**: Có thể scroll danh sách dài

#### Multi-select Spinner
- ⚠️ **Hidden**: Phải mở mới thấy
- ⚠️ **Less user-friendly**: Khó chọn nhiều

---

## 9. TÀI LIỆU THAM KHẢO

### 9.1. Official Documentation
- **RecyclerView**: https://developer.android.com/guide/topics/ui/layout/recyclerview
- **RadioButton**: https://developer.android.com/reference/android/widget/RadioButton
- **SQLite**: https://developer.android.com/training/data-storage/sqlite

---

## 10. KẾT LUẬN

**Quản lý Thông Báo** sử dụng các công nghệ:
- ✅ **RadioButton Group**: Chọn loại người nhận
- ✅ **RecyclerView**: Hiển thị danh sách user
- ✅ **Checkbox**: Chọn từng user
- ✅ **SQLite**: Lưu trữ thông báo
- ✅ **Dialog**: Hiển thị lịch sử
- ✅ **SharedPreferences**: Lưu thông tin user

**Công nghệ nổi bật:**
1. RadioButton Group (mutual exclusion)
2. RecyclerView với Checkbox (multi-select)
3. SQLite operations (insert, select, delete)
4. Full-screen Dialog (lịch sử)

---

*Tài liệu này giải thích công nghệ sử dụng trong quản lý thông báo.*

