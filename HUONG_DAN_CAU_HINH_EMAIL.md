# HƯỚNG DẪN CẤU HÌNH GỬI EMAIL TỰ ĐỘNG

## 📧 Cấu hình SMTP Server

### Cách 1: Sử dụng Gmail SMTP (Miễn phí)

1. **Tạo App Password cho Gmail:**
   - Vào https://myaccount.google.com/
   - Bật 2-Step Verification (nếu chưa bật)
   - Vào "App passwords" → Tạo app password mới
   - Copy app password (16 ký tự)

2. **Cập nhật EmailSender.java:**
   ```java
   private static final String SMTP_HOST = "smtp.gmail.com";
   private static final String SMTP_PORT = "587";
   private static final String SMTP_USERNAME = "your-email@gmail.com"; // Email của bạn
   private static final String SMTP_PASSWORD = "your-16-char-app-password"; // App Password
   ```

### Cách 2: Sử dụng SendGrid (Miễn phí 100 emails/ngày)

1. **Đăng ký SendGrid:**
   - Vào https://sendgrid.com/
   - Đăng ký tài khoản miễn phí
   - Tạo API Key

2. **Cập nhật EmailSender.java:**
   ```java
   private static final String SMTP_HOST = "smtp.sendgrid.net";
   private static final String SMTP_PORT = "587";
   private static final String SMTP_USERNAME = "apikey";
   private static final String SMTP_PASSWORD = "your-sendgrid-api-key";
   ```

### Cách 3: Sử dụng SMTP Server khác

Bạn có thể dùng bất kỳ SMTP server nào:
- Outlook: `smtp-mail.outlook.com` (port 587)
- Yahoo: `smtp.mail.yahoo.com` (port 587)
- Custom SMTP server của bạn

## ⚙️ Cấu hình trong Code

Mở file `EmailSender.java` và cập nhật các thông tin sau:

```java
private static final String SMTP_HOST = "smtp.gmail.com";
private static final String SMTP_PORT = "587";
private static final String SMTP_USERNAME = "your-email@gmail.com";
private static final String SMTP_PASSWORD = "your-app-password";
```

## 🔒 Bảo mật

**QUAN TRỌNG:** Không nên hardcode password trong code. Nên:
1. Lưu trong `strings.xml` (vẫn không an toàn lắm)
2. Hoặc dùng BuildConfig với local.properties (tốt hơn)
3. Hoặc dùng backend server để gửi email (an toàn nhất)

## 📝 Lưu ý

- Gmail: Cần App Password, không dùng mật khẩu thường
- SendGrid: Miễn phí 100 emails/ngày
- Email được gửi trong background thread, không block UI
- Kiểm tra logcat để debug nếu có lỗi

## 🐛 Debug

Nếu email không gửi được, kiểm tra:
1. Logcat với tag "EmailSender"
2. Kiểm tra SMTP credentials
3. Kiểm tra internet connection
4. Kiểm tra firewall/antivirus có chặn không



