# BOTTOM NAVIGATION VIEW - HIỂN THỊ LABEL DƯỚI ICON
## Material Design BottomNavigationView - Ứng dụng Cuối Kỳ

---

## 1. TỔNG QUAN

### 1.1. Component sử dụng
Ứng dụng sử dụng **BottomNavigationView** từ **Material Design Components** để tạo thanh điều hướng dưới cùng màn hình.

**BottomNavigationView** là:
- Component của Material Design
- Hiển thị các icon với label (chữ) bên dưới
- Tự động highlight item được chọn
- Hỗ trợ animation mượt mà

---

## 2. DEPENDENCY (Thư viện)

### 2.1. Trong file `build.gradle.kts`

```kotlin
implementation(libs.material)  // Material Design Components
```

**Material Design Components** bao gồm:
- BottomNavigationView
- NavigationView
- FloatingActionButton
- Snackbar
- Và nhiều component khác

---

## 3. CẤU HÌNH BOTTOM NAVIGATION

### 3.1. File Menu: `bottom_nav_customer.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@+id/nav_home"
        android:icon="@drawable/bottom_btn1"
        android:title="Trang chủ" />
    <item
        android:id="@+id/nav_search"
        android:icon="@drawable/search"
        android:title="Tìm kiếm" />
    <item
        android:id="@+id/nav_favorite"
        android:icon="@drawable/ic_favorite"
        android:title="Yêu thích" />
    <item
        android:id="@+id/nav_cart"
        android:icon="@drawable/buy"
        android:title="Giỏ hàng" />
    <item
        android:id="@+id/nav_profile"
        android:icon="@drawable/bottom_btn2"
        android:title="Tài khoản" />
</menu>
```

### 3.2. Giải thích các thuộc tính

#### A. `android:id`
- **Chức năng**: Định danh duy nhất cho mỗi item
- **Ví dụ**: `@+id/nav_search`

#### B. `android:icon`
- **Chức năng**: Icon hiển thị cho item
- **Ví dụ**: `@drawable/search` (icon kính lúp)

#### C. `android:title`
- **Chức năng**: **Chữ hiển thị dưới icon** (label)
- **Ví dụ**: `"Tìm kiếm"` - Đây chính là chữ xuất hiện dưới icon kính lúp!

---

## 4. LAYOUT XML

### 4.1. Trong `activity_customer_main.xml`

```xml
<com.google.android.material.bottomnavigation.BottomNavigationView
    android:id="@+id/bottomNavigation"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_alignParentBottom="true"
    app:menu="@menu/bottom_nav_customer" />
```

### 4.2. Giải thích các thuộc tính

- **`android:id`**: ID của BottomNavigationView
- **`android:layout_width`**: Chiều rộng (match_parent = toàn màn hình)
- **`android:layout_height`**: Chiều cao (wrap_content = tự động)
- **`app:menu`**: **Quan trọng!** Link đến file menu XML chứa các item

---

## 5. CODE JAVA

### 5.1. Khởi tạo trong CustomerMainActivity

```java
// Bottom navigation
private BottomNavigationView bottomNavigation;

private void initViews() {
    bottomNavigation = findViewById(R.id.bottomNavigation);
    
    // Setup click listener
    bottomNavigation.setOnItemSelectedListener(item -> {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_search) {
            Intent intent = new Intent(this, CustomerSearchActivity.class);
            startActivity(intent);
            return true;
        }
        // ... các item khác
        return false;
    });
}
```

### 5.2. Xử lý click item

```java
bottomNavigation.setOnItemSelectedListener(item -> {
    int itemId = item.getItemId();
    
    if (itemId == R.id.nav_home) {
        // Đã ở trang chủ
        return true;
    } else if (itemId == R.id.nav_search) {
        // Chuyển đến màn hình tìm kiếm
        Intent intent = new Intent(this, CustomerSearchActivity.class);
        startActivity(intent);
        return true;
    } else if (itemId == R.id.nav_favorite) {
        Intent intent = new Intent(this, FavoriteActivity.class);
        startActivity(intent);
        return true;
    }
    // ... các item khác
    
    return false;
});
```

---

## 6. CÁCH HIỂN THỊ LABEL (CHỮ DƯỚI ICON)

### 6.1. Tự động hiển thị
**BottomNavigationView tự động hiển thị label dưới icon** dựa trên:
- Thuộc tính `android:title` trong file menu XML
- Material Design tự động render label

### 6.2. Các chế độ hiển thị

#### A. Chế độ mặc định (3-5 items)
- **Hiển thị**: Icon + Label (chữ) luôn hiển thị
- **Ví dụ**: Icon kính lúp + chữ "Tìm kiếm" bên dưới

#### B. Chế độ nhiều items (>5)
- **Hiển thị**: Chỉ icon, label ẩn (trừ item được chọn)
- **Có thể cấu hình**: `app:labelVisibilityMode`

### 6.3. Cấu hình Label Visibility

```xml
<com.google.android.material.bottomnavigation.BottomNavigationView
    android:id="@+id/bottomNavigation"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:menu="@menu/bottom_nav_customer"
    app:labelVisibilityMode="labeled" />  <!-- Luôn hiển thị label -->
```

**Các giá trị `labelVisibilityMode`:**
- **`labeled`**: Luôn hiển thị label
- **`unlabeled`**: Không hiển thị label
- **`auto`**: Tự động (mặc định)
- **`selected`**: Chỉ hiển thị label cho item được chọn

---

## 7. CÁC THUỘC TÍNH KHÁC

### 7.1. Màu sắc

```xml
<com.google.android.material.bottomnavigation.BottomNavigationView
    app:itemIconTint="@color/bottom_nav_icon_color"
    app:itemTextColor="@color/bottom_nav_text_color" />
```

### 7.2. Background

```xml
<com.google.android.material.bottomnavigation.BottomNavigationView
    android:background="@color/bottom_nav_background" />
```

### 7.3. Elevation (Đổ bóng)

```xml
<com.google.android.material.bottomnavigation.BottomNavigationView
    app:elevation="8dp" />
```

---

## 8. CÁCH HOẠT ĐỘNG

### 8.1. Quy trình hiển thị

```
1. BottomNavigationView load menu từ XML
   ↓
2. Parse các item (icon, title)
   ↓
3. Render icon và label (chữ) cho mỗi item
   ↓
4. Hiển thị trên màn hình
   ↓
5. Khi click item → gọi onItemSelectedListener
```

### 8.2. Tại sao chữ "Tìm kiếm" xuất hiện?

- **File menu XML** có `android:title="Tìm kiếm"` cho item `nav_search`
- **BottomNavigationView** tự động đọc `android:title` và hiển thị dưới icon
- **Material Design** tự động render label với style đẹp mắt

---

## 9. SO SÁNH VỚI CÁC CÁCH KHÁC

### 9.1. BottomNavigationView vs Custom Layout

#### BottomNavigationView (Material Design)
- ✅ **Tự động**: Hiển thị label tự động
- ✅ **Animation**: Có animation mượt mà
- ✅ **Material Design**: Tuân thủ Material Design guidelines
- ✅ **Dễ dùng**: Chỉ cần định nghĩa menu XML

#### Custom Layout (LinearLayout + ImageView + TextView)
- ⚠️ **Thủ công**: Phải tự code hiển thị label
- ⚠️ **Không có animation**: Phải tự implement
- ⚠️ **Phức tạp hơn**: Nhiều code hơn

### 9.2. BottomNavigationView vs TabLayout

- **BottomNavigationView**: Dùng cho navigation chính (5 items trở xuống)
- **TabLayout**: Dùng cho tabs trong cùng màn hình

---

## 10. TÀI LIỆU THAM KHẢO

### 10.1. Official Documentation
- **Material Design Bottom Navigation**: https://material.io/components/bottom-navigation
- **Android BottomNavigationView**: https://developer.android.com/reference/com/google/android/material/bottomnavigation/BottomNavigationView

### 10.2. Material Design Guidelines
- **Bottom Navigation**: https://material.io/design/components/bottom-navigation.html

---

## 11. KẾT LUẬN

**Chữ "Tìm kiếm" xuất hiện dưới icon kính lúp** là do:

1. ✅ **File menu XML** (`bottom_nav_customer.xml`) có thuộc tính `android:title="Tìm kiếm"`
2. ✅ **BottomNavigationView** tự động đọc và hiển thị label từ `android:title`
3. ✅ **Material Design** tự động render với style đẹp mắt

**Công nghệ sử dụng:**
- **Component**: `com.google.android.material.bottomnavigation.BottomNavigationView`
- **Thư viện**: Material Design Components
- **Cấu hình**: File menu XML với `android:title`

**Không cần code thêm** - BottomNavigationView tự động xử lý việc hiển thị label!

---

## 12. HÌNH ẢNH MINH HỌA

```
┌─────────────────────────────────┐
│                                 │
│      Nội dung màn hình          │
│                                 │
├─────────────────────────────────┤
│  🏠      🔍      ❤️      🛒      👤 │
│Trang   Tìm    Yêu   Giỏ  Tài    │
│chủ    kiếm   thích hàng khoản   │
└─────────────────────────────────┘
     ↑
  Label tự động
  hiển thị từ
  android:title
```

---

*Tài liệu này giải thích cách BottomNavigationView hiển thị label (chữ) dưới icon.*

