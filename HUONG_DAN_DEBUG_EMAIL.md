# HƯỚNG DẪN DEBUG LỖI GỬI EMAIL

## 🔍 Cách kiểm tra lỗi chi tiết:

1. **Mở Logcat trong Android Studio:**
   - View → Tool Windows → Logcat
   - Filter theo tag: `EmailSender`

2. **Xem lỗi cụ thể:**
   - Nếu thấy "Authentication Failed" → Lỗi xác thực (email/password sai)
   - Nếu thấy "Connection refused" → Không kết nối được SMTP server
   - Nếu thấy "Could not connect to SMTP host" → SMTP host sai

## 🔧 Các giải pháp:

### Giải pháp 1: Thử SMTP của trường

Email trường học có thể dùng SMTP riêng. Thử cập nhật trong `EmailSender.java`:

```java
// Thử SMTP của trường TDMU
private static final String SMTP_HOST = "smtp.tdmu.edu.vn"; // hoặc mail.tdmu.edu.vn
private static final String SMTP_PORT = "587"; // hoặc 465
private static final String SMTP_USERNAME = "2224802010259@student.tdmu.edu.vn";
private static final String SMTP_PASSWORD = "mật-khẩu-thường-của-bạn"; // Có thể cần mật khẩu thường
```

### Giải pháp 2: Kiểm tra App Password

- App Password phải được tạo từ chính email `2224802010259@student.tdmu.edu.vn`
- Nếu email trường không hỗ trợ App Password, thử dùng mật khẩu thường

### Giải pháp 3: Dùng Gmail cá nhân

Nếu email trường không hoạt động, có thể dùng Gmail cá nhân:

```java
private static final String SMTP_USERNAME = "your-personal-gmail@gmail.com";
private static final String SMTP_PASSWORD = "app-password-from-gmail";
```

### Giải pháp 4: Tắt gửi email tự động (tạm thời)

Nếu không cần gửi email ngay, có thể comment lại dòng gửi email trong `CheckoutActivity.java`:

```java
// EmailHelper.sendOrderConfirmationEmail(this, order); // Tạm thời tắt
```

## 📝 Lưu ý:

- Email trường học thường có hạn chế về SMTP
- Có thể cần liên hệ IT của trường để lấy thông tin SMTP chính xác
- Hoặc dùng email Gmail cá nhân để test trước

